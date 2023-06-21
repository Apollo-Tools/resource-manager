package at.uibk.dps.rm.service.deployment.docker;

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


@RequiredArgsConstructor
public class LambdaJavaBuildService {
    private final List<FunctionDeployment> functionDeployments;

    private final Path functionsDir;

    public Single<ProcessOutput> buildAndZipJavaFunctions() {
        if (!hasLambdaJavaFunctions()) {
            return Single.just(new ProcessOutput());
        }
        List<String> dockerCommands = new java.util.ArrayList<>(List.of("docker", "run", "-v",
            functionsDir.toAbsolutePath().toString().replace("\\", "/") + ":/projects", "-w",
            "/projects", "gradle:7-jdk-jammy", "/bin/sh", "-c"));
        String dockerInteractiveCommands = "for dir in *_java11;do cd \"$dir\";ls;" +
            "gradle buildLambdaZip; cd ..;done; exit";
        dockerCommands.add(dockerInteractiveCommands);
        ProcessExecutor processExecutor = new ProcessExecutor(functionsDir, dockerCommands);
        return processExecutor.executeCli();
    }

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
