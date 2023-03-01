package at.uibk.dps.rm.service.deployment.terraform;

import at.uibk.dps.rm.entity.dto.credentials.DockerCredentials;
import at.uibk.dps.rm.entity.model.Function;
import at.uibk.dps.rm.entity.model.FunctionResource;
import at.uibk.dps.rm.service.deployment.ProcessExecutor;
import at.uibk.dps.rm.service.deployment.sourcecode.PackagePythonCode;
import at.uibk.dps.rm.service.deployment.sourcecode.PackageSourceCode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class FunctionFileService {

    private final List<FunctionResource> functionResources;

    private final Path functionsDir;

    private final DockerCredentials dockerCredentials;

    private final Set<Long> functionIds = new HashSet<>();

    private final List<String> functionIdentifiers = new ArrayList<>();

    public FunctionFileService(List<FunctionResource> functionResources, Path functionsDir,
                               DockerCredentials dockerCredentials) {
        this.functionResources = functionResources;
        this.functionsDir = functionsDir;
        this.dockerCredentials = dockerCredentials;
    }

    public void packageCode() throws IOException, InterruptedException {
        PackageSourceCode packageSourceCode;
        StringBuilder functionsString = new StringBuilder();
        for (FunctionResource fr : functionResources) {
            Function function = fr.getFunction();
            if (functionIds.contains(function.getFunctionId())) {
                continue;
            }
            String runtime = function.getRuntime().getName().toLowerCase();
            String functionIdentifier =  function.getName().toLowerCase() +
                "_" + runtime.replace(".", "");
            if (runtime.startsWith("python")) {
                packageSourceCode = new PackagePythonCode();
                packageSourceCode.composeSourceCode(functionsDir, functionIdentifier,
                    function.getCode());

                functionsString.append(String.format(
                    "  %s:\n" +
                    "    lang: python3-flask-debian\n" +
                    "    handler: ./%s\n" +
                    "    image: %s/%s:latest\n", functionIdentifier, functionIdentifier,
                    dockerCredentials.getUsername(), functionIdentifier));
            }
            functionIds.add(function.getFunctionId());
            functionIdentifiers.add(functionIdentifier);
        }

        String stackFile = String.format(
            "version: 1.0\n" +
                "provider:\n" +
                "  name: openfaas\n" +
                "configuration:\n" +
                "  templates:\n" +
                "    - name: python3-flask-debian\n" +
                "functions:\n" +
                "%s\n", functionsString);
        createStackFile(functionsDir, stackFile);

        buildFunctionsDockerFile(functionsDir);
        pushDockerFiles(functionsDir);
    }

    private void createStackFile(Path rootFolder, String fileContent) throws IOException {
        Path filePath = Path.of(rootFolder.toString(), "stack.yml");
        Files.writeString(filePath, fileContent, StandardCharsets.UTF_8, StandardOpenOption.CREATE);
    }

    private int buildFunctionsDockerFile(Path rootFolder) throws IOException, InterruptedException {
        ProcessExecutor processExecutor = new ProcessExecutor(rootFolder,"faas-cli", "build", "-f", "stack.yml", "--shrinkwrap");
        return processExecutor.executeCli();
    }

    private int pushDockerFiles(Path rootFolder) throws IOException, InterruptedException {
        List<String> dockerCommands = new java.util.ArrayList<>(List.of("docker", "run", "-v",
            "\"/var/run/docker.sock:/var/run/docker.sock\"", "--privileged", "--rm", "-v",
            rootFolder.toAbsolutePath() + "\\build:/build", "docker:latest", "sh", "-c"));
        StringBuilder dockerInteractiveCommands = new StringBuilder("\"cd ./build && docker login -u " +
            dockerCredentials.getUsername() + " -p " + dockerCredentials.getAccessToken());
        for (String functionIdentifier : functionIdentifiers) {
            dockerInteractiveCommands.append(String.format("&& docker build -t %s/%s ./%s",
                dockerCredentials.getUsername(), functionIdentifier, functionIdentifier));
            dockerInteractiveCommands.append(String.format("&& docker push %s/%s", dockerCredentials.getUsername(),
                functionIdentifier));
        }
        dockerInteractiveCommands.append("\"");
        dockerCommands.add(dockerInteractiveCommands.toString());
        ProcessExecutor processExecutor = new ProcessExecutor(rootFolder, dockerCommands);
        return processExecutor.executeCli();
    }
}
