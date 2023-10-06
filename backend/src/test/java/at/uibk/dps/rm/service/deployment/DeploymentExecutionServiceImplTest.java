package at.uibk.dps.rm.service.deployment;

import at.uibk.dps.rm.entity.deployment.*;
import at.uibk.dps.rm.entity.deployment.module.FaasModule;
import at.uibk.dps.rm.entity.deployment.module.TerraformModule;
import at.uibk.dps.rm.entity.dto.config.ConfigDTO;
import at.uibk.dps.rm.entity.dto.deployment.DeployResourcesDTO;
import at.uibk.dps.rm.entity.dto.deployment.TerminateResourcesDTO;
import at.uibk.dps.rm.entity.dto.resource.ResourceProviderEnum;
import at.uibk.dps.rm.entity.model.Function;
import at.uibk.dps.rm.entity.model.Region;
import at.uibk.dps.rm.service.ServiceProxy;
import at.uibk.dps.rm.service.deployment.docker.DockerHubImageChecker;
import at.uibk.dps.rm.service.deployment.terraform.FunctionPrepareService;
import at.uibk.dps.rm.service.deployment.terraform.MainFileService;
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
import io.vertx.junit5.RunTestOnContext;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Implements tests for the {@link DeploymentExecutionServiceImpl} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class DeploymentExecutionServiceImplTest {

    private DeploymentExecutionService deploymentExecutionService;

    @RegisterExtension
    public static final RunTestOnContext rtoc = new RunTestOnContext();

    private final ConfigDTO config = TestConfigProvider.getConfigDTO();

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
        DeployResourcesDTO deployRequest = TestRequestProvider.createDeployRequest();
        FunctionsToDeploy functionsToDeploy = TestDTOProvider.createFunctionsToDeploy();
        Function f1 = deployRequest.getFunctionDeployments().get(0).getFunction();
        Function f2 = deployRequest.getFunctionDeployments().get(2).getFunction();
        try (MockedConstruction<ConfigUtility> ignoredConfig = Mockprovider.mockConfig(config);
             MockedConstruction<FunctionPrepareService> ignoredFPS = Mockprovider
                .mockFunctionPrepareService(functionsToDeploy);
             MockedConstruction<DockerHubImageChecker> ignoredImageChecker =
                 Mockprovider.mockDockerHubImageChecker(deployRequest.getFunctionDeployments(), Set.of(f1, f2))) {
            deploymentExecutionService.packageFunctionsCode(deployRequest,
                testContext.succeeding(result -> testContext.verify(() -> {
                    assertThat(result.getFunctionIdentifiers().size()).isEqualTo(2);
                    assertThat(result.getFunctionIdentifiers().get(0)).isEqualTo("foo1_python38");
                    assertThat(result.getFunctionIdentifiers().get(1)).isEqualTo("foo2_python38");
                    assertThat(result.getDockerFunctionsString()).isEqualTo("\"  foo1_python38:\\n    " +
                        "lang: python3-flask-debian\\n    handler: ./foo1_python38\\n    " +
                        "image: user/foo1_python38:latest\\n  foo2_python38:\\n    lang: python3-flask-debian\\n    " +
                        "handler: ./foo2_python38\\n    image: user/foo2_python38:latest\\n\"");
                    testContext.completeNow();
                })));
        }
    }

    @Test
    void setUpTFModules(VertxTestContext testContext) {
        DeployResourcesDTO deployRequest = TestRequestProvider.createDeployRequest();
        Region r1 = TestResourceProviderProvider.createRegion(1L, "r1");
        Region r2 = TestResourceProviderProvider.createRegion(2L, "r2");
        TerraformModule m1 = new FaasModule(ResourceProviderEnum.AWS, r1);
        TerraformModule m2 = new FaasModule(ResourceProviderEnum.AWS, r2);

        try (MockedConstruction<ConfigUtility> ignoredConfig = Mockprovider.mockConfig(config);
            MockedConstruction<TerraformSetupService> ignoredTFS =
                Mockprovider.mockTFSetupServiceSetupModuleDirs(config, Single.just(List.of(m1, m2)));
            MockedConstruction<MainFileService> ignoredMFS = Mockprovider.mockMainFileService(Completable.complete())) {
                deploymentExecutionService.setUpTFModules(deployRequest,
                    testContext.succeeding(result -> testContext.verify(() -> {
                        assertThat(result.getEdgeLoginCredentials()).isEqualTo("");
                        assertThat(result.getCloudCredentials().size()).isEqualTo(0);
                        testContext.completeNow();
                    })));
        }
    }

    @Test
    void setUpTFModulesMainFileServiceFailed(VertxTestContext testContext) {
        DeployResourcesDTO deployRequest = TestRequestProvider.createDeployRequest();
        Region r1 = TestResourceProviderProvider.createRegion(1L, "r1");
        Region r2 = TestResourceProviderProvider.createRegion(2L, "r2");
        TerraformModule m1 = new FaasModule(ResourceProviderEnum.AWS, r1);
        TerraformModule m2 = new FaasModule(ResourceProviderEnum.AWS, r2);

        try (MockedConstruction<ConfigUtility> ignoredConfig = Mockprovider.mockConfig(config);
            MockedConstruction<TerraformSetupService> ignoredTFS =
                Mockprovider.mockTFSetupServiceSetupModuleDirs(config, Single.just(List.of(m1, m2)));
            MockedConstruction<MainFileService> ignoredMFS = Mockprovider
                .mockMainFileService(Completable.error(RuntimeException::new))) {
                deploymentExecutionService.setUpTFModules(deployRequest,
                    testContext.failing(throwable -> testContext.verify(() -> {
                        assertThat(throwable).isInstanceOf(RuntimeException.class);
                        testContext.completeNow();
                    })));
        }
    }

    @Test
    void setUpTFModulesTerraformSetupServiceFailed(VertxTestContext testContext) {
        DeployResourcesDTO deployRequest = TestRequestProvider.createDeployRequest();

        try (MockedConstruction<ConfigUtility> ignoredConfig = Mockprovider.mockConfig(config);
             MockedConstruction<TerraformSetupService> ignoredTFS = Mockprovider
                 .mockTFSetupServiceSetupModuleDirs(config, Single.error(RuntimeException::new))) {
                deploymentExecutionService.setUpTFModules(deployRequest,
                    testContext.failing(throwable -> testContext.verify(() -> {
                        assertThat(throwable).isInstanceOf(RuntimeException.class);
                        testContext.completeNow();
                    })));
        }
    }

    @Test
    void getNecessaryCredentials(VertxTestContext testContext) {
        TerminateResourcesDTO terminateRequest = TestRequestProvider.createTerminateRequest();
        DeploymentCredentials deploymentCredentials = TestDTOProvider.createDeploymentCredentialsAWSOpenfaas();

        try (MockedConstruction<ConfigUtility> ignoredConfig = Mockprovider.mockConfig(config);
             MockedConstruction<TerraformSetupService> ignoredTFS =
                 Mockprovider.mockTFSetupServiceGetTerminationCreds(Single.just(deploymentCredentials))) {
                deploymentExecutionService.getNecessaryCredentials(terminateRequest,
                    testContext.succeeding(result -> testContext.verify(() -> {
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
        TerminateResourcesDTO terminateRequest = TestRequestProvider.createTerminateRequest();

        try (MockedConstruction<ConfigUtility> ignoredConfig = Mockprovider.mockConfig(config);
             MockedConstruction<TerraformSetupService> ignoredTFS =
                     Mockprovider.mockTFSetupServiceGetTerminationCreds(Single.error(IllegalStateException::new))) {
            deploymentExecutionService.getNecessaryCredentials(terminateRequest,
                testContext.failing(throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(IllegalStateException.class);
                    testContext.completeNow();
                })));
        }
    }
}
