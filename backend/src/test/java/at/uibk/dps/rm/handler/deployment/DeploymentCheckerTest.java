package at.uibk.dps.rm.handler.deployment;

import at.uibk.dps.rm.entity.deployment.DeploymentCredentials;
import at.uibk.dps.rm.entity.deployment.DeploymentPath;
import at.uibk.dps.rm.entity.deployment.FunctionsToDeploy;
import at.uibk.dps.rm.entity.deployment.ProcessOutput;
import at.uibk.dps.rm.entity.dto.DeployResourcesRequest;
import at.uibk.dps.rm.entity.dto.TerminateResourcesRequest;
import at.uibk.dps.rm.exception.DeploymentTerminationFailedException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.service.deployment.docker.DockerImageService;
import at.uibk.dps.rm.service.deployment.executor.MainTerraformExecutor;
import at.uibk.dps.rm.service.deployment.executor.TerraformExecutor;
import at.uibk.dps.rm.service.rxjava3.database.log.LogService;
import at.uibk.dps.rm.service.rxjava3.database.log.ReservationLogService;
import at.uibk.dps.rm.service.rxjava3.deployment.DeploymentService;
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
 * Implements tests for the {@link DeploymentChecker} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class DeploymentCheckerTest {

    @RegisterExtension
    private static final RunTestOnContext rtoc = new RunTestOnContext();

    private DeploymentChecker deploymentChecker;

    private final JsonObject config = TestConfigProvider.getConfig();

    @Mock
    private DeploymentService deploymentService;

    @Mock
    private LogService logService;

    @Mock
    private ReservationLogService reservationLogService;

    @Mock
    private Process processMainTF;

    @Mock
    private Process processContainer;

    @BeforeEach
    void initTest() {
        rtoc.vertx();
        JsonMapperConfig.configJsonMapper();
        deploymentChecker = new DeploymentChecker(deploymentService, logService, reservationLogService);
    }

    @ParameterizedTest
    @ValueSource(strings={"valid", "outputFailed", "applyFailed", "initFailed", "initContainersFailed",
        "setupTfModulesFailed", "buildDockerFailed", "packageFunctionsCodeFailed"})
    void deployResources(String testCase, VertxTestContext testContext) {
        DeployResourcesRequest deployRequest = TestRequestProvider.createDeployRequest();
        FunctionsToDeploy functionsToDeploy = TestDTOProvider.createFunctionsToDeploy();
        JsonObject log = JsonObject.mapFrom(TestLogProvider.createLog(1L));
        DeploymentCredentials deploymentCredentials = TestDTOProvider.createDeploymentCredentialsAWSEdge();
        DeploymentPath deploymentPath = new DeploymentPath(deployRequest.getReservation().getReservationId(), config);
        ProcessOutput poDocker = TestDTOProvider.createProcessOutput(processMainTF, "docker");
        ProcessOutput poInit = TestDTOProvider.createProcessOutput(processMainTF, "init");
        ProcessOutput poApply = TestDTOProvider.createProcessOutput(processMainTF, "apply");
        ProcessOutput poOutput = TestDTOProvider.createProcessOutput(processMainTF, "output");
        ProcessOutput poContainer = TestDTOProvider.createProcessOutput(processContainer, "container");

        when(deploymentService.packageFunctionsCode(deployRequest))
            .thenReturn(testCase.equals("packageFunctionsCodeFailed") ? Single.error(IOException::new) :
                Single.just(functionsToDeploy));
        when(deploymentService.setUpTFModules(deployRequest)).thenReturn(testCase.equals("setupTfModulesFailed") ?
            Single.error(IOException::new) : Single.just(deploymentCredentials));

        if (!testCase.equals("packageFunctionsCodeFailed")) {
            when(logService.save(any())).thenReturn(Single.just(log));
            when(reservationLogService.save(any())).thenReturn(Single.just(new JsonObject()));
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
             MockedConstruction<DockerImageService> ignoredDocker = Mockprovider
                 .mockDockerImageService(functionsToDeploy, poDocker);
             MockedConstruction<MainTerraformExecutor> ignoredMTFE =
                 Mockprovider.mockMainTerraformExecutor(deploymentPath, poInit, poApply, poOutput);
             MockedConstruction<TerraformExecutor> ignoredTFE =
                 Mockprovider.mockTerraformExecutor(deployRequest, deploymentPath, poContainer, "init")
        ) {
            deploymentChecker.deployResources(deployRequest)
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
        TerminateResourcesRequest terminateRequest = TestRequestProvider.createTerminateRequest();
        DeploymentPath deploymentPath = new DeploymentPath(terminateRequest.getReservation().getReservationId(),
            config);
        DeploymentCredentials deploymentCredentials = TestDTOProvider.createDeploymentCredentialsAWSEdge();
        JsonObject log = JsonObject.mapFrom(TestLogProvider.createLog(1L));
        ProcessOutput poDestroy = TestDTOProvider.createProcessOutput(processMainTF, "destroy");
        ProcessOutput poContainer = TestDTOProvider.createProcessOutput(processContainer, "container");

        when(deploymentService.getNecessaryCredentials(terminateRequest))
            .thenReturn(testCase.equals("getCredentialsFailed") ? Single.error(NotFoundException::new) :
                Single.just(deploymentCredentials));
        when(logService.save(any())).thenReturn(Single.just(log));
        when(reservationLogService.save(any())).thenReturn(Single.just(new JsonObject()));
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
        TerminateResourcesRequest terminateRequest = TestRequestProvider.createTerminateRequest();
        DeploymentPath deploymentPath = new DeploymentPath(terminateRequest.getReservation().getReservationId(),
            config);
        DeploymentCredentials deploymentCredentials = TestDTOProvider.createDeploymentCredentialsAWSEdge();
        ProcessOutput poDestroy = TestDTOProvider.createProcessOutput(null, "destroy");

        when(deploymentService.getNecessaryCredentials(terminateRequest)).thenReturn(Single.just(deploymentCredentials));

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
        long reservationId = 1L;

        when(deploymentService.deleteTFDirs(reservationId))
            .thenReturn(isValid ? Completable.complete() : Completable.error(IOException::new));

        deploymentChecker.deleteTFDirs(reservationId)
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
        long reservationId = 1L, resourceReservationId = 2L;
        DeploymentPath deploymentPath = new DeploymentPath(reservationId, config);
        ProcessOutput processOutput = TestDTOProvider.createProcessOutput(processContainer, testCase);
        JsonObject log = JsonObject.mapFrom(TestLogProvider.createLog(1L));

        when(processContainer.exitValue()).thenReturn(isValid ? 0 : -1);
        when(logService.save(any())).thenReturn(Single.just(log));
        when(reservationLogService.save(any())).thenReturn(Single.just(new JsonObject()));

        try(MockedConstruction<ConfigUtility> ignoredConfig = Mockprovider.mockConfig(config);
            MockedConstruction<TerraformExecutor> ignoredTFE = Mockprovider.mockTerraformExecutor(deploymentPath,
                resourceReservationId, processOutput, testCase)
        ) {
            Completable completable;
            if (testCase.equals("apply")) {
                completable = deploymentChecker.deployContainer(reservationId, resourceReservationId);
            } else {
                completable = deploymentChecker.terminateContainer(reservationId, resourceReservationId);
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
