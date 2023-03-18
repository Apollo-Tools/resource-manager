package at.uibk.dps.rm.service.deployment.executor;

import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.core.Vertx;
import lombok.AllArgsConstructor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.List;

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

    public Single<Integer> executeCli() {
        Maybe<Process> result = vertx.executeBlocking(fut -> {
            final Process process;
            try {
                process = new ProcessBuilder(commands)
                    .directory(workingDirectory.toFile())
                    .inheritIO()
                    .redirectErrorStream(true)
                    .start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            fut.complete(process);
        });
        return result.flatMapSingle(process -> Single.fromCompletionStage(process.onExit().minimalCompletionStage()))
            .map(Process::exitValue)
            .toSingle();
    }

    private void printOutput(Process process) {
        BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
        input.lines().forEach(System.out::println);
    }
}
