package at.uibk.dps.rm.service.deployment;

import at.uibk.dps.rm.entity.deployment.*;
import at.uibk.dps.rm.entity.deployment.module.FaasModule;
import at.uibk.dps.rm.entity.deployment.module.TerraformModule;
import at.uibk.dps.rm.entity.dto.deployment.DeployResourcesDAO;
import at.uibk.dps.rm.entity.dto.deployment.TerminateResourcesDAO;
import at.uibk.dps.rm.entity.dto.resource.ResourceProviderEnum;
import at.uibk.dps.rm.entity.model.Region;
import at.uibk.dps.rm.service.ServiceProxy;
import at.uibk.dps.rm.service.deployment.terraform.FunctionPrepareService;
import at.uibk.dps.rm.service.deployment.terraform.MainFileService;
import at.uibk.dps.rm.service.deployment.terraform.TerraformFileService;
import at.uibk.dps.rm.service.deployment.terraform.TerraformSetupService;
import at.uibk.dps.rm.testutil.mockprovider.Mockprovider;
import at.uibk.dps.rm.testutil.objectprovider.TestConfigProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestDTOProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestRequestProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestResourceProviderProvider;
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
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

/**
 * Implements tests for the {@link DeploymentExecutionServiceImpl} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class DeploymentServiceImplTest {

    private DeploymentExecutionService deploymentExecutionService;

    @RegisterExtension
    private static final RunTestOnContext rtoc = new RunTestOnContext();

    private final JsonObject config = TestConfigProvider.getConfig();

    @BeforeEach
    void initTest() {
        rtoc.vertx();
        System.setProperty("os.name", "Linux");
        JsonMapperConfig.configJsonMapper();
        deploymentExecutionService = new DeploymentExecutionServiceImpl();
    }

    @Test
    void getServiceProxyAddress() {
        String expected = "deployment-execution-service-address";

        String result = ((ServiceProxy) deploymentExecutionService).getServiceProxyAddress();

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void packageFunctionsCode(VertxTestContext testContext) {
        DeployResourcesDAO deployRequest = TestRequestProvider.createDeployRequest();
        FunctionsToDeploy functionsToDeploy = TestDTOProvider.createFunctionsToDeploy();

        try (MockedConstruction<ConfigUtility> ignoredConfig = Mockprovider.mockConfig(config);
            MockedConstruction<FunctionPrepareService> ignoredFPS = Mockprovider
                .mockFunctionPrepareService(functionsToDeploy)) {
            deploymentExecutionService.packageFunctionsCode(deployRequest)
                .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                    assertThat(result.getFunctionIdentifiers().size()).isEqualTo(2);
                    assertThat(result.getFunctionIdentifiers().get(0)).isEqualTo("foo1_python39");
                    assertThat(result.getFunctionIdentifiers().get(1)).isEqualTo("foo2_python39");
                    assertThat(result.getDockerFunctionsString()).isEqualTo("\"  foo1_python39:\\n    " +
                        "lang: python3-flask-debian\\n    handler: ./foo1_python39\\n    " +
                        "image: user/foo1_python39:latest\\n  foo2_python39:\\n    lang: python3-flask-debian\\n    " +
                        "handler: ./foo2_python39\\n    image: user/foo2_python39:latest\\n\"");
                    testContext.completeNow();
                })));
        }
    }

    @Test
    void setUpTFModules(VertxTestContext testContext) {
        DeployResourcesDAO deployRequest = TestRequestProvider.createDeployRequest();
        Region r1 = TestResourceProviderProvider.createRegion(1L, "r1");
        Region r2 = TestResourceProviderProvider.createRegion(2L, "r2");
        TerraformModule m1 = new FaasModule(ResourceProviderEnum.AWS, r1);
        TerraformModule m2 = new FaasModule(ResourceProviderEnum.AWS, r2);

        try (MockedConstruction<ConfigUtility> ignoredConfig = Mockprovider.mockConfig(config);
            MockedConstruction<TerraformSetupService> ignoredTFS =
                Mockprovider.mockTFSetupServiceSetupModuleDirs(Single.just(List.of(m1, m2)));
            MockedConstruction<MainFileService> ignoredMFS = Mockprovider.mockMainFileService(Completable.complete())) {
                deploymentExecutionService.setUpTFModules(deployRequest)
                    .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                        assertThat(result.getEdgeLoginCredentials()).isEqualTo("");
                        assertThat(result.getCloudCredentials().size()).isEqualTo(0);
                        testContext.completeNow();
                    })));
        }
    }

    @Test
    void setUpTFModulesMainFileServiceFailed(VertxTestContext testContext) {
        DeployResourcesDAO deployRequest = TestRequestProvider.createDeployRequest();
        Region r1 = TestResourceProviderProvider.createRegion(1L, "r1");
        Region r2 = TestResourceProviderProvider.createRegion(2L, "r2");
        TerraformModule m1 = new FaasModule(ResourceProviderEnum.AWS, r1);
        TerraformModule m2 = new FaasModule(ResourceProviderEnum.AWS, r2);

        try (MockedConstruction<ConfigUtility> ignoredConfig = Mockprovider.mockConfig(config);
            MockedConstruction<TerraformSetupService> ignoredTFS =
                Mockprovider.mockTFSetupServiceSetupModuleDirs(Single.just(List.of(m1, m2)));
            MockedConstruction<MainFileService> ignoredMFS = Mockprovider
                .mockMainFileService(Completable.error(RuntimeException::new))) {
                deploymentExecutionService.setUpTFModules(deployRequest)
                    .onComplete(testContext.failing(throwable -> testContext.verify(() -> {
                        assertThat(throwable).isInstanceOf(RuntimeException.class);
                        testContext.completeNow();
                    })));
        }
    }

    @Test
    void setUpTFModulesTerraformSetupServiceFailed(VertxTestContext testContext) {
        DeployResourcesDAO deployRequest = TestRequestProvider.createDeployRequest();

        try (MockedConstruction<ConfigUtility> ignoredConfig = Mockprovider.mockConfig(config);
             MockedConstruction<TerraformSetupService> ignoredTFS = Mockprovider
                 .mockTFSetupServiceSetupModuleDirs(Single.error(RuntimeException::new))) {
                deploymentExecutionService.setUpTFModules(deployRequest)
                    .onComplete(testContext.failing(throwable -> testContext.verify(() -> {
                        assertThat(throwable).isInstanceOf(RuntimeException.class);
                        testContext.completeNow();
                    })));
        }
    }

    @Test
    void getNecessaryCredentials(VertxTestContext testContext) {
        TerminateResourcesDAO terminateRequest = TestRequestProvider.createTerminateRequest();
        DeploymentCredentials deploymentCredentials = TestDTOProvider.createDeploymentCredentialsAWSOpenfaas();

        try (MockedConstruction<ConfigUtility> ignoredConfig = Mockprovider.mockConfig(config);
             MockedConstruction<TerraformSetupService> ignoredTFS =
                 Mockprovider.mockTFSetupServiceGetTerminationCreds(Single.just(deploymentCredentials))) {
                deploymentExecutionService.getNecessaryCredentials(terminateRequest)
                    .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                        assertThat(result.getOpenFaasCredentialsString())
                            .isEqualTo("openfaas_login_data={r1={auth_user=\"user\", auth_pw=\"pw\"}}");
                        assertThat(result.getCloudCredentials().size()).isEqualTo(1);
                        assertThat(result.getCloudCredentials().get(0).getCredentialsId()).isEqualTo(1L);
                        testContext.completeNow();
                    })));
        }
    }

    @Test
    void getNecessaryCredentialsFailed(VertxTestContext testContext) {
        TerminateResourcesDAO terminateRequest = TestRequestProvider.createTerminateRequest();

        try (MockedConstruction<ConfigUtility> ignoredConfig = Mockprovider.mockConfig(config);
             MockedConstruction<TerraformSetupService> ignoredTFS =
                     Mockprovider.mockTFSetupServiceGetTerminationCreds(Single.error(IllegalStateException::new))) {
            deploymentExecutionService.getNecessaryCredentials(terminateRequest)
                .onComplete(testContext.failing(throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(IllegalStateException.class);
                    testContext.completeNow();
                })));
        }
    }

    @Test
    void deleteTFDirs(VertxTestContext testContext) {
        long deploymentId = 1L;
        Path rootFolder = new DeploymentPath(deploymentId, config).getRootFolder();

        try (MockedConstruction<ConfigUtility> ignoredConfig = Mockprovider.mockConfig(config);
             MockedStatic<TerraformFileService> mockedStatic = Mockito.mockStatic(TerraformFileService.class)) {
            mockedStatic.when(() -> TerraformFileService.deleteAllDirs(any(), eq(rootFolder)))
                .thenReturn(Completable.complete());
            deploymentExecutionService.deleteTFDirs(deploymentId)
                .onComplete(testContext.succeedingThenComplete());
        }
        testContext.completeNow();
    }

    @Test
    void deleteTFDirsFailed(VertxTestContext testContext) {
        long deploymentId = 1L;
        JsonObject config = TestConfigProvider.getConfig();
        Path rootFolder = new DeploymentPath(deploymentId, config).getRootFolder();

        try (MockedConstruction<ConfigUtility> ignoredConfig = Mockprovider.mockConfig(config);
             MockedStatic<TerraformFileService> mockedStatic = Mockito.mockStatic(TerraformFileService.class)) {
                mockedStatic.when(() -> TerraformFileService.deleteAllDirs(any(), eq(rootFolder)))
                    .thenReturn(Completable.error(IOException::new));
                deploymentExecutionService.deleteTFDirs(deploymentId)
                    .onComplete(testContext.failing(throwable -> testContext.verify(() -> {
                        assertThat(throwable).isInstanceOf(IOException.class);
                        testContext.completeNow();
                    })));
        }
        testContext.completeNow();
    }
}
