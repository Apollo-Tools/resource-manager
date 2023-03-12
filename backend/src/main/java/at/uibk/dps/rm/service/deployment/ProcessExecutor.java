package at.uibk.dps.rm.service.deployment;

import io.reactivex.rxjava3.core.Single;
import lombok.AllArgsConstructor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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

    public Single<Process> executeCli() throws IOException {
        final Process process = new ProcessBuilder(commands)
            .directory(workingDirectory.toFile())
            .inheritIO()
            .redirectErrorStream(true)
            .start();
        //printOutput(process);
        return Single.fromCompletionStage(process.onExit().minimalCompletionStage());
    }

    private void printOutput(Process process) {
        BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
        input.lines().forEach(System.out::println);
    }
}
