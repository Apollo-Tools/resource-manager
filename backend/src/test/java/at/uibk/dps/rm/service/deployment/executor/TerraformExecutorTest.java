package at.uibk.dps.rm.service.deployment.executor;

import at.uibk.dps.rm.entity.deployment.DeploymentPath;
import at.uibk.dps.rm.entity.deployment.ProcessOutput;
import at.uibk.dps.rm.testutil.mockprovider.Mockprovider;
import at.uibk.dps.rm.testutil.objectprovider.TestConfigProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestDTOProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestExecutorProvider;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
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
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.when;

/**
 * Implements tests for the {@link TerraformExecutor} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class TerraformExecutorTest {



    private final JsonObject config = TestConfigProvider.getConfig();

    @Mock
    private Vertx vertx;

    @Mock
    private FileSystem fileSystem;

    @Mock
    private Process process;

    @BeforeEach
    void initTest() {
        System.setProperty("os.name", "Linux");
    }

    @Test
    void setPluginCacheFolder(VertxTestContext testContext) {
        DeploymentPath deploymentPath = new DeploymentPath(1L, config);
        Path cacheFolderPath = deploymentPath.getTFCacheFolder();
        Buffer fileContent = Buffer.buffer("plugin_cache_dir = \"" +
            cacheFolderPath.toString().replace("\\", "/") + "\"");
        String configPath = Paths.get("terraform", "config.tfrc").toString();
        MainTerraformExecutor terraformExecutor = TestExecutorProvider.createTerraformExecutorAWSOpenFaas(vertx);
        when(vertx.fileSystem()).thenReturn(fileSystem);
        when(fileSystem.mkdirs(cacheFolderPath.toString())).thenReturn(Completable.complete());
        when(fileSystem.writeFile(configPath, fileContent)).thenReturn(Completable.complete());

        terraformExecutor.setPluginCacheFolder(cacheFolderPath)
            .blockingSubscribe(() -> {
                },
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
        testContext.completeNow();
    }

    private static Stream<Arguments> provideCommands() {
        return Stream.of(
          Arguments.of(List.of("terraform", "init")),
          Arguments.of(List.of("terraform", "apply", "-auto-approve")),
          Arguments.of(List.of("terraform", "output", "--json")),
          Arguments.of(List.of("terraform", "destroy", "-auto-approve"))
        );
    }



    @ParameterizedTest
    @MethodSource("provideCommands")
    void commands(List<String> commands, VertxTestContext testContext) {
        DeploymentPath deploymentPath = new DeploymentPath(1L, config);
        TerraformExecutor terraformExecutor = TestExecutorProvider.createTerraformExecutor(vertx);
        ProcessOutput processOutput = TestDTOProvider.createProcessOutput(process, "output");

        try (MockedConstruction<ProcessExecutor> ignored = Mockprovider.mockProcessExecutor(deploymentPath,
            processOutput, commands)) {
            Single<ProcessOutput> single = Single.just(new ProcessOutput());
            switch (commands.get(1)) {
                case "init":
                    single = terraformExecutor.init(deploymentPath.getRootFolder());
                    break;
                case "apply":
                    single = terraformExecutor.apply(deploymentPath.getRootFolder());
                    break;
                case "output":
                    single = terraformExecutor.getOutput(deploymentPath.getRootFolder());
                    break;
                case "destroy":
                    single = terraformExecutor.destroy(deploymentPath.getRootFolder());
                    break;
            }
            single.subscribe(result -> testContext.verify(() -> {
                        assertThat(result.getOutput()).isEqualTo("output");
                        testContext.completeNow();
                    }),
                    throwable -> testContext.verify(() -> fail("method has thrown exception"))
                );
        }
    }
}
