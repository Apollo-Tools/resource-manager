package at.uibk.dps.rm.service.deployment.docker;

import at.uibk.dps.rm.entity.deployment.ProcessOutput;
import at.uibk.dps.rm.entity.dto.credentials.DockerCredentials;
import at.uibk.dps.rm.service.deployment.executor.ProcessExecutor;
import at.uibk.dps.rm.testutil.mockprovider.Mockprovider;
import at.uibk.dps.rm.testutil.objectprovider.TestConfigProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestDTOProvider;
import at.uibk.dps.rm.util.configuration.ConfigUtility;
import io.reactivex.rxjava3.core.Completable;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.core.buffer.Buffer;
import io.vertx.rxjava3.core.file.FileSystem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.when;

/**
 * Implements tests for the {@link OpenFaasImageService} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class DockerImageServiceTest {

    private OpenFaasImageService dockerImageService;

    private final JsonObject config = TestConfigProvider.getConfig();

    @Mock
    private Vertx vertx;

    private Path functionsDir;

    @Mock
    private FileSystem fileSystem;

    @Mock
    private ProcessOutput processOutput;

    @Mock
    private Process process;

    @BeforeEach
    void initTest() {
        DockerCredentials dockerCredentials = TestDTOProvider.createDockerCredentials();
        List<String> functionIdentifiers = List.of("func_identifier");
        functionsDir = Path.of("./functions");
        dockerImageService = new OpenFaasImageService(vertx, dockerCredentials, functionIdentifiers, functionsDir);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    void buildOpenFaasImages(int exitValue, VertxTestContext testContext) {
        String functionString = "functions";

        when(vertx.fileSystem()).thenReturn(fileSystem);
        when(fileSystem.writeFile(eq(Path.of(functionsDir.toString(), "stack.yml").toString()), any(Buffer.class)))
            .thenReturn(Completable.complete());
        when(processOutput.getProcess()).thenReturn(process);
        doCallRealMethod().when(processOutput).setOutput(any());
        when(processOutput.getOutput())
            .thenReturn("build output\n")
            .thenReturn("push output\n")
            .thenCallRealMethod();
        when(process.exitValue()).thenReturn(0).thenReturn(exitValue);

        try (MockedConstruction<ConfigUtility> ignoredConfig = Mockprovider.mockConfig(config);
             MockedConstruction<ProcessExecutor> ignored = Mockprovider.mockProcessExecutor(processOutput)) {
                dockerImageService.buildOpenFaasImages(functionString)
                    .subscribe(result -> testContext.verify(() -> {
                            assertThat(result.getOutput()).isEqualTo("build output\npush output\n");
                            assertThat(result.getProcess().exitValue()).isEqualTo(exitValue);
                            testContext.completeNow();
                        }),
                        throwable -> testContext.verify(() -> fail("method has thrown exception"))
                    );
        }
    }

    @Test
    void buildOpenFaasImagesBuildFailed(VertxTestContext testContext) {
        String functionString = "functions";

        when(vertx.fileSystem()).thenReturn(fileSystem);
        when(fileSystem.writeFile(eq(Path.of(functionsDir.toString(), "stack.yml").toString()), any(Buffer.class)))
            .thenReturn(Completable.complete());
        when(processOutput.getProcess()).thenReturn(process);
        when(processOutput.getOutput())
            .thenReturn("build output\n");
        when(process.exitValue()).thenReturn(-1);

        try (MockedConstruction<ConfigUtility> ignoredConfig = Mockprovider.mockConfig(config);
             MockedConstruction<ProcessExecutor> ignored = Mockprovider.mockProcessExecutor(processOutput)) {
                dockerImageService.buildOpenFaasImages(functionString)
                    .subscribe(result -> testContext.verify(() -> {
                            assertThat(result.getOutput()).isEqualTo("build output\n");
                            assertThat(result.getProcess().exitValue()).isEqualTo(-1);
                            testContext.completeNow();
                        }),
                        throwable -> testContext.verify(() -> fail("method has thrown exception"))
                    );
        }
    }

    @Test
    void buildOpenFaasImagesCreateStackFileFailed(VertxTestContext testContext) {
        String functionString = "functions";

        when(vertx.fileSystem()).thenReturn(fileSystem);
        when(fileSystem.writeFile(eq(Path.of(functionsDir.toString(), "stack.yml").toString()), any(Buffer.class)))
            .thenReturn(Completable.error(IOException::new));

        try(MockedConstruction<ProcessExecutor> ignored = Mockprovider.mockProcessExecutor(processOutput)) {
            dockerImageService.buildOpenFaasImages(functionString)
                .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                    throwable -> testContext.verify(() -> {
                        assertThat(throwable).isInstanceOf(IOException.class);
                        testContext.completeNow();
                    })
                );
        }
    }

    @Test
    void buildOpenFaasImagesEmptyFunctionIdentifiers(VertxTestContext testContext) {
        String functionString = "";
        List<String> functionIdentifiers = List.of();
        functionsDir = Path.of("./functions");
        DockerCredentials dockerCredentials = TestDTOProvider.createDockerCredentials();
        dockerImageService = new OpenFaasImageService(vertx, dockerCredentials, functionIdentifiers, functionsDir);

        dockerImageService.buildOpenFaasImages(functionString)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.getOutput()).isNull();
                    assertThat(result.getProcess()).isNull();
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }
}
