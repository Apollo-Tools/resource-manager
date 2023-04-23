package at.uibk.dps.rm.service.deployment.executor;

import at.uibk.dps.rm.entity.deployment.ProcessOutput;
import lombok.AllArgsConstructor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 * This class can be used to execute cli processes and provide the output of the process to the
 * caller.
 *
 * @author matthi-g
 */
@AllArgsConstructor
public class CliOutputProvider implements Callable<ProcessOutput> {

    private final ProcessBuilder processBuilder;

    /**
     * Execute the process and return the output after termination.
     *
     * @return the output of the terminated process
     */
    @Override
    public ProcessOutput call() {
        ProcessOutput processOutput = new ProcessOutput();
        try {
            final Process process = processBuilder.start();
            processOutput.setProcess(process);
            processOutput.setOutput(getProcessOutput(process));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return processOutput;
    }

    /**
     * Get the output of a process.
     *
     * @param process the process
     * @return the output of the process
     * @throws IOException while reading the InputStream of the process
     */
    private String getProcessOutput(Process process) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        return reader.lines().collect(Collectors.joining("\n"));
    }
}
