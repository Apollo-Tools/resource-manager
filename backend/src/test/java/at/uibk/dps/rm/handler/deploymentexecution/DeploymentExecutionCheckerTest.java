package at.uibk.dps.rm.handler.deploymentexecution;

import at.uibk.dps.rm.entity.deployment.DeploymentCredentials;
import at.uibk.dps.rm.entity.deployment.DeploymentPath;
import at.uibk.dps.rm.entity.deployment.FunctionsToDeploy;
import at.uibk.dps.rm.entity.deployment.ProcessOutput;
import at.uibk.dps.rm.entity.deployment.module.FaasModule;
import at.uibk.dps.rm.entity.deployment.module.ModuleType;
import at.uibk.dps.rm.entity.deployment.module.ServiceModule;
import at.uibk.dps.rm.entity.deployment.module.TerraformModule;
import at.uibk.dps.rm.entity.dto.config.ConfigDTO;
import at.uibk.dps.rm.entity.dto.deployment.DeployResourcesDTO;
import at.uibk.dps.rm.entity.dto.deployment.SetupTFModulesOutputDTO;
import at.uibk.dps.rm.entity.dto.deployment.StartupShutdownServicesDTO;
import at.uibk.dps.rm.entity.dto.deployment.TerminateResourcesDTO;
import at.uibk.dps.rm.entity.dto.resource.ResourceProviderEnum;
import at.uibk.dps.rm.entity.model.Deployment;
import at.uibk.dps.rm.entity.model.Region;
import at.uibk.dps.rm.entity.model.ServiceDeployment;
import at.uibk.dps.rm.exception.DeploymentTerminationFailedException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import at.uibk.dps.rm.service.deployment.docker.LambdaJavaBuildService;
import at.uibk.dps.rm.service.deployment.docker.LambdaLayerService;
import at.uibk.dps.rm.service.deployment.docker.OpenFaasImageService;
import at.uibk.dps.rm.service.deployment.executor.MainTerraformExecutor;
import at.uibk.dps.rm.service.deployment.executor.TerraformExecutor;
import at.uibk.dps.rm.service.deployment.terraform.TerraformFileService;
import at.uibk.dps.rm.service.rxjava3.database.log.DeploymentLogService;
import at.uibk.dps.rm.service.rxjava3.database.log.LogService;
import at.uibk.dps.rm.service.rxjava3.deployment.DeploymentExecutionService;
import at.uibk.dps.rm.testutil.mockprovider.DeploymentPrepareMockprovider;
import at.uibk.dps.rm.testutil.mockprovider.Mockprovider;
import at.uibk.dps.rm.testutil.mockprovider.TerraformExecutorMockprovider;
import at.uibk.dps.rm.testutil.objectprovider.*;
import at.uibk.dps.rm.util.configuration.ConfigUtility;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.RunTestOnContext;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.core.file.FileSystem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Implements tests for the {@link DeploymentExecutionChecker} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class DeploymentExecutionCheckerTest {

    @RegisterExtension
    public static final RunTestOnContext rtoc = new RunTestOnContext();

    private DeploymentExecutionChecker deploymentChecker;

    private final ConfigDTO config = TestConfigProvider.getConfigDTO();

    @Mock
    private ServiceProxyProvider serviceProxyProvider;

    @Mock
    private DeploymentExecutionService deploymentExecutionService;

    @Mock
    private LogService logService;

    @Mock
    private DeploymentLogService deploymentLogService;

    @Mock
    private Process processMainTF;

    @Mock
    private Process processService;

    @Mock
    private Process processBuildLambda;

    private JsonObject log;
    private DeploymentPath deploymentPath;
    private StartupShutdownServicesDTO startupShutdownServicesDTO;
    private List<String> serviceTargets, refreshTargets;

    @BeforeEach
    void initTest() {
        rtoc.vertx();
        JsonMapperConfig.configJsonMapper();
        deploymentChecker = new DeploymentExecutionChecker(serviceProxyProvider);
        log = JsonObject.mapFrom(TestLogProvider.createLog(1L));
        Deployment d1 = TestDeploymentProvider.createDeployment(1L);
        deploymentPath = new DeploymentPath(d1.getDeploymentId(), config);
        ServiceDeployment sd1 = TestServiceProvider.createServiceDeployment(2L, 3L, d1);
        ServiceDeployment sd2 = TestServiceProvider.createServiceDeployment(3L, 4L, d1);
        startupShutdownServicesDTO = TestDTOProvider.createStartupShutdownServicesDTO(d1,
            List.of(sd1, sd2));
        serviceTargets = List.of("module.service_deploy.module.deployment_2", "module.service_deploy.module" +
            ".deployment_3");
        refreshTargets = List.of("module.service_deploy");

        lenient().when(serviceProxyProvider.getDeploymentExecutionService()).thenReturn(deploymentExecutionService);
        lenient().when(serviceProxyProvider.getLogService()).thenReturn(logService);
        lenient().when(serviceProxyProvider.getDeploymentLogService()).thenReturn(deploymentLogService);
    }

    @ParameterizedTest
    @ValueSource(strings={"valid", "outputFailed", "applyFailed", "initFailed",
        "setupTfModulesFailed", "buildDockerFailed", "packageFunctionsCodeFailed"})
    void deployResources(String testCase, VertxTestContext testContext) {
        DeployResourcesDTO deployRequest = TestRequestProvider.createDeployRequest();
        FunctionsToDeploy functionsToDeploy = TestDTOProvider.createFunctionsToDeploy();
        JsonObject log = JsonObject.mapFrom(TestLogProvider.createLog(1L));
        DeploymentCredentials deploymentCredentials = TestDTOProvider.createDeploymentCredentialsAWSOpenfaas();
        Region region = TestResourceProviderProvider.createRegion(1L, "us-east-1");
        TerraformModule faasModule = new FaasModule(ResourceProviderEnum.AWS, region);
        TerraformModule servicePrepullModule = new ServiceModule("service_prepull", ModuleType.SERVICE_PREPULL);
        TerraformModule serviceDeployModule = new ServiceModule("service_deploy", ModuleType.SERVICE_DEPLOY);
        List<String> initTargets = Stream.of(faasModule, servicePrepullModule)
            .map(module -> "module." + module.getModuleName())
            .collect(Collectors.toList());
        SetupTFModulesOutputDTO modulesOutput = TestDTOProvider.createTFModulesOutput(List.of(faasModule,
                servicePrepullModule, serviceDeployModule),
            deploymentCredentials);
        DeploymentPath deploymentPath = new DeploymentPath(deployRequest.getDeployment().getDeploymentId(), config);
        ProcessOutput poDocker = TestDTOProvider.createProcessOutput(processMainTF, "docker");
        ProcessOutput poInit = TestDTOProvider.createProcessOutput(processMainTF, "init");
        ProcessOutput poApply = TestDTOProvider.createProcessOutput(processMainTF, "apply");
        ProcessOutput poRefresh = TestDTOProvider.createProcessOutput(processMainTF, "refresh");
        ProcessOutput poOutput = TestDTOProvider.createProcessOutput(processMainTF, "output");
        ProcessOutput poService = TestDTOProvider.createProcessOutput(processService, "service");
        ProcessOutput poBuildLambda = TestDTOProvider.createProcessOutput(processBuildLambda, "lambda");

        when(deploymentExecutionService.packageFunctionsCode(deployRequest))
            .thenReturn(testCase.equals("packageFunctionsCodeFailed") ? Single.error(IOException::new) :
                Single.just(functionsToDeploy));
        when(deploymentExecutionService.setUpTFModules(deployRequest))
            .thenReturn(testCase.equals("setupTfModulesFailed") ? Single.error(IOException::new) :
                Single.just(modulesOutput));

        if (!testCase.equals("packageFunctionsCodeFailed")) {
            when(logService.save(any())).thenReturn(Single.just(log));
            when(deploymentLogService.save(any())).thenReturn(Single.just(new JsonObject()));
            when(processMainTF.exitValue()).thenReturn(testCase.equals("buildDockerFailed") ? -1 : 0)
                .thenReturn(testCase.equals("initFailed") ? -1 : 0)
                .thenReturn(testCase.equals("applyFailed") ? -1 : 0)
                .thenReturn(testCase.equals("outputFailed") ? -1 : 0);
        }

        try (MockedConstruction<ConfigUtility> ignoredConfig = Mockprovider.mockConfig(config);
             MockedConstruction<OpenFaasImageService> ignoredDocker = DeploymentPrepareMockprovider
                 .mockDockerImageService(poDocker);
             MockedConstruction<MainTerraformExecutor> ignoredMTFE =
                 TerraformExecutorMockprovider.mockMainTerraformExecutor(deploymentPath, poInit, poApply, poRefresh,
                     poOutput, initTargets);
             MockedConstruction<TerraformExecutor> ignoredTFE =
                 TerraformExecutorMockprovider.mockTerraformExecutor(deployRequest, deploymentPath, poService, "init");
             MockedConstruction<LambdaJavaBuildService> ignoredLJBS = DeploymentPrepareMockprovider.mockLambdaJavaService(poBuildLambda);
             MockedConstruction<LambdaLayerService> ignoredLLS = DeploymentPrepareMockprovider.mockLambdaLayerService(poBuildLambda)
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
    @ValueSource(strings = {"valid", "destroyFailed", "getCredentialsFailed"})
    void terminateResources(String testCase, VertxTestContext testContext) {
        TerminateResourcesDTO terminateRequest = TestRequestProvider.createTerminateRequest();
        DeploymentCredentials deploymentCredentials = TestDTOProvider.createDeploymentCredentialsAWSOpenfaas();
        ProcessOutput poDestroy = TestDTOProvider.createProcessOutput(processMainTF, "destroy");
        ProcessOutput poService = TestDTOProvider.createProcessOutput(processService, "service");

        when(deploymentExecutionService.getNecessaryCredentials(terminateRequest))
            .thenReturn(testCase.equals("getCredentialsFailed") ? Single.error(NotFoundException::new) :
                Single.just(deploymentCredentials));
        if (!testCase.equals("getCredentialsFailed")) {
            when(logService.save(any())).thenReturn(Single.just(log));
            when(deploymentLogService.save(any())).thenReturn(Single.just(new JsonObject()));
        }
        if (!testCase.equals("getCredentialsFailed") && !testCase.equals("destroyContainerFailed")) {
            when(processMainTF.exitValue()).thenReturn(testCase.equals("destroyFailed") ? -1 : 0);
        }

        try (MockedConstruction<ConfigUtility> ignoredConfig = Mockprovider.mockConfig(config);
             MockedConstruction<MainTerraformExecutor> ignoredMTFE = TerraformExecutorMockprovider
                 .mockMainTerraformExecutor(deploymentPath, poDestroy);
             MockedConstruction<TerraformExecutor> ignoredTFE = TerraformExecutorMockprovider
                 .mockTerraformExecutor(terminateRequest, deploymentPath, poService, "destroy")
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
                 TerraformExecutorMockprovider.mockMainTerraformExecutor(deploymentPath, poDestroy);
             MockedConstruction<TerraformExecutor> ignoredTFE =
                 TerraformExecutorMockprovider.mockTerraformExecutor(terminateRequest, deploymentPath, poDestroy, "destroy")
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
    void startupServices(boolean isValid, VertxTestContext testContext) {
        ProcessOutput processOutput = TestDTOProvider.createProcessOutput(processService,
            "{\"service_output\": {\"value\": {\"service_deployment_2\": \"test2\",\"service_deployment_3\": " +
                "\"test3\"}}}");

        when(processService.exitValue()).thenReturn(isValid ? 0 : -1);
        when(logService.save(any())).thenReturn(Single.just(log));
        when(deploymentLogService.save(any())).thenReturn(Single.just(new JsonObject()));

        try(MockedConstruction<ConfigUtility> ignoredConfig = Mockprovider.mockConfig(config);
            MockedConstruction<TerraformExecutor> ignoredTFE = TerraformExecutorMockprovider.mockTerraformExecutor(deploymentPath,
                processOutput, serviceTargets, refreshTargets, "apply", "refresh", "output")
        ) {
            Single<JsonObject> single = deploymentChecker.startupServices(startupShutdownServicesDTO);
            single.subscribe(result -> testContext.verify(() -> {
                if (!isValid) {
                    fail("method did not throw exception");
                }
                assertThat(result.encode()).isEqualTo("{\"service_deployments\":{\"" +
                    "service_deployment_2\":\"test2\",\"service_deployment_3\":\"test3\"}}");
                testContext.completeNow();
            }), throwable -> testContext.verify(() -> {
                if (isValid) {
                    fail("method has thrown exception");
                } else {
                    assertThat(throwable).isInstanceOf(DeploymentTerminationFailedException.class);
                }
                testContext.completeNow();
            }));
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shutdownServices(boolean isValid, VertxTestContext testContext) {
        ProcessOutput processOutput = TestDTOProvider.createProcessOutput(processService, "destroy");

        when(processService.exitValue()).thenReturn(isValid ? 0 : -1);
        when(logService.save(any())).thenReturn(Single.just(log));
        when(deploymentLogService.save(any())).thenReturn(Single.just(new JsonObject()));

        try(MockedConstruction<ConfigUtility> ignoredConfig = Mockprovider.mockConfig(config);
            MockedConstruction<TerraformExecutor> ignoredTFE = TerraformExecutorMockprovider.mockTerraformExecutor(deploymentPath,
                processOutput, serviceTargets, refreshTargets, "destroy")
        ) {
            Completable completable = deploymentChecker.shutdownServices(startupShutdownServicesDTO);
            completable.subscribe(() -> testContext.verify(() -> {
                if (!isValid) {
                    fail("method did not throw exception");
                }
                testContext.completeNow();
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

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void deleteTFDirs(boolean isValid, VertxTestContext testContext) {
        long deploymentId = 1L;

        try(MockedConstruction<ConfigUtility> ignored = Mockprovider.mockConfig(config);
            MockedStatic<TerraformFileService> mocked = mockStatic(TerraformFileService.class)) {
            mocked.when(() -> TerraformFileService.deleteAllDirs(any(FileSystem.class), any(Path.class)))
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
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void tfLockFileExists(boolean exists, VertxTestContext testContext, Vertx vertx) {
        Path tfPath = Path.of("test-tmp");
        if (exists) {
            vertx.fileSystem()
                .mkdirsBlocking(tfPath.toString())
                .createFileBlocking(Path.of(tfPath.toString(), ".terraform.lock.hcl").toString());
        }

        deploymentChecker.tfLockFileExists(tfPath.toString())
            .subscribe(result -> testContext.verify(() -> {
                assertThat(result).isEqualTo(exists);
                if (exists) {
                    vertx.fileSystem()
                        .deleteBlocking(Path.of(tfPath.toString(), ".terraform.lock.hcl").toString());
                }
                testContext.completeNow();
            }), throwable -> testContext.verify(() -> fail("method has thrown exception")));
    }

}
