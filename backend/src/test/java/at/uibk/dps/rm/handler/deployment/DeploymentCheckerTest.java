package at.uibk.dps.rm.handler.deployment;

import at.uibk.dps.rm.entity.deployment.DeploymentCredentials;
import at.uibk.dps.rm.entity.deployment.DeploymentPath;
import at.uibk.dps.rm.entity.deployment.FunctionsToDeploy;
import at.uibk.dps.rm.entity.deployment.ProcessOutput;
import at.uibk.dps.rm.entity.dto.DeployResourcesRequest;
import at.uibk.dps.rm.entity.dto.TerminateResourcesRequest;
import at.uibk.dps.rm.entity.model.ServiceReservation;
import at.uibk.dps.rm.exception.DeploymentTerminationFailedException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.service.deployment.docker.DockerImageService;
import at.uibk.dps.rm.service.deployment.executor.MainTerraformExecutor;
import at.uibk.dps.rm.service.deployment.executor.TerraformExecutor;
import at.uibk.dps.rm.service.rxjava3.database.log.LogService;
import at.uibk.dps.rm.service.rxjava3.database.log.ReservationLogService;
import at.uibk.dps.rm.service.rxjava3.deployment.DeploymentService;
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
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

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
    private Process processSuccess;

    @Mock
    private Process processFailed;

    @BeforeEach
    void initTest() {
        rtoc.vertx();
        JsonMapperConfig.configJsonMapper();
        deploymentChecker = new DeploymentChecker(deploymentService, logService, reservationLogService);
    }

    @Test
    void deployResources(VertxTestContext testContext) {
        DeployResourcesRequest deployRequest = TestRequestProvider.createDeployRequest();
        FunctionsToDeploy functionsToDeploy = TestDTOProvider.createFunctionsToDeploy();
        JsonObject log = JsonObject.mapFrom(TestLogProvider.createLog(1L));
        DeploymentCredentials deploymentCredentials = TestDTOProvider.createDeploymentCredentialsAWSEdge();
        DeploymentPath deploymentPath = new DeploymentPath(deployRequest.getReservation().getReservationId(), config);
        ProcessOutput poDocker = TestDTOProvider.createProcessOutput(processSuccess, "docker");
        ProcessOutput poInit = TestDTOProvider.createProcessOutput(processSuccess, "init");
        ProcessOutput poApply = TestDTOProvider.createProcessOutput(processSuccess, "apply");
        ProcessOutput poOutput = TestDTOProvider.createProcessOutput(processSuccess, "output");

        when(deploymentService.packageFunctionsCode(deployRequest)).thenReturn(Single.just(functionsToDeploy));
        when(logService.save(any())).thenReturn(Single.just(log));
        when(reservationLogService.save(any())).thenReturn(Single.just(new JsonObject()));
        when(deploymentService.setUpTFModules(deployRequest)).thenReturn(Single.just(deploymentCredentials));
        when(processSuccess.exitValue()).thenReturn(0);

        try (MockedConstruction<ConfigUtility> ignoredConfig = Mockito.mockConstruction(ConfigUtility.class,
            (mock, context) -> given(mock.getConfig()).willReturn(Single.just(config)))) {
            try (MockedConstruction<DockerImageService> ignoredDocker = Mockito.mockConstruction(DockerImageService.class,
                (mock, context) -> given(mock.buildOpenFaasImages(functionsToDeploy.getDockerFunctionsString()))
                    .willReturn(Single.just(poDocker)))) {
                try (MockedConstruction<MainTerraformExecutor> ignoredMTFE = Mockito.mockConstruction(MainTerraformExecutor.class,
                    (mock, context) -> {
                        given(mock.setPluginCacheFolder(deploymentPath.getTFCacheFolder()))
                            .willReturn(Completable.complete());
                        given(mock.init(deploymentPath.getRootFolder())).willReturn(Single.just(poInit));
                        given(mock.apply(deploymentPath.getRootFolder())).willReturn(Single.just(poApply));
                        given(mock.getOutput(deploymentPath.getRootFolder())).willReturn(Single.just(poOutput));
                    })) {
                    try (MockedConstruction<TerraformExecutor> ignoredTFE = Mockito.mockConstruction(TerraformExecutor.class,
                        (mock, context) -> {
                            for (ServiceReservation sr : deployRequest.getServiceReservations()) {
                                Path containerPath = Path.of(deploymentPath.getRootFolder().toString(), "container",
                                    String.valueOf(sr.getResourceReservationId()));
                                given(mock.init(containerPath))
                                    .willReturn(Single.just(poInit));
                            }
                        })) {
                        deploymentChecker.deployResources(deployRequest)
                            .subscribe(result -> testContext.verify(() -> {
                                    assertThat(result.getOutput()).isEqualTo("output");
                                    assertThat(result.getProcess().exitValue()).isEqualTo(0);
                                    testContext.completeNow();
                                }),
                                throwable -> testContext.verify(() -> fail("method has thrown exception"))
                            );
                    }
                }
            }
        }
    }

    @Test
    void deployResourcesOutputFailed(VertxTestContext testContext) {
        DeployResourcesRequest deployRequest = TestRequestProvider.createDeployRequest();
        FunctionsToDeploy functionsToDeploy = TestDTOProvider.createFunctionsToDeploy();
        JsonObject log = JsonObject.mapFrom(TestLogProvider.createLog(1L));
        DeploymentCredentials deploymentCredentials = TestDTOProvider.createDeploymentCredentialsAWSEdge();
        DeploymentPath deploymentPath = new DeploymentPath(deployRequest.getReservation().getReservationId(), config);
        ProcessOutput poDocker = TestDTOProvider.createProcessOutput(processSuccess, "docker");
        ProcessOutput poInit = TestDTOProvider.createProcessOutput(processSuccess, "init");
        ProcessOutput poApply = TestDTOProvider.createProcessOutput(processSuccess, "apply");
        ProcessOutput poOutput = TestDTOProvider.createProcessOutput(processFailed, "output");

        when(deploymentService.packageFunctionsCode(deployRequest)).thenReturn(Single.just(functionsToDeploy));
        when(logService.save(any())).thenReturn(Single.just(log));
        when(reservationLogService.save(any())).thenReturn(Single.just(new JsonObject()));
        when(deploymentService.setUpTFModules(deployRequest)).thenReturn(Single.just(deploymentCredentials));
        when(processSuccess.exitValue()).thenReturn(0);
        when(processFailed.exitValue()).thenReturn(-1);

        try (MockedConstruction<ConfigUtility> ignoredConfig = Mockito.mockConstruction(ConfigUtility.class,
            (mock, context) -> given(mock.getConfig()).willReturn(Single.just(config)))) {
            try (MockedConstruction<DockerImageService> ignoredDocker = Mockito.mockConstruction(DockerImageService.class,
                (mock, context) -> given(mock.buildOpenFaasImages(functionsToDeploy.getDockerFunctionsString()))
                    .willReturn(Single.just(poDocker)))) {
                try (MockedConstruction<MainTerraformExecutor> ignoredMTFE = Mockito.mockConstruction(MainTerraformExecutor.class,
                    (mock, context) -> {
                        given(mock.setPluginCacheFolder(deploymentPath.getTFCacheFolder()))
                            .willReturn(Completable.complete());
                        given(mock.init(deploymentPath.getRootFolder())).willReturn(Single.just(poInit));
                        given(mock.apply(deploymentPath.getRootFolder())).willReturn(Single.just(poApply));
                        given(mock.getOutput(deploymentPath.getRootFolder())).willReturn(Single.just(poOutput));
                    })) {
                    try (MockedConstruction<TerraformExecutor> ignoredTFE = Mockito.mockConstruction(TerraformExecutor.class,
                        (mock, context) -> {
                            for (ServiceReservation sr : deployRequest.getServiceReservations()) {
                                Path containerPath = Path.of(deploymentPath.getRootFolder().toString(), "container",
                                    String.valueOf(sr.getResourceReservationId()));
                                given(mock.init(containerPath))
                                    .willReturn(Single.just(poInit));
                            }
                        })) {
                        deploymentChecker.deployResources(deployRequest)
                            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                                throwable -> testContext.verify(() -> {
                                    assertThat(throwable).isInstanceOf(DeploymentTerminationFailedException.class);
                                    testContext.completeNow();
                                }));
                    }
                }
            }
        }
    }

    @Test
    void deployResourcesApplyFailed(VertxTestContext testContext) {
        DeployResourcesRequest deployRequest = TestRequestProvider.createDeployRequest();
        FunctionsToDeploy functionsToDeploy = TestDTOProvider.createFunctionsToDeploy();
        JsonObject log = JsonObject.mapFrom(TestLogProvider.createLog(1L));
        DeploymentCredentials deploymentCredentials = TestDTOProvider.createDeploymentCredentialsAWSEdge();
        DeploymentPath deploymentPath = new DeploymentPath(deployRequest.getReservation().getReservationId(), config);
        ProcessOutput poDocker = TestDTOProvider.createProcessOutput(processSuccess, "docker");
        ProcessOutput poInit = TestDTOProvider.createProcessOutput(processSuccess, "init");
        ProcessOutput poApply = TestDTOProvider.createProcessOutput(processFailed, "apply");

        when(deploymentService.packageFunctionsCode(deployRequest)).thenReturn(Single.just(functionsToDeploy));
        when(logService.save(any())).thenReturn(Single.just(log));
        when(reservationLogService.save(any())).thenReturn(Single.just(new JsonObject()));
        when(deploymentService.setUpTFModules(deployRequest)).thenReturn(Single.just(deploymentCredentials));
        when(processSuccess.exitValue()).thenReturn(0);
        when(processFailed.exitValue()).thenReturn(-1);

        try (MockedConstruction<ConfigUtility> ignoredConfig = Mockito.mockConstruction(ConfigUtility.class,
            (mock, context) -> given(mock.getConfig()).willReturn(Single.just(config)))) {
            try (MockedConstruction<DockerImageService> ignoredDocker = Mockito.mockConstruction(DockerImageService.class,
                (mock, context) -> given(mock.buildOpenFaasImages(functionsToDeploy.getDockerFunctionsString()))
                    .willReturn(Single.just(poDocker)))) {
                try (MockedConstruction<MainTerraformExecutor> ignoredMTFE = Mockito.mockConstruction(MainTerraformExecutor.class,
                    (mock, context) -> {
                        given(mock.setPluginCacheFolder(deploymentPath.getTFCacheFolder()))
                            .willReturn(Completable.complete());
                        given(mock.init(deploymentPath.getRootFolder())).willReturn(Single.just(poInit));
                        given(mock.apply(deploymentPath.getRootFolder())).willReturn(Single.just(poApply));
                    })) {
                    try (MockedConstruction<TerraformExecutor> ignoredTFE = Mockito.mockConstruction(TerraformExecutor.class,
                        (mock, context) -> {
                            for (ServiceReservation sr : deployRequest.getServiceReservations()) {
                                Path containerPath = Path.of(deploymentPath.getRootFolder().toString(), "container",
                                    String.valueOf(sr.getResourceReservationId()));
                                given(mock.init(containerPath))
                                    .willReturn(Single.just(poInit));
                            }
                        })) {
                        deploymentChecker.deployResources(deployRequest)
                            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                                throwable -> testContext.verify(() -> {
                                    assertThat(throwable).isInstanceOf(DeploymentTerminationFailedException.class);
                                    testContext.completeNow();
                                }));
                    }
                }
            }
        }
    }

    @Test
    void deployResourcesInitFailed(VertxTestContext testContext) {
        DeployResourcesRequest deployRequest = TestRequestProvider.createDeployRequest();
        FunctionsToDeploy functionsToDeploy = TestDTOProvider.createFunctionsToDeploy();
        JsonObject log = JsonObject.mapFrom(TestLogProvider.createLog(1L));
        DeploymentCredentials deploymentCredentials = TestDTOProvider.createDeploymentCredentialsAWSEdge();
        DeploymentPath deploymentPath = new DeploymentPath(deployRequest.getReservation().getReservationId(), config);
        ProcessOutput poDocker = TestDTOProvider.createProcessOutput(processSuccess, "docker");
        ProcessOutput poInit = TestDTOProvider.createProcessOutput(processFailed, "init");

        when(deploymentService.packageFunctionsCode(deployRequest)).thenReturn(Single.just(functionsToDeploy));
        when(logService.save(any())).thenReturn(Single.just(log));
        when(reservationLogService.save(any())).thenReturn(Single.just(new JsonObject()));
        when(deploymentService.setUpTFModules(deployRequest)).thenReturn(Single.just(deploymentCredentials));
        when(processSuccess.exitValue()).thenReturn(0);
        when(processFailed.exitValue()).thenReturn(-1);

        try (MockedConstruction<ConfigUtility> ignoredConfig = Mockito.mockConstruction(ConfigUtility.class,
            (mock, context) -> given(mock.getConfig()).willReturn(Single.just(config)))) {
            try (MockedConstruction<DockerImageService> ignoredDocker = Mockito.mockConstruction(DockerImageService.class,
                (mock, context) -> given(mock.buildOpenFaasImages(functionsToDeploy.getDockerFunctionsString()))
                    .willReturn(Single.just(poDocker)))) {
                try (MockedConstruction<MainTerraformExecutor> ignoredMTFE = Mockito.mockConstruction(MainTerraformExecutor.class,
                    (mock, context) -> {
                        given(mock.setPluginCacheFolder(deploymentPath.getTFCacheFolder()))
                            .willReturn(Completable.complete());
                        given(mock.init(deploymentPath.getRootFolder())).willReturn(Single.just(poInit));
                    })) {
                    try (MockedConstruction<TerraformExecutor> ignoredTFE = Mockito.mockConstruction(TerraformExecutor.class,
                        (mock, context) -> {
                            for (ServiceReservation sr : deployRequest.getServiceReservations()) {
                                Path containerPath = Path.of(deploymentPath.getRootFolder().toString(), "container",
                                    String.valueOf(sr.getResourceReservationId()));
                                given(mock.init(containerPath))
                                    .willReturn(Single.just(poInit));
                            }
                        })) {
                        deploymentChecker.deployResources(deployRequest)
                            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                                throwable -> testContext.verify(() -> {
                                    assertThat(throwable).isInstanceOf(DeploymentTerminationFailedException.class);
                                    testContext.completeNow();
                                }));
                    }
                }
            }
        }
    }

    @Test
    void deployResourcesSetupTFModulesFailed(VertxTestContext testContext) {
        DeployResourcesRequest deployRequest = TestRequestProvider.createDeployRequest();
        FunctionsToDeploy functionsToDeploy = TestDTOProvider.createFunctionsToDeploy();
        JsonObject log = JsonObject.mapFrom(TestLogProvider.createLog(1L));
        ProcessOutput poDocker = TestDTOProvider.createProcessOutput(processSuccess, "docker");

        when(deploymentService.packageFunctionsCode(deployRequest)).thenReturn(Single.just(functionsToDeploy));
        when(logService.save(any())).thenReturn(Single.just(log));
        when(reservationLogService.save(any())).thenReturn(Single.just(new JsonObject()));
        when(deploymentService.setUpTFModules(deployRequest)).thenReturn(Single.error(IOException::new));
        when(processSuccess.exitValue()).thenReturn(0);

        try (MockedConstruction<ConfigUtility> ignoredConfig = Mockito.mockConstruction(ConfigUtility.class,
            (mock, context) -> given(mock.getConfig()).willReturn(Single.just(config)))) {
            try (MockedConstruction<DockerImageService> ignoredDocker = Mockito.mockConstruction(DockerImageService.class,
                (mock, context) -> given(mock.buildOpenFaasImages(functionsToDeploy.getDockerFunctionsString()))
                    .willReturn(Single.just(poDocker)))) {
                deploymentChecker.deployResources(deployRequest)
                    .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                        throwable -> testContext.verify(() -> {
                            assertThat(throwable).isInstanceOf(IOException.class);
                            testContext.completeNow();
                        }));
            }
        }
    }

    @Test
    void deployResourcesBuildDockerImageAndPushDockerImageFailed(VertxTestContext testContext) {
        DeployResourcesRequest deployRequest = TestRequestProvider.createDeployRequest();
        FunctionsToDeploy functionsToDeploy = TestDTOProvider.createFunctionsToDeploy();
        DeploymentCredentials deploymentCredentials = TestDTOProvider.createDeploymentCredentialsAWSEdge();
        JsonObject log = JsonObject.mapFrom(TestLogProvider.createLog(1L));
        ProcessOutput poDocker = TestDTOProvider.createProcessOutput(processFailed, "docker");

        when(deploymentService.packageFunctionsCode(deployRequest)).thenReturn(Single.just(functionsToDeploy));
        when(logService.save(any())).thenReturn(Single.just(log));
        when(reservationLogService.save(any())).thenReturn(Single.just(new JsonObject()));
        when(deploymentService.setUpTFModules(deployRequest)).thenReturn(Single.just(deploymentCredentials));
        when(processFailed.exitValue()).thenReturn(-1);

        try (MockedConstruction<ConfigUtility> ignoredConfig = Mockito.mockConstruction(ConfigUtility.class,
            (mock, context) -> given(mock.getConfig()).willReturn(Single.just(config)))) {
            try (MockedConstruction<DockerImageService> ignoredDocker = Mockito.mockConstruction(DockerImageService.class,
                (mock, context) -> given(mock.buildOpenFaasImages(functionsToDeploy.getDockerFunctionsString()))
                    .willReturn(Single.just(poDocker)))) {
                deploymentChecker.deployResources(deployRequest)
                    .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                        throwable -> testContext.verify(() -> {
                            assertThat(throwable).isInstanceOf(DeploymentTerminationFailedException.class);
                            testContext.completeNow();
                        }));
            }
        }
    }

    @Test
    void deployResourcesPackageFunctionsCodeFailed(VertxTestContext testContext) {
        DeployResourcesRequest deployRequest = TestRequestProvider.createDeployRequest();
        DeploymentCredentials deploymentCredentials = TestDTOProvider.createDeploymentCredentialsAWSEdge();

        when(deploymentService.packageFunctionsCode(deployRequest)).thenReturn(Single.error(IOException::new));
        when(deploymentService.setUpTFModules(deployRequest)).thenReturn(Single.just(deploymentCredentials));

        try (MockedConstruction<ConfigUtility> ignoredConfig = Mockito.mockConstruction(ConfigUtility.class,
            (mock, context) -> given(mock.getConfig()).willReturn(Single.just(config)))) {
            deploymentChecker.deployResources(deployRequest)
                .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                    throwable -> testContext.verify(() -> {
                        assertThat(throwable).isInstanceOf(IOException.class);
                        testContext.completeNow();
                    }));
        }
    }

    @Test
    void terminateResource(VertxTestContext testContext) {
        TerminateResourcesRequest terminateRequest = TestRequestProvider.createTerminateRequest();
        DeploymentPath deploymentPath = new DeploymentPath(terminateRequest.getReservation().getReservationId(),
            config);
        DeploymentCredentials deploymentCredentials = TestDTOProvider.createDeploymentCredentialsAWSEdge();
        JsonObject log = JsonObject.mapFrom(TestLogProvider.createLog(1L));
        ProcessOutput poDestroy = TestDTOProvider.createProcessOutput(processSuccess, "destroy");

        when(deploymentService.getNecessaryCredentials(terminateRequest)).thenReturn(Single.just(deploymentCredentials));
        when(logService.save(any())).thenReturn(Single.just(log));
        when(reservationLogService.save(any())).thenReturn(Single.just(new JsonObject()));
        when(processSuccess.exitValue()).thenReturn(0);

        try (MockedConstruction<ConfigUtility> ignoredConfig = Mockito.mockConstruction(ConfigUtility.class,
            (mock, context) -> given(mock.getConfig()).willReturn(Single.just(config)))) {
            try (MockedConstruction<MainTerraformExecutor> ignoredMTFE = Mockito.mockConstruction(MainTerraformExecutor.class,
                (mock, context) -> {
                    given(mock.setPluginCacheFolder(deploymentPath.getTFCacheFolder()))
                        .willReturn(Completable.complete());
                    given(mock.destroy(deploymentPath.getRootFolder())).willReturn(Single.just(poDestroy));
                })) {
                try (MockedConstruction<TerraformExecutor> ignoredTFE = Mockito.mockConstruction(TerraformExecutor.class,
                    (mock, context) -> {
                        for (ServiceReservation sr : terminateRequest.getServiceReservations()) {
                            Path containerPath = Path.of(deploymentPath.getRootFolder().toString(), "container",
                                String.valueOf(sr.getResourceReservationId()));
                            given(mock.destroy(containerPath))
                                .willReturn(Single.just(poDestroy));
                        }
                    })) {
                    deploymentChecker.terminateResources(terminateRequest)
                        .blockingSubscribe(() -> {
                            },
                            throwable -> testContext.verify(() -> fail("method has thrown exception"))
                        );
                    testContext.completeNow();
                }
            }
        }
    }

    @Test
    void terminateResourceDestroyFailed(VertxTestContext testContext) {
        TerminateResourcesRequest terminateRequest = TestRequestProvider.createTerminateRequest();
        DeploymentPath deploymentPath = new DeploymentPath(terminateRequest.getReservation().getReservationId(),
            config);
        DeploymentCredentials deploymentCredentials = TestDTOProvider.createDeploymentCredentialsAWSEdge();
        JsonObject log = JsonObject.mapFrom(TestLogProvider.createLog(1L));
        ProcessOutput poDestroy = TestDTOProvider.createProcessOutput(processFailed, "destroy");

        when(deploymentService.getNecessaryCredentials(terminateRequest)).thenReturn(Single.just(deploymentCredentials));
        when(logService.save(any())).thenReturn(Single.just(log));
        when(reservationLogService.save(any())).thenReturn(Single.just(new JsonObject()));
        when(processFailed.exitValue()).thenReturn(-1);

        try (MockedConstruction<ConfigUtility> ignoredConfig = Mockito.mockConstruction(ConfigUtility.class,
            (mock, context) -> given(mock.getConfig()).willReturn(Single.just(config)))) {
            try (MockedConstruction<MainTerraformExecutor> ignoredMTFE = Mockito.mockConstruction(MainTerraformExecutor.class,
                (mock, context) -> {
                    given(mock.setPluginCacheFolder(deploymentPath.getTFCacheFolder()))
                        .willReturn(Completable.complete());
                    given(mock.destroy(deploymentPath.getRootFolder())).willReturn(Single.just(poDestroy));
                })) {
                try (MockedConstruction<TerraformExecutor> ignoredTFE = Mockito.mockConstruction(TerraformExecutor.class,
                    (mock, context) -> {
                        for (ServiceReservation sr : terminateRequest.getServiceReservations()) {
                            Path containerPath = Path.of(deploymentPath.getRootFolder().toString(), "container",
                                String.valueOf(sr.getResourceReservationId()));
                            given(mock.destroy(containerPath))
                                .willReturn(Single.just(poDestroy));
                        }
                    })) {
                    deploymentChecker.terminateResources(terminateRequest)
                        .blockingSubscribe(() -> testContext.verify(() ->
                                fail("method did not throw exception")),
                            throwable -> testContext.verify(() -> {
                                assertThat(throwable).isInstanceOf(DeploymentTerminationFailedException.class);
                                testContext.completeNow();
                            })
                        );
                }
            }
        }
    }

    @Test
    void terminateResourceGetCredentialsFailed(VertxTestContext testContext) {
        TerminateResourcesRequest terminateRequest = TestRequestProvider.createTerminateRequest();
        DeploymentPath deploymentPath = new DeploymentPath(terminateRequest.getReservation().getReservationId(),
            config);
        ProcessOutput poDestroy = TestDTOProvider.createProcessOutput(processSuccess, "destroy");
        JsonObject log = JsonObject.mapFrom(TestLogProvider.createLog(1L));

        when(logService.save(any())).thenReturn(Single.just(log));
        when(reservationLogService.save(any())).thenReturn(Single.just(new JsonObject()));
        when(deploymentService.getNecessaryCredentials(terminateRequest)).thenReturn(Single.error(NotFoundException::new));

        try (MockedConstruction<TerraformExecutor> ignoredTFE = Mockito.mockConstruction(TerraformExecutor.class,
            (mock, context) -> {
                for (ServiceReservation sr : terminateRequest.getServiceReservations()) {
                    Path containerPath = Path.of(deploymentPath.getRootFolder().toString(), "container",
                        String.valueOf(sr.getResourceReservationId()));
                    given(mock.destroy(containerPath))
                        .willReturn(Single.just(poDestroy));
                }
            })) {
            try (MockedConstruction<ConfigUtility> ignoredConfig = Mockito.mockConstruction(ConfigUtility.class,
                (mock, context) -> given(mock.getConfig()).willReturn(Single.just(config)))) {
                deploymentChecker.terminateResources(terminateRequest)
                    .blockingSubscribe(() -> testContext.verify(() -> fail("method did not throw exception")),
                        throwable -> testContext.verify(() -> {
                            assertThat(throwable).isInstanceOf(NotFoundException.class);
                            testContext.completeNow();
                        })
                    );
            }
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

        try (MockedConstruction<ConfigUtility> ignoredConfig = Mockito.mockConstruction(ConfigUtility.class,
            (mock, context) -> given(mock.getConfig()).willReturn(Single.just(config)))) {
            try (MockedConstruction<MainTerraformExecutor> ignoredMTFE = Mockito.mockConstruction(MainTerraformExecutor.class,
                (mock, context) -> {
                    given(mock.setPluginCacheFolder(deploymentPath.getTFCacheFolder()))
                        .willReturn(Completable.complete());
                    given(mock.destroy(deploymentPath.getRootFolder())).willReturn(Single.just(poDestroy));
                })) {
                try (MockedConstruction<TerraformExecutor> ignoredTFE = Mockito.mockConstruction(TerraformExecutor.class,
                    (mock, context) -> {
                        for (ServiceReservation sr : terminateRequest.getServiceReservations()) {
                            Path containerPath = Path.of(deploymentPath.getRootFolder().toString(), "container",
                                String.valueOf(sr.getResourceReservationId()));
                            given(mock.destroy(containerPath))
                                .willReturn(Single.just(poDestroy));
                        }
                    })) {
                    deploymentChecker.terminateResources(terminateRequest)
                        .blockingSubscribe(() -> {
                            },
                            throwable -> testContext.verify(() -> fail("method has thrown exception"))
                        );
                    testContext.completeNow();
                }
            }
        }
    }

    @Test
    void deleteTFDirs(VertxTestContext testContext) {
        long reservationId = 1L;

        when(deploymentService.deleteTFDirs(reservationId)).thenReturn(Completable.complete());

        deploymentChecker.deleteTFDirs(reservationId)
            .blockingSubscribe(() -> {},
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
        testContext.completeNow();
    }

    @Test
    void deleteTFDirsFailed(VertxTestContext testContext) {
        long reservationId = 1L;

        when(deploymentService.deleteTFDirs(reservationId)).thenReturn(Completable.error(IOException::new));

        deploymentChecker.deleteTFDirs(reservationId)
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(IOException.class);
                    testContext.completeNow();
                })
            );
    }
}
