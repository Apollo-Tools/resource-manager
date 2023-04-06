package at.uibk.dps.rm.service.deployment.executor;

import at.uibk.dps.rm.entity.deployment.DeploymentPath;
import at.uibk.dps.rm.entity.deployment.ProcessOutput;
import at.uibk.dps.rm.testutil.objectprovider.TestDTOProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestExecutorProvider;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.core.buffer.Buffer;
import io.vertx.rxjava3.core.file.FileSystem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class TerraformExecutorTest {

    @Mock
    private Vertx vertx;

    @Mock
    private FileSystem fileSystem;

    @Mock
    private Process process;

    @ParameterizedTest
    @CsvSource({
        "Windows, APPDATA, \\terraform.rc",
        "Linux, user.home, \\.terraformrc",
    })
    void setPluginCacheFolder(String os, String homePathName, String configFileName, VertxTestContext testContext) {
        DeploymentPath deploymentPath = new DeploymentPath(1L);
        Path cacheFolderPath = deploymentPath.getTFCacheFolder();
        Buffer fileContent = Buffer.buffer("plugin_cache_dir = \"" +
            cacheFolderPath.toString().replace("\\", "/") + "\"");
        System.setProperty("os.name", os);
        String configPath = Paths.get(System.getenv(homePathName) + configFileName).toString();
        TerraformExecutor terraformExecutor = TestExecutorProvider.createTerraformExecutorAWSEdge(vertx);
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

    @Test
    void init(VertxTestContext testContext) {
        DeploymentPath deploymentPath = new DeploymentPath(1L);
        TerraformExecutor terraformExecutor = TestExecutorProvider.createTerraformExecutorAWSEdge(vertx);
        ProcessOutput processOutput = TestDTOProvider.createProcessOutput(process, "output");

        try (MockedConstruction<ProcessExecutor> ignored = Mockito.mockConstruction(ProcessExecutor.class,
            (mock, context) -> {
                given(mock.executeCli()).willReturn(Single.just(processOutput));
                assertThat(context.arguments().get(1)).isEqualTo(new String[]{"terraform", "init"});
                assertThat(context.arguments().get(0)).isEqualTo(deploymentPath.getRootFolder());
            })) {
            terraformExecutor.init(deploymentPath.getRootFolder())
                .subscribe(result -> testContext.verify(() -> {
                        assertThat(result.getProcessOutput()).isEqualTo("output");
                        testContext.completeNow();
                    }),
                    throwable -> testContext.verify(() -> fail("method has thrown exception"))
                );
        }
    }

    @Test
    void apply(VertxTestContext testContext) {
        DeploymentPath deploymentPath = new DeploymentPath(1L);
        TerraformExecutor terraformExecutor = TestExecutorProvider.createTerraformExecutorAWSEdge(vertx);
        ProcessOutput processOutput = TestDTOProvider.createProcessOutput(process, "output");
        List<String> commands = TestExecutorProvider.tfCommandsWithCredsAWSEdge("apply");

        try (MockedConstruction<ProcessExecutor> ignored = Mockito.mockConstruction(ProcessExecutor.class,
            (mock, context) -> {
                given(mock.executeCli()).willReturn(Single.just(processOutput));
                assertThat(context.arguments().get(1)).isEqualTo(commands);
                assertThat(context.arguments().get(0)).isEqualTo(deploymentPath.getRootFolder());
            })) {
            terraformExecutor.apply(deploymentPath.getRootFolder())
                .subscribe(result -> testContext.verify(() -> {
                        assertThat(result.getProcessOutput()).isEqualTo("output");
                        testContext.completeNow();
                    }),
                    throwable -> testContext.verify(() -> fail("method has thrown exception"))
                );
        }
    }

    @Test
    void getOutput(VertxTestContext testContext) {
        DeploymentPath deploymentPath = new DeploymentPath(1L);
        TerraformExecutor terraformExecutor = TestExecutorProvider.createTerraformExecutorAWSEdge(vertx);
        ProcessOutput processOutput = TestDTOProvider.createProcessOutput(process, "output");
        List<String> commands = List.of("terraform", "output", "--json");

        try (MockedConstruction<ProcessExecutor> ignored = Mockito.mockConstruction(ProcessExecutor.class,
            (mock, context) -> {
                given(mock.executeCli()).willReturn(Single.just(processOutput));
                assertThat(context.arguments().get(1)).isEqualTo(commands);
                assertThat(context.arguments().get(0)).isEqualTo(deploymentPath.getRootFolder());
            })) {
            terraformExecutor.getOutput(deploymentPath.getRootFolder())
                .subscribe(result -> testContext.verify(() -> {
                        assertThat(result.getProcessOutput()).isEqualTo("output");
                        testContext.completeNow();
                    }),
                    throwable -> testContext.verify(() -> fail("method has thrown exception"))
                );
        }
    }

    @Test
    void destroy(VertxTestContext testContext) {
        DeploymentPath deploymentPath = new DeploymentPath(1L);
        TerraformExecutor terraformExecutor = TestExecutorProvider.createTerraformExecutorAWSEdge(vertx);
        ProcessOutput processOutput = TestDTOProvider.createProcessOutput(process, "output");
        List<String> commands = TestExecutorProvider.tfCommandsWithCredsAWSEdge("destroy");

        try (MockedConstruction<ProcessExecutor> ignored = Mockito.mockConstruction(ProcessExecutor.class,
            (mock, context) -> {
                given(mock.executeCli()).willReturn(Single.just(processOutput));
                assertThat(context.arguments().get(1)).isEqualTo(commands);
                assertThat(context.arguments().get(0)).isEqualTo(deploymentPath.getRootFolder());
            })) {
            terraformExecutor.destroy(deploymentPath.getRootFolder())
                .subscribe(result -> testContext.verify(() -> {
                        assertThat(result.getProcessOutput()).isEqualTo("output");
                        testContext.completeNow();
                    }),
                    throwable -> testContext.verify(() -> fail("method has thrown exception"))
                );
        }
    }

    @Test
    void destroyNoEdgeCredentials(VertxTestContext testContext) {
        DeploymentPath deploymentPath = new DeploymentPath(1L);
        TerraformExecutor terraformExecutor = TestExecutorProvider.createTerraformExecutorAWS(vertx);
        ProcessOutput processOutput = TestDTOProvider.createProcessOutput(process, "output");
        List<String> commands = TestExecutorProvider.tfCommandsWithCredsAWS("destroy");

        try (MockedConstruction<ProcessExecutor> ignored = Mockito.mockConstruction(ProcessExecutor.class,
            (mock, context) -> {
                given(mock.executeCli()).willReturn(Single.just(processOutput));
                assertThat(context.arguments().get(1)).isEqualTo(commands);
                assertThat(context.arguments().get(0)).isEqualTo(deploymentPath.getRootFolder());
            })) {
            terraformExecutor.destroy(deploymentPath.getRootFolder())
                .subscribe(result -> testContext.verify(() -> {
                        assertThat(result.getProcessOutput()).isEqualTo("output");
                        testContext.completeNow();
                    }),
                    throwable -> testContext.verify(() -> fail("method has thrown exception"))
                );
        }
    }
}