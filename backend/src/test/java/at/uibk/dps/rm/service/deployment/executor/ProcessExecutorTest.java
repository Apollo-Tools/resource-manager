package at.uibk.dps.rm.service.deployment.executor;

import at.uibk.dps.rm.testutil.mockprovider.ProcessExecutorMockprovider;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * Implements tests for the {@link ProcessExecutor} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class ProcessExecutorTest {

    @Test
    void executeCli(VertxTestContext testContext) {
        Path workingDir = Paths.get(".");
        String[] command = {"bash", "-c", "echo 'hello test'"};
        ProcessExecutor processExecutor = new ProcessExecutor(workingDir, command);

        processExecutor.executeCli()
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.getProcess().exitValue()).isEqualTo(0);
                    assertThat(result.getOutput()).isEqualTo("hello test");
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void executeCliException(VertxTestContext testContext) {
        Path workingDir = Paths.get(".");
        String[] command = {"bash", "-c", "echo 'hello test'"};
        ProcessExecutor processExecutor = new ProcessExecutor(workingDir, command);

        try (MockedConstruction<ProcessBuilder> ignored = ProcessExecutorMockprovider.mockProcessBuilderIOException(workingDir)) {
            processExecutor.executeCli()
                .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                    throwable -> testContext.verify(() -> {
                        assertThat(throwable).isInstanceOf(RuntimeException.class);
                        testContext.completeNow();
                    }));
        }
    }
}
