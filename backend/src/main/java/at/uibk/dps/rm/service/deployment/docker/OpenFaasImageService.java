package at.uibk.dps.rm.service.deployment.docker;

import at.uibk.dps.rm.entity.deployment.FunctionsToDeploy;
import at.uibk.dps.rm.entity.deployment.ProcessOutput;
import at.uibk.dps.rm.entity.dto.config.ConfigDTO;
import at.uibk.dps.rm.entity.dto.credentials.DockerCredentials;
import at.uibk.dps.rm.service.deployment.executor.ProcessExecutor;
import at.uibk.dps.rm.util.configuration.ConfigUtility;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.core.buffer.Buffer;
import lombok.AllArgsConstructor;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This service can be used to build docker images for OpenFaaS and push them to a docker registry.
 *
 * @author matthi-g
 */
@AllArgsConstructor
public class OpenFaasImageService {

    private final Vertx vertx;

    private final DockerCredentials dockerCredentials;

    private final FunctionsToDeploy functionsToDeploy;

    private final Path functionsDir;

    /**
     * Build and push a docker image for each entry in the functionsString that can be deployed to
     * OpenFaaS.
     *
     * @return a Single that emits the process output of the build process
     */
    public Single<ProcessOutput> buildOpenFaasImages() {
        if (functionsToDeploy.getDockerFunctionIdentifiers().isEmpty()) {
            return Single.just(new ProcessOutput());
        }
        String stackFile = String.format(
            "version: 1.0\n" +
                "provider:\n" +
                "  name: openfaas\n" +
                "functions:\n" +
                "%s\n", functionsToDeploy.getDockerFunctionsString());

        return createStackFile(functionsDir, stackFile)
            .andThen(generateFunctionsDockerFiles(functionsDir))
            .flatMap(buildOutput -> {
                if (buildOutput.getProcess().exitValue() != 0) {
                    return Single.just(buildOutput);
                }
                return new ConfigUtility(vertx).getConfigDTO()
                        .flatMap(config -> buildAndPushDockerImages(functionsDir, config))
                        .map(pushOutput -> {
                            pushOutput.setOutput(buildOutput.getOutput() + pushOutput.getOutput());
                            return pushOutput;
                        });
            });
    }

    /**
     * Create the stack file where all functions and function templates are defined.
     *
     * @param rootFolder the path where the stack file should be created
     * @param fileContent the content of the stack file
     * @return a Completable
     */
    private Completable createStackFile(Path rootFolder, String fileContent) {
        Path filePath = Path.of(rootFolder.toString(), "stack.yml");
        return vertx.fileSystem().writeFile(filePath.toString(), Buffer.buffer(fileContent));
    }

    /**
     * Generate a dockerfile for each function from the stack file.
     *
     * @param rootFolder the path where the stack file is located
     * @return a Single that emits the process output of the generation of the dockerfiles
     */
    private Single<ProcessOutput> generateFunctionsDockerFiles(Path rootFolder) {
        ProcessExecutor processExecutor = new ProcessExecutor(rootFolder.toAbsolutePath(),"faas-cli",
            "build", "-f", "stack.yml", "--shrinkwrap");
        return processExecutor.executeCli();
    }

    /**
     * Build and push all docker images with the dockerfiles located in the rootFolder.
     *
     * @param rootFolder the path to the dockerfiles
     * @param config the vertx config
     * @return a Single that emits the process output of the build and pushing process
     */
    private Single<ProcessOutput> buildAndPushDockerImages(Path rootFolder, ConfigDTO config) {
        String dindDir = config.getDindDirectory();
        List<String> dockerCommands = new java.util.ArrayList<>(List.of("docker", "run", "-v",
            "/var/run/docker.sock:/var/run/docker.sock", "--privileged", "--rm", "-v",
            Path.of(dindDir, rootFolder.toString()).toAbsolutePath().toString().replace("\\", "/") +
                "/build:/build","docker:24.0-cli", "/bin/sh", "-c"));
        StringBuilder dockerInteractiveCommands = getBuildxCreateCommands(config).append(getBuildxBuildCommands());
        dockerCommands.add(dockerInteractiveCommands.toString());
        ProcessExecutor processExecutor = new ProcessExecutor(rootFolder, dockerCommands);
        return processExecutor.executeCli();
    }

    private StringBuilder getBuildxCreateCommands(ConfigDTO config) {
        String buildxConfig = config.getDockerInsecureRegistries()
                .stream()
                .map(registry -> "[registry.\\\"" + registry + "\\\"]\n  insecure = true")
                .collect(Collectors.joining("\n"));
        return new StringBuilder("cd ./build && docker login " +
            dockerCredentials.getRegistry() + " -u " + dockerCredentials.getUsername() + " -p " +
            dockerCredentials.getAccessToken() +
            " && docker buildx create --name multiarch --driver docker-container --bootstrap --use --config " +
                "<(echo '" + buildxConfig + "') --driver-opt network=host");
    }

    private StringBuilder getBuildxBuildCommands() {
        StringBuilder dockerInteractiveCommands = new StringBuilder();
        for (String functionIdentifier : functionsToDeploy.getDockerFunctionIdentifiers()) {
            String ARCHITECTURES = "linux/amd64,linux/arm/v7";
            dockerInteractiveCommands.append(String.format(" && docker buildx build " +
                    "-t %s/%s/%s ./%s --platform %s --push --provenance=false", dockerCredentials.getRegistry(),
                dockerCredentials.getUsername(), functionIdentifier, functionIdentifier, ARCHITECTURES)
            );
        }
        return dockerInteractiveCommands;
    }
}
