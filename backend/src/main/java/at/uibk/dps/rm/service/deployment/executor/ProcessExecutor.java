package at.uibk.dps.rm.service.deployment.executor;

import at.uibk.dps.rm.entity.deployment.ProcessOutput;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.core.Vertx;
import lombok.AllArgsConstructor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@AllArgsConstructor
public class ProcessExecutor {

    private final Vertx vertx;

    private final Path workingDirectory;

    private final List<String> commands;

    public ProcessExecutor(Vertx vertx, Path workingDirectory, String ...commands) {
        this.vertx = vertx;
        this.workingDirectory = workingDirectory;
        this.commands = List.of(commands);
    }

    public Single<ProcessOutput> executeCli() {
        ExecutorService pool = Executors.newSingleThreadExecutor();
        ProcessBuilder processBuilder = new ProcessBuilder(commands)
            .directory(workingDirectory.toFile())
            .inheritIO()
            .redirectErrorStream(true);
        return Single.fromFuture(pool.submit(new CliOutputProvider(processBuilder)))
            .flatMap(processOutput ->
                Single.fromCompletionStage(processOutput.getProcess().onExit().minimalCompletionStage())
                    .map(process -> processOutput));
            //.subscribeOn(Schedulers.io());
    }

    public Single<ProcessOutput> executeCliDep() {
        Maybe<ProcessOutput> result = vertx.executeBlocking(fut -> {
            ProcessOutput processOutput = new ProcessOutput();
            try {
                final Process process;
                process = new ProcessBuilder(commands)
                    .directory(workingDirectory.toFile())
                    .redirectErrorStream(true)
                    .start();
                processOutput.setProcess(process);
                processOutput.setProcessOutput(getProcessOutput(process));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            fut.complete(processOutput);
        });
        return result.flatMapSingle(processOutput ->
                Single.fromCompletionStage(processOutput.getProcess().onExit().minimalCompletionStage())
                    .map(process -> processOutput))
            .toSingle();
    }

    private String getProcessOutput(Process process) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String result = reader.lines().collect(Collectors.joining("\n"));
        reader.close();
        return result;
    }
}
