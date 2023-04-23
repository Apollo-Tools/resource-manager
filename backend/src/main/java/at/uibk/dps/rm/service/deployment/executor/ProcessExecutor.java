package at.uibk.dps.rm.service.deployment.executor;

import at.uibk.dps.rm.entity.deployment.ProcessOutput;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lombok.AllArgsConstructor;

import java.nio.file.Path;
import java.util.List;

/**
 * Execute a cli process in a configurable working directory with a list of commands.
 *
 * @author matthi-g
 */
@AllArgsConstructor
public class ProcessExecutor {

    private final Path workingDirectory;

    private final List<String> commands;

    /**
     * Create an instance from the workingDirectory and commands.
     *
     * @param workingDirectory the working directory of the process
     * @param commands the list of commands to execute
     */
    public ProcessExecutor(Path workingDirectory, String ...commands) {
        this.workingDirectory = workingDirectory;
        this.commands = List.of(commands);
    }

    /**
     * Execute the cli commands as a process.
     *
     * @return a Single that emits the process output of the commands
     */
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
