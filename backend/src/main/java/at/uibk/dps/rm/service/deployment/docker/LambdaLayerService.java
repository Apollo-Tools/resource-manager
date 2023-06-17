package at.uibk.dps.rm.service.deployment.docker;

import at.uibk.dps.rm.entity.deployment.ProcessOutput;
import at.uibk.dps.rm.entity.model.FunctionDeployment;
import at.uibk.dps.rm.service.deployment.executor.ProcessExecutor;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;

import java.nio.file.Path;
import java.util.List;

@RequiredArgsConstructor
public class LambdaLayerService {
    private final List<FunctionDeployment> functionDeployments;

    private final Path functionsDir;

    public Single<ProcessOutput> buildLambdaLayers() {
        if (!hasZippedCode()) {
            return Single.just(new ProcessOutput());
        }
        List<String> dockerCommands = new java.util.ArrayList<>(List.of("docker", "run", "-v",
            functionsDir.toAbsolutePath().toString().replace("\\", "/") + "/layers:/var/task",
            "public.ecr.aws/sam/build-python3.8:latest", "/bin/sh", "-c"));
        String dockerInteractiveCommands = "for dir in *_python38;do pip install -r \"$dir\"/requirements.txt " +
            "-t python/lib/python3.8/site-packages/; done;zip -qr ./python38.zip ./python;rm -r ./python; exit";
        dockerCommands.add(dockerInteractiveCommands);
        ProcessExecutor processExecutor = new ProcessExecutor(functionsDir, dockerCommands);
        return processExecutor.executeCli();
    }

    private boolean hasZippedCode() {
        return functionDeployments.stream().anyMatch(functionDeployment -> functionDeployment.getFunction().getIsFile());
    }
}
