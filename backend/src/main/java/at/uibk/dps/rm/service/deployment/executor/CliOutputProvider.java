package at.uibk.dps.rm.service.deployment.executor;

import at.uibk.dps.rm.entity.deployment.ProcessOutput;
import lombok.AllArgsConstructor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

@AllArgsConstructor
public class CliOutputProvider implements Callable<ProcessOutput> {

    private final ProcessBuilder processBuilder;

    @Override
    public ProcessOutput call() {
        ProcessOutput processOutput = new ProcessOutput();
        try {
            final Process process = processBuilder.start();
            processOutput.setProcess(process);
            processOutput.setProcessOutput(getProcessOutput(process));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return processOutput;
    }

    private String getProcessOutput(Process process) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        new String(process.getInputStream().readAllBytes());
        String result = reader.lines().collect(Collectors.joining("\n"));
        reader.close();
        return result;
    }
}
