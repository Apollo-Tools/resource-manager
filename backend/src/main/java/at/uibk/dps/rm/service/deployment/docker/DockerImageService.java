package at.uibk.dps.rm.service.deployment.docker;

import at.uibk.dps.rm.entity.dto.credentials.DockerCredentials;
import at.uibk.dps.rm.service.deployment.ProcessExecutor;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.core.buffer.Buffer;

import java.nio.file.Path;
import java.util.List;

public class DockerImageService {

    private final Vertx vertx;

    private final DockerCredentials dockerCredentials;


    private final List<String> functionIdentifiers;

    private final Path functionsDir;

    public DockerImageService(Vertx vertx, DockerCredentials dockerCredentials,
                              List<String> functionIdentifiers, Path functionsDir) {
        this.vertx = vertx;
        this.dockerCredentials = dockerCredentials;
        this.functionIdentifiers = functionIdentifiers;
        this.functionsDir = functionsDir;
    }

    public Single<Integer> buildAndPushDockerImages(String functionsString) {
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
        return vertx.fileSystem().writeFile(filePath.toString(), Buffer.buffer(fileContent));
    }

    private Single<Integer> buildFunctionsDockerFiles(Path rootFolder) {
        ProcessExecutor processExecutor = new ProcessExecutor(vertx, rootFolder,"faas-cli", "build", "-f",
            "stack.yml", "--shrinkwrap");
        return processExecutor.executeCli();
    }

    private Single<Integer> pushDockerImages(Path rootFolder) {
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
        ProcessExecutor processExecutor = new ProcessExecutor(vertx, rootFolder, dockerCommands);
        return processExecutor.executeCli();
    }
}