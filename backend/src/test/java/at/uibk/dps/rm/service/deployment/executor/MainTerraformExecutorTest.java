package at.uibk.dps.rm.service.deployment.executor;

import at.uibk.dps.rm.entity.deployment.DeploymentPath;
import at.uibk.dps.rm.entity.deployment.ProcessOutput;
import at.uibk.dps.rm.entity.dto.config.ConfigDTO;
import at.uibk.dps.rm.testutil.mockprovider.ProcessExecutorMockprovider;
import at.uibk.dps.rm.testutil.objectprovider.TestConfigProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestDTOProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestExecutorProvider;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
/**
 * Implements tests for the {@link MainTerraformExecutor} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class MainTerraformExecutorTest {

    private final ConfigDTO config = TestConfigProvider.getConfigDTO();

    @Mock
    private Process process;

    @BeforeEach
    void initTest() {
        System.setProperty("os.name", "Linux");
    }

    @Test
    void apply(VertxTestContext testContext) {
        DeploymentPath deploymentPath = new DeploymentPath(1L, config);
        MainTerraformExecutor terraformExecutor = TestExecutorProvider.createTerraformExecutorAWSOpenFaas();
        ProcessOutput processOutput = TestDTOProvider.createProcessOutput(process, "output");
        List<String> commands = TestExecutorProvider.tfCommandsWithCredsAWSOpenFaas("apply");

        try (MockedConstruction<ProcessExecutor> ignored = ProcessExecutorMockprovider
                .mockProcessExecutor(deploymentPath.getRootFolder(), processOutput, commands)) {
            terraformExecutor.apply(deploymentPath.getRootFolder())
                .subscribe(result -> testContext.verify(() -> {
                        assertThat(result.getOutput()).isEqualTo("output");
                        testContext.completeNow();
                    }),
                    throwable -> testContext.verify(() -> fail("method has thrown exception"))
                );
        }
    }

    @Test
    void applyWithTargets(VertxTestContext testContext) {
        List<String> targets = List.of("t1", "t2");
        DeploymentPath deploymentPath = new DeploymentPath(1L, config);
        MainTerraformExecutor terraformExecutor = TestExecutorProvider.createTerraformExecutorAWSOpenFaas();
        ProcessOutput processOutput = TestDTOProvider.createProcessOutput(process, "output");
        List<String> commands = TestExecutorProvider.tfCommandsWithCredsAWSOpenFaas("apply", targets);

        try (MockedConstruction<ProcessExecutor> ignored = ProcessExecutorMockprovider
                .mockProcessExecutor(deploymentPath.getRootFolder(), processOutput, commands)) {
            terraformExecutor.apply(deploymentPath.getRootFolder(), targets)
                .subscribe(result -> testContext.verify(() -> {
                        assertThat(result.getOutput()).isEqualTo("output");
                        testContext.completeNow();
                    }),
                    throwable -> testContext.verify(() -> fail("method has thrown exception"))
                );
        }
    }

    @Test
    void getOutput(VertxTestContext testContext) {
        DeploymentPath deploymentPath = new DeploymentPath(1L, config);
        MainTerraformExecutor terraformExecutor = TestExecutorProvider.createTerraformExecutorAWSOpenFaas();
        ProcessOutput processOutput = TestDTOProvider.createProcessOutput(process, "output");
        List<String> commands = List.of("terraform", "output", "--json");

        try (MockedConstruction<ProcessExecutor> ignored = ProcessExecutorMockprovider
                .mockProcessExecutor(deploymentPath.getRootFolder(), processOutput, commands)) {
            terraformExecutor.getOutput(deploymentPath.getRootFolder())
                .subscribe(result -> testContext.verify(() -> {
                        assertThat(result.getOutput()).isEqualTo("output");
                        testContext.completeNow();
                    }),
                    throwable -> testContext.verify(() -> fail("method has thrown exception"))
                );
        }
    }

    @Test
    void destroy(VertxTestContext testContext) {
        DeploymentPath deploymentPath = new DeploymentPath(1L, config);
        MainTerraformExecutor terraformExecutor = TestExecutorProvider.createTerraformExecutorAWSOpenFaas();
        ProcessOutput processOutput = TestDTOProvider.createProcessOutput(process, "output");
        List<String> commands = TestExecutorProvider.tfCommandsWithCredsAWSOpenFaas("destroy");

        try (MockedConstruction<ProcessExecutor> ignored = ProcessExecutorMockprovider
                .mockProcessExecutor(deploymentPath.getRootFolder(), processOutput, commands)) {
            terraformExecutor.destroy(deploymentPath.getRootFolder())
                .subscribe(result -> testContext.verify(() -> {
                        assertThat(result.getOutput()).isEqualTo("output");
                        testContext.completeNow();
                    }),
                    throwable -> testContext.verify(() -> fail("method has thrown exception"))
                );
        }
    }

    @Test
    void destroyWithTargets(VertxTestContext testContext) {
        List<String> targets = List.of("t1", "t2");
        DeploymentPath deploymentPath = new DeploymentPath(1L, config);
        MainTerraformExecutor terraformExecutor = TestExecutorProvider.createTerraformExecutorAWSOpenFaas();
        ProcessOutput processOutput = TestDTOProvider.createProcessOutput(process, "output");
        List<String> commands = TestExecutorProvider.tfCommandsWithCredsAWSOpenFaas("destroy", targets);

        try (MockedConstruction<ProcessExecutor> ignored = ProcessExecutorMockprovider
                .mockProcessExecutor(deploymentPath.getRootFolder(), processOutput, commands)) {
            terraformExecutor.destroy(deploymentPath.getRootFolder(), targets)
                .subscribe(result -> testContext.verify(() -> {
                        assertThat(result.getOutput()).isEqualTo("output");
                        testContext.completeNow();
                    }),
                    throwable -> testContext.verify(() -> fail("method has thrown exception"))
                );
        }
    }

    @Test
    void destroyNoEdgeCredentials(VertxTestContext testContext) {
        DeploymentPath deploymentPath = new DeploymentPath(1L, config);
        MainTerraformExecutor terraformExecutor = TestExecutorProvider.createTerraformExecutorAWS();
        ProcessOutput processOutput = TestDTOProvider.createProcessOutput(process, "output");
        List<String> commands = TestExecutorProvider.tfCommandsWithCredsAWS("destroy");

        try (MockedConstruction<ProcessExecutor> ignored = ProcessExecutorMockprovider
                .mockProcessExecutor(deploymentPath.getRootFolder(), processOutput, commands)) {
            terraformExecutor.destroy(deploymentPath.getRootFolder())
                .subscribe(result -> testContext.verify(() -> {
                        assertThat(result.getOutput()).isEqualTo("output");
                        testContext.completeNow();
                    }),
                    throwable -> testContext.verify(() -> fail("method has thrown exception"))
                );
        }
    }

    @Test
    void refresh(VertxTestContext testContext) {
        DeploymentPath deploymentPath = new DeploymentPath(1L, config);
        MainTerraformExecutor terraformExecutor = TestExecutorProvider.createTerraformExecutorAWSOpenFaas();
        ProcessOutput processOutput = TestDTOProvider.createProcessOutput(process, "output");
        List<String> commands = TestExecutorProvider.tfCommandsWithCredsAWSOpenFaas("-refresh-only");

        try (MockedConstruction<ProcessExecutor> ignored = ProcessExecutorMockprovider
            .mockProcessExecutor(deploymentPath.getRootFolder(), processOutput, commands)) {
            terraformExecutor.refresh(deploymentPath.getRootFolder())
                .subscribe(result -> testContext.verify(() -> {
                        assertThat(result.getOutput()).isEqualTo("output");
                        testContext.completeNow();
                    }),
                    throwable -> testContext.verify(() -> fail("method has thrown exception"))
                );
        }
    }

    @ParameterizedTest
    @CsvSource({
        "Windows, \"\"",
        "Linux, \"\"",
    })
    void getOpenFaasCredentialsCommand(String os) {
        System.setProperty("os.name", os);
        MainTerraformExecutor terraformExecutor = TestExecutorProvider.createTerraformExecutorAWSOpenFaas();
        String expectedOutput = os.equals("Windows") ? "-var=\"openfaas_login_data={r1={auth_user=\\\"user\\\", " +
            "auth_pw=\\\"pw\\\"}}\"" : "-var=openfaas_login_data={r1={auth_user=\"user\", auth_pw=\"pw\"}}";

        String result = terraformExecutor.getOpenFaasCredentialsCommand();

        assertThat(result).isEqualTo(expectedOutput);
    }
}
