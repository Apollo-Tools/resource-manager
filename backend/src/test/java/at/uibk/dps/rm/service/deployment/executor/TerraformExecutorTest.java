package at.uibk.dps.rm.service.deployment.executor;

import at.uibk.dps.rm.entity.deployment.DeploymentPath;
import at.uibk.dps.rm.entity.deployment.ProcessOutput;
import at.uibk.dps.rm.entity.dto.config.ConfigDTO;
import at.uibk.dps.rm.testutil.mockprovider.ProcessExecutorMockprovider;
import at.uibk.dps.rm.testutil.objectprovider.TestConfigProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestDTOProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestExecutorProvider;
import io.reactivex.rxjava3.core.Single;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * Implements tests for the {@link TerraformExecutor} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class TerraformExecutorTest {

    private final ConfigDTO config = TestConfigProvider.getConfigDTO();

    @Mock
    private Process process;

    @BeforeEach
    void initTest() {
        System.setProperty("os.name", "Linux");
    }

    private static Stream<Arguments> provideCommands() {
        return Stream.of(
          Arguments.of(List.of("terraform", "init")),
          Arguments.of(List.of("terraform", "apply", "-auto-approve")),
          Arguments.of(List.of("terraform", "apply", "-refresh-only", "-auto-approve")),
          Arguments.of(List.of("terraform", "output", "--json")),
          Arguments.of(List.of("terraform", "destroy", "-auto-approve"))
        );
    }

    @ParameterizedTest
    @MethodSource("provideCommands")
    void commands(List<String> commands, VertxTestContext testContext) {
        DeploymentPath deploymentPath = new DeploymentPath(1L, config);
        TerraformExecutor terraformExecutor = TestExecutorProvider.createTerraformExecutor();
        ProcessOutput processOutput = TestDTOProvider.createProcessOutput(process, "output");

        try (MockedConstruction<ProcessExecutor> ignored = ProcessExecutorMockprovider
                .mockProcessExecutor(deploymentPath.getRootFolder(), processOutput, commands)) {
            Single<ProcessOutput> single = Single.just(new ProcessOutput());
            switch (commands.get(1)) {
                case "init":
                    single = terraformExecutor.init(deploymentPath.getRootFolder());
                    break;
                case "apply":
                    if (commands.contains("-refresh-only"))
                        single = terraformExecutor.refresh(deploymentPath.getRootFolder());
                    else
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

    private static Stream<Arguments> provideCommandsWithTargets() {
        return Stream.of(
          Arguments.of(List.of("terraform", "apply", "-auto-approve", "-target", "t1", "-target", "t2")),
          Arguments.of(List.of("terraform", "apply", "-refresh-only", "-auto-approve", "-target", "t1", "-target", "t2")),
          Arguments.of(List.of("terraform", "destroy", "-auto-approve", "-target", "t1", "-target", "t2"))
        );
    }

    @ParameterizedTest
    @MethodSource("provideCommandsWithTargets")
    void commandsWithTargets(List<String> commands, VertxTestContext testContext) {
        List<String> targets = List.of("t1", "t2");
        DeploymentPath deploymentPath = new DeploymentPath(1L, config);
        TerraformExecutor terraformExecutor = TestExecutorProvider.createTerraformExecutor();
        ProcessOutput processOutput = TestDTOProvider.createProcessOutput(process, "output");

        try (MockedConstruction<ProcessExecutor> ignored = ProcessExecutorMockprovider
                .mockProcessExecutor(deploymentPath.getRootFolder(), processOutput, commands)) {
            Single<ProcessOutput> single = Single.just(new ProcessOutput());
            switch (commands.get(1)) {
                case "apply":
                    if (commands.contains("-refresh-only"))
                        single = terraformExecutor.refresh(deploymentPath.getRootFolder(), targets);
                    else
                        single = terraformExecutor.apply(deploymentPath.getRootFolder(), targets);
                    break;
                case "destroy":
                    single = terraformExecutor.destroy(deploymentPath.getRootFolder(), targets);
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
