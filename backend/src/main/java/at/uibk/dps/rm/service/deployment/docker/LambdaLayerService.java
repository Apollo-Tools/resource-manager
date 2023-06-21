package at.uibk.dps.rm.service.deployment.docker;

import at.uibk.dps.rm.entity.deployment.ProcessOutput;
import at.uibk.dps.rm.entity.dto.resource.RuntimeEnum;
import at.uibk.dps.rm.entity.model.Function;
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
        List<String> dockerCommands = new java.util.ArrayList<>(List.of("docker", "run", "--rm",
            "-v", functionsDir.toAbsolutePath().toString().replace("\\", "/") + "/layers:/var/task",
            "public.ecr.aws/sam/build-python3.8:latest", "/bin/sh", "-c"));
        String dockerInteractiveCommands = "for dir in *_python38;do pip install -r \"$dir\"/requirements.txt " +
            "-t python/lib/python3.8/site-packages/; done;zip -qr ./python38.zip ./python;rm -r ./python; exit";
        dockerCommands.add(dockerInteractiveCommands);
        ProcessExecutor processExecutor = new ProcessExecutor(functionsDir, dockerCommands);
        return processExecutor.executeCli();
    }

    private boolean hasZippedCode() {
        return functionDeployments.stream().anyMatch(functionDeployment -> {
                Function function = functionDeployment.getFunction();
                boolean isFile = function.getIsFile();
                boolean isPython =RuntimeEnum.fromRuntime(function.getRuntime()).equals(RuntimeEnum.PYTHON38);
                return isFile && isPython;
        });
    }
}
