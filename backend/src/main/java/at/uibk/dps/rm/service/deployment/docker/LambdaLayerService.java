package at.uibk.dps.rm.service.deployment.docker;

import at.uibk.dps.rm.entity.deployment.DeploymentPath;
import at.uibk.dps.rm.entity.deployment.ProcessOutput;
import at.uibk.dps.rm.entity.dto.resource.RuntimeEnum;
import at.uibk.dps.rm.entity.model.Function;
import at.uibk.dps.rm.entity.model.FunctionDeployment;
import at.uibk.dps.rm.service.deployment.executor.ProcessExecutor;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;

import java.nio.file.Path;
import java.util.List;

/**
 * This class is used to build layers for python functions that are deployed to AWS Lambda.
 *
 * @author matthi-g
 */
@RequiredArgsConstructor
public class LambdaLayerService {
    private final List<FunctionDeployment> functionDeployments;

    private final DeploymentPath deploymentPath;

    /**
     * Builds and zips the dependencies of all python functions as a layer.
     *
     * @return a Single that emits the output of the build process
     */
    public Single<ProcessOutput> buildLambdaLayers(String dindDirectory) {
        if (!hasZippedCode()) {
            return Single.just(new ProcessOutput());
        }
        String dindLayersPath = Path.of(dindDirectory, deploymentPath.getLayersFolder().toString())
            .toAbsolutePath().toString().replace("\\", "/");
        List<String> dockerCommands = new java.util.ArrayList<>(List.of("docker", "run", "--rm",
            "-v", dindLayersPath + ":/var/task",
            "public.ecr.aws/sam/build-python3.8:latest", "/bin/sh", "-c"));
        // Making the dummy file is necessary for blank requirements.txt files
        String dockerInteractiveCommands = "mkdir -p python; echo -e \"placeholder\" >> python/placeholder.txt;" +
            "for dir in ./*_python38;do pip install -r \"$dir\"/requirements.txt " +
            "-t python/lib/python3.8/site-packages/; done;zip -qr ./python38.zip ./python;rm -r ./python; exit";
        dockerCommands.add(dockerInteractiveCommands);
        ProcessExecutor processExecutor = new ProcessExecutor(deploymentPath.getFunctionsFolder(), dockerCommands);
        return processExecutor.executeCli();
    }

    /**
     * Check if the functionDeployments contain python functions that were uploaded as zip file.
     *
     * @return true if the functionDeployments contain python functions that were uploaded as zip
     * file, else false
     */
    private boolean hasZippedCode() {
        return functionDeployments.stream().anyMatch(functionDeployment -> {
                Function function = functionDeployment.getFunction();
                boolean isFile = function.getIsFile();
                boolean isPython = RuntimeEnum.fromRuntime(function.getRuntime()).equals(RuntimeEnum.PYTHON38);
                return isFile && isPython;
        });
    }
}
