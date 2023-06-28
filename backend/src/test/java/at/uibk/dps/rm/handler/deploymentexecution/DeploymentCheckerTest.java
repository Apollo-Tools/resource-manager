package at.uibk.dps.rm.handler.deploymentexecution;

import at.uibk.dps.rm.entity.deployment.DeploymentCredentials;
import at.uibk.dps.rm.entity.deployment.DeploymentPath;
import at.uibk.dps.rm.entity.deployment.FunctionsToDeploy;
import at.uibk.dps.rm.entity.deployment.ProcessOutput;
import at.uibk.dps.rm.entity.dto.deployment.DeployResourcesDTO;
import at.uibk.dps.rm.entity.dto.deployment.TerminateResourcesDTO;
import at.uibk.dps.rm.exception.DeploymentTerminationFailedException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.service.deployment.docker.OpenFaasImageService;
import at.uibk.dps.rm.service.deployment.executor.MainTerraformExecutor;
import at.uibk.dps.rm.service.deployment.executor.TerraformExecutor;
import at.uibk.dps.rm.service.rxjava3.database.log.DeploymentLogService;
import at.uibk.dps.rm.service.rxjava3.database.log.LogService;
import at.uibk.dps.rm.service.rxjava3.deployment.DeploymentExecutionService;
import at.uibk.dps.rm.testutil.mockprovider.Mockprovider;
import at.uibk.dps.rm.testutil.objectprovider.*;
import at.uibk.dps.rm.util.configuration.ConfigUtility;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.RunTestOnContext;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Implements tests for the {@link DeploymentExecutionChecker} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class DeploymentCheckerTest {

    @RegisterExtension
    private static final RunTestOnContext rtoc = new RunTestOnContext();

    private DeploymentExecutionChecker deploymentChecker;

    private final JsonObject config = TestConfigProvider.getConfig();

    @Mock
    private DeploymentExecutionService deploymentExecutionService;

    @Mock
    private LogService logService;

    @Mock
    private DeploymentLogService deploymentLogService;

    @Mock
    private Process processMainTF;

    @Mock
    private Process processContainer;

    @BeforeEach
    void initTest() {
        rtoc.vertx();
        JsonMapperConfig.configJsonMapper();
        deploymentChecker = new DeploymentExecutionChecker(deploymentExecutionService, logService, deploymentLogService);
    }

    @ParameterizedTest
    @ValueSource(strings={"valid", "outputFailed", "applyFailed", "initFailed", "initContainersFailed",
        "setupTfModulesFailed", "buildDockerFailed", "packageFunctionsCodeFailed"})
    void deployResources(String testCase, VertxTestContext testContext) {
        DeployResourcesDTO deployRequest = TestRequestProvider.createDeployRequest();
        FunctionsToDeploy functionsToDeploy = TestDTOProvider.createFunctionsToDeploy();
        JsonObject log = JsonObject.mapFrom(TestLogProvider.createLog(1L));
        DeploymentCredentials deploymentCredentials = TestDTOProvider.createDeploymentCredentialsAWSOpenfaas();
        DeploymentPath deploymentPath = new DeploymentPath(deployRequest.getDeployment().getDeploymentId(), config);
        ProcessOutput poDocker = TestDTOProvider.createProcessOutput(processMainTF, "docker");
        ProcessOutput poInit = TestDTOProvider.createProcessOutput(processMainTF, "init");
        ProcessOutput poApply = TestDTOProvider.createProcessOutput(processMainTF, "apply");
        ProcessOutput poOutput = TestDTOProvider.createProcessOutput(processMainTF, "output");
        ProcessOutput poContainer = TestDTOProvider.createProcessOutput(processContainer, "container");

        when(deploymentExecutionService.packageFunctionsCode(deployRequest))
            .thenReturn(testCase.equals("packageFunctionsCodeFailed") ? Single.error(IOException::new) :
                Single.just(functionsToDeploy));
        when(deploymentExecutionService.setUpTFModules(deployRequest)).thenReturn(testCase.equals("setupTfModulesFailed") ?
            Single.error(IOException::new) : Single.just(deploymentCredentials));

        if (!testCase.equals("packageFunctionsCodeFailed")) {
            when(logService.save(any())).thenReturn(Single.just(log));
            when(deploymentLogService.save(any())).thenReturn(Single.just(new JsonObject()));
            when(processMainTF.exitValue()).thenReturn(testCase.equals("buildDockerFailed") ? -1 : 0)
                .thenReturn(testCase.equals("initFailed") ? -1 : 0)
                .thenReturn(testCase.equals("applyFailed") ? -1 : 0)
                .thenReturn(testCase.equals("outputFailed") ? -1 : 0);
        }

        if (!testCase.equals("setupTfModulesFailed") && !testCase.equals("buildDockerFailed") &&
                !testCase.equals("packageFunctionsCodeFailed")) {
            when(processContainer.exitValue()).thenReturn(testCase.equals("initContainersFailed") ? -1 : 0);
        }

        try (MockedConstruction<ConfigUtility> ignoredConfig = Mockprovider.mockConfig(config);
             MockedConstruction<OpenFaasImageService> ignoredDocker = Mockprovider
                 .mockDockerImageService(functionsToDeploy, poDocker);
             MockedConstruction<MainTerraformExecutor> ignoredMTFE =
                 Mockprovider.mockMainTerraformExecutor(deploymentPath, poInit, poApply, poOutput);
             MockedConstruction<TerraformExecutor> ignoredTFE =
                 Mockprovider.mockTerraformExecutor(deployRequest, deploymentPath, poContainer, "init")
        ) {
            deploymentChecker.applyResourceDeployment(deployRequest)
                .subscribe(result -> testContext.verify(() -> {
                    if (testCase.equals("valid")) {
                        assertThat(result.getOutput()).isEqualTo("output");
                        assertThat(result.getProcess().exitValue()).isEqualTo(0);
                    } else {
                        fail("method did not throw exception");
                    }
                    testContext.completeNow();
                }), throwable -> testContext.verify(() -> {
                    switch (testCase) {
                        case "valid":
                            fail("method has thrown exception");
                            break;
                        case "outputFailed":
                        case "applyFailed":
                        case "initFailed":
                        case "buildDockerFailed":
                            assertThat(throwable).isInstanceOf(DeploymentTerminationFailedException.class);
                            break;
                        case "setupTfModulesFailed":
                        case "packageFunctionsCodeFailed":
                            assertThat(throwable).isInstanceOf(IOException.class);
                            break;
                    }
                    testContext.completeNow();
                }));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"valid", "destroyFailed", "destroyContainerFailed", "getCredentialsFailed"})
    void terminateResource(String testCase, VertxTestContext testContext) {
        TerminateResourcesDTO terminateRequest = TestRequestProvider.createTerminateRequest();
        DeploymentPath deploymentPath = new DeploymentPath(terminateRequest.getDeployment().getDeploymentId(),
            config);
        DeploymentCredentials deploymentCredentials = TestDTOProvider.createDeploymentCredentialsAWSOpenfaas();
        JsonObject log = JsonObject.mapFrom(TestLogProvider.createLog(1L));
        ProcessOutput poDestroy = TestDTOProvider.createProcessOutput(processMainTF, "destroy");
        ProcessOutput poContainer = TestDTOProvider.createProcessOutput(processContainer, "container");

        when(deploymentExecutionService.getNecessaryCredentials(terminateRequest))
            .thenReturn(testCase.equals("getCredentialsFailed") ? Single.error(NotFoundException::new) :
                Single.just(deploymentCredentials));
        when(logService.save(any())).thenReturn(Single.just(log));
        when(deploymentLogService.save(any())).thenReturn(Single.just(new JsonObject()));
        when(processContainer.exitValue()).thenReturn(testCase.equals("destroyContainerFailed") ? -1 : 0);
        if (!testCase.equals("getCredentialsFailed") && !testCase.equals("destroyContainerFailed")) {
            when(processMainTF.exitValue()).thenReturn(testCase.equals("destroyFailed") ? -1 : 0);
        }

        try (MockedConstruction<ConfigUtility> ignoredConfig = Mockprovider.mockConfig(config);
             MockedConstruction<MainTerraformExecutor> ignoredMTFE = Mockprovider
                 .mockMainTerraformExecutor(deploymentPath, poDestroy);
             MockedConstruction<TerraformExecutor> ignoredTFE = Mockprovider
                 .mockTerraformExecutor(terminateRequest, deploymentPath, poContainer, "destroy")
        ) {
            deploymentChecker.terminateResources(terminateRequest)
                .blockingSubscribe(() -> testContext.verify(() -> {
                        if (!testCase.equals("valid")) {
                            fail("method did not throw exception");
                        }
                        testContext.completeNow();
                    }), throwable -> testContext.verify(() -> {
                    switch (testCase) {
                        case "valid":
                            testContext.verify(() -> fail("method has thrown exception"));
                            break;
                        case "destroyFailed":
                        case "destroyContainerFailed":
                            assertThat(throwable).isInstanceOf(DeploymentTerminationFailedException.class);
                            break;
                        case "getCredentialsFailed":
                            assertThat(throwable).isInstanceOf(NotFoundException.class);
                            break;
                    }
                        testContext.completeNow();
                    })
                );
            testContext.completeNow();
        }
    }

    @Test
    void persistLogsEmptyProcessOutput(VertxTestContext testContext) {
        TerminateResourcesDTO terminateRequest = TestRequestProvider.createTerminateRequest();
        DeploymentPath deploymentPath = new DeploymentPath(terminateRequest.getDeployment().getDeploymentId(),
            config);
        DeploymentCredentials deploymentCredentials = TestDTOProvider.createDeploymentCredentialsAWSOpenfaas();
        ProcessOutput poDestroy = TestDTOProvider.createProcessOutput(null, "destroy");

        when(deploymentExecutionService.getNecessaryCredentials(terminateRequest)).thenReturn(Single.just(deploymentCredentials));

        try (MockedConstruction<ConfigUtility> ignoredConfig = Mockprovider.mockConfig(config);
             MockedConstruction<MainTerraformExecutor> ignoredMTFE =
                 Mockprovider.mockMainTerraformExecutor(deploymentPath, poDestroy);
             MockedConstruction<TerraformExecutor> ignoredTFE =
                 Mockprovider.mockTerraformExecutor(terminateRequest, deploymentPath, poDestroy, "destroy")
        ) {
            deploymentChecker.terminateResources(terminateRequest)
                .blockingSubscribe(() -> {
                    },
                    throwable -> testContext.verify(() -> fail("method has thrown exception"))
                );
            testContext.completeNow();
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void deleteTFDirs(boolean isValid, VertxTestContext testContext) {
        long deploymentId = 1L;

        when(deploymentExecutionService.deleteTFDirs(deploymentId))
            .thenReturn(isValid ? Completable.complete() : Completable.error(IOException::new));

        deploymentChecker.deleteTFDirs(deploymentId)
            .blockingSubscribe(() -> testContext.verify(() -> {
                if (!isValid) {
                    fail("method did not throw exception");
                }
            }), throwable -> testContext.verify(() -> {
                if (isValid) {
                    fail("method has thrown exception");
                } else {
                    assertThat(throwable).isInstanceOf(IOException.class);
                }
                testContext.completeNow();
            }));
        testContext.completeNow();
    }

    private static Stream<Arguments> provideDeployTerminateContainer() {
        return Stream.of(
            Arguments.of("apply", true),
            Arguments.of("apply", false),
            Arguments.of("destroy", true),
            Arguments.of("destroy", false)
        );
    }

    @ParameterizedTest
    @MethodSource("provideDeployTerminateContainer")
    void deployTerminateContainer(String testCase, boolean isValid, VertxTestContext testContext) {
        long deploymentId = 1L, resourceDeploymentId = 2L;
        DeploymentPath deploymentPath = new DeploymentPath(deploymentId, config);
        ProcessOutput processOutput = TestDTOProvider.createProcessOutput(processContainer, testCase);
        JsonObject log = JsonObject.mapFrom(TestLogProvider.createLog(1L));

        when(processContainer.exitValue()).thenReturn(isValid ? 0 : -1);
        when(logService.save(any())).thenReturn(Single.just(log));
        when(deploymentLogService.save(any())).thenReturn(Single.just(new JsonObject()));

        try(MockedConstruction<ConfigUtility> ignoredConfig = Mockprovider.mockConfig(config);
            MockedConstruction<TerraformExecutor> ignoredTFE = Mockprovider.mockTerraformExecutor(deploymentPath,
                resourceDeploymentId, processOutput, testCase)
        ) {
            Completable completable;
            if (testCase.equals("apply")) {
                completable = deploymentChecker.startContainer(deploymentId, resourceDeploymentId);
            } else {
                completable = deploymentChecker.stopContainer(deploymentId, resourceDeploymentId);
            }
            completable.blockingSubscribe(() -> testContext.verify(() -> {
                if (!isValid) {
                    fail("method did not throw exception");
                }
            }), throwable -> testContext.verify(() -> {
                if (isValid) {
                    fail("method has thrown exception");
                } else {
                    assertThat(throwable).isInstanceOf(DeploymentTerminationFailedException.class);
                }
                testContext.completeNow();
            }));
            testContext.completeNow();
        }
    }
}
