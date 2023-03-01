package at.uibk.dps.rm.service.deployment;

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

    public int executeCli() throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(commands);
        processBuilder.directory(workingDirectory.toFile());
        processBuilder.redirectErrorStream(true);
        final Process process = processBuilder.start();
        Thread thread = printOutput(process);
        thread.start();
        process.waitFor();
        process.destroy();
        return process.exitValue();
    }

    private Thread printOutput(Process process) {
        return new Thread(() -> {
            BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
            input.lines().forEach(System.out::println);
        });
    }
}
