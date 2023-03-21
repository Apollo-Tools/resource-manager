package at.uibk.dps.rm.service.deployment.executor;

import at.uibk.dps.rm.entity.deployment.ProcessOutput;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lombok.AllArgsConstructor;

import java.nio.file.Path;
import java.util.List;

@AllArgsConstructor
public class ProcessExecutor {

    private final Path workingDirectory;

    private final List<String> commands;

    public ProcessExecutor(Path workingDirectory, String ...commands) {
        this.workingDirectory = workingDirectory;
        this.commands = List.of(commands);
    }

    public Single<ProcessOutput> executeCli() {
        ProcessBuilder processBuilder = new ProcessBuilder(commands)
            .directory(workingDirectory.toFile())
            .redirectErrorStream(true);
        return Single.fromCallable(new CliOutputProvider(processBuilder))
            .subscribeOn(Schedulers.io())
            .flatMap(processOutput ->
                Single.fromCompletionStage(processOutput.getProcess().onExit().minimalCompletionStage())
                    .map(process -> processOutput));
    }
}
