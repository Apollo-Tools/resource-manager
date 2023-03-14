package at.uibk.dps.rm.service.deployment.terraform;

import at.uibk.dps.rm.entity.dto.credentials.DockerCredentials;
import at.uibk.dps.rm.entity.model.Function;
import at.uibk.dps.rm.entity.model.FunctionResource;
import at.uibk.dps.rm.service.deployment.ProcessExecutor;
import at.uibk.dps.rm.service.deployment.sourcecode.PackagePythonCode;
import at.uibk.dps.rm.service.deployment.sourcecode.PackageSourceCode;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.core.buffer.Buffer;
import io.vertx.rxjava3.core.file.FileSystem;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class FunctionFileService {

    private final Vertx vertx;

    private final FileSystem fileSystem;

    private final List<FunctionResource> functionResources;

    private final Path functionsDir;

    private final DockerCredentials dockerCredentials;

    private final Set<Long> functionIds = new HashSet<>();

    private final List<String> functionIdentifiers = new ArrayList<>();

    private final Set<Long> dockerFunctions = new HashSet<>();

    public FunctionFileService(Vertx vertx, List<FunctionResource> functionResources, Path functionsDir,
                               DockerCredentials dockerCredentials) {
        this.vertx = vertx;
        this.fileSystem = vertx.fileSystem();
        this.functionResources = functionResources;
        this.functionsDir = functionsDir;
        this.dockerCredentials = dockerCredentials;
    }

    public Single<Integer> packageCode() throws IOException {
        PackageSourceCode packageSourceCode;
        StringBuilder functionsString = new StringBuilder();
        List<Completable> completables = new ArrayList<>();
        for (FunctionResource fr : functionResources) {
            Function function = fr.getFunction();
            if (functionIds.contains(function.getFunctionId())) {
                continue;
            }
            String runtime = function.getRuntime().getName().toLowerCase();
            String functionIdentifier =  function.getName().toLowerCase() +
                "_" + runtime.replace(".", "");
            if (runtime.startsWith("python")) {
                packageSourceCode = new PackagePythonCode(vertx, fileSystem);
                completables.add(packageSourceCode.composeSourceCode(functionsDir, functionIdentifier,
                    function.getCode()));
                if (deployFunctionOnVMOrEdge(function, functionResources)) {
                    functionsString.append(String.format(
                        "  %s:\n" +
                            "    lang: python3-flask-debian\n" +
                            "    handler: ./%s\n" +
                            "    image: %s/%s:latest\n", functionIdentifier, functionIdentifier,
                        dockerCredentials.getUsername(), functionIdentifier));
                    dockerFunctions.add(function.getFunctionId());
                }
            }
            functionIds.add(function.getFunctionId());
            functionIdentifiers.add(functionIdentifier);
        }
        // TODO: add check if this is necessary (=no changes since last push)
        if (dockerFunctions.isEmpty()) {
            return Single.just(0);
        }

        return Completable.merge(completables)
            // See for more information: https://github.com/ReactiveX/RxJava#deferred-dependent
            .andThen(Single.fromCallable(() -> buildAndPushDockerImages(functionsString.toString())))
            .flatMap(res -> res)
            .map(Process::exitValue);
    }

    private Single<Process> buildAndPushDockerImages(String functionsString) throws IOException {
        String stackFile = String.format(
            "version: 1.0\n" +
                "provider:\n" +
                "  name: openfaas\n" +
                "configuration:\n" +
                "  templates:\n" +
                "    - name: python3-flask-debian\n" +
                "functions:\n" +
                "%s\n", functionsString);

        return createStackFile(functionsDir, stackFile)
            .andThen(buildFunctionsDockerFiles(functionsDir))
            .flatMap(res -> pushDockerImages(functionsDir));
    }

    private Completable createStackFile(Path rootFolder, String fileContent) {
        Path filePath = Path.of(rootFolder.toString(), "stack.yml");
        return fileSystem.writeFile(filePath.toString(), Buffer.buffer(fileContent));
    }

    private Single<Process> buildFunctionsDockerFiles(Path rootFolder) throws IOException {
        ProcessExecutor processExecutor = new ProcessExecutor(rootFolder,"faas-cli", "build", "-f",
            "stack.yml", "--shrinkwrap");
        return processExecutor.executeCli();
    }

    private Single<Process> pushDockerImages(Path rootFolder) throws IOException {
        List<String> dockerCommands = new java.util.ArrayList<>(List.of("docker", "run", "-v",
            "\"/var/run/docker.sock:/var/run/docker.sock\"", "--privileged", "--rm", "-v",
            rootFolder.toAbsolutePath() + "\\build:/build", "docker:latest", "sh", "-c"));
        StringBuilder dockerInteractiveCommands = new StringBuilder("\"cd ./build && docker login -u " +
            dockerCredentials.getUsername() + " -p " + dockerCredentials.getAccessToken() + " && docker buildx create " +
            "--name multiarch --driver docker-container --bootstrap --use");
        for (String functionIdentifier : functionIdentifiers) {
            dockerInteractiveCommands.append(String.format(" && docker buildx build -t %s/%s ./%s --platform " +
                    "linux/arm/v7,linux/amd64 --push",
                dockerCredentials.getUsername(), functionIdentifier, functionIdentifier));
        }
        dockerInteractiveCommands.append("\"");
        dockerCommands.add(dockerInteractiveCommands.toString());
        ProcessExecutor processExecutor = new ProcessExecutor(rootFolder, dockerCommands);
        return processExecutor.executeCli();
    }

    private boolean deployFunctionOnVMOrEdge(Function function, List<FunctionResource> functionResources) {
        return functionResources.stream().anyMatch(functionResource -> {
            String resourceType = functionResource.getResource().getResourceType().getResourceType();
            return functionResource.getFunction().equals(function) &&
                (resourceType.equals("edge") || resourceType.equals("vm"));
        });
    }
}
