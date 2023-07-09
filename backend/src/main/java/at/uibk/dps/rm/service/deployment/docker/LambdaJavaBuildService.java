package at.uibk.dps.rm.service.deployment.docker;

import at.uibk.dps.rm.entity.deployment.DeploymentPath;
import at.uibk.dps.rm.entity.deployment.ProcessOutput;
import at.uibk.dps.rm.entity.dto.resource.PlatformEnum;
import at.uibk.dps.rm.entity.dto.resource.RuntimeEnum;
import at.uibk.dps.rm.entity.model.Function;
import at.uibk.dps.rm.entity.model.FunctionDeployment;
import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.service.deployment.executor.ProcessExecutor;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;

import java.nio.file.Path;
import java.util.List;

/**
 * This class is used to build java functions for deployment.
 *
 * @author matthi-g
 */
@RequiredArgsConstructor
public class LambdaJavaBuildService {
    private final List<FunctionDeployment> functionDeployments;

    private final DeploymentPath deploymentPath;

    /**
     * Builds and zips all java functions that are part of the functionDeployments.
     *
     * @return a Single that emits the output of the build process
     */
    public Single<ProcessOutput> buildAndZipJavaFunctions(String dindDirectory) {
        if (!hasLambdaJavaFunctions()) {
            return Single.just(new ProcessOutput());
        }
        String dindFunctionsPath = Path.of(dindDirectory, deploymentPath.getFunctionsFolder().toString())
            .toAbsolutePath().toString().replace("\\", "/");
        List<String> dockerCommands = new java.util.ArrayList<>(List.of("docker", "run", "--rm",
            "-v", dindFunctionsPath + ":/projects",
            "-w", "/projects", "gradle:7-jdk11-jammy", "/bin/sh", "-c"));
        String dockerInteractiveCommands = "for dir in *_java11;do cd \"$dir\";gradle buildLambdaZip; cd ..;done; exit";
        dockerCommands.add(dockerInteractiveCommands);
        ProcessExecutor processExecutor = new ProcessExecutor(deploymentPath.getFunctionsFolder(), dockerCommands);
        return processExecutor.executeCli();
    }

    /**
     * Check whether the functionDeployments contain at least one java function or not.
     *
     * @return true if the functionDeployments contain at least one java function, else false
     */
    private boolean hasLambdaJavaFunctions() {
        return functionDeployments.stream().anyMatch(functionDeployment -> {
            Function function = functionDeployment.getFunction();
            Resource resource = functionDeployment.getResource();
            boolean hasJava = RuntimeEnum.fromRuntime(function.getRuntime()).equals(RuntimeEnum.JAVA11);
            boolean hasLambda = PlatformEnum.fromPlatform(resource.getPlatform()).equals(PlatformEnum.LAMBDA);
            return hasLambda && hasJava;
        });
    }
}
