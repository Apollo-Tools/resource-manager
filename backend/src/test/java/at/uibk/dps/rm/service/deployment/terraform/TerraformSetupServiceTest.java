package at.uibk.dps.rm.service.deployment.terraform;

import at.uibk.dps.rm.entity.deployment.DeploymentCredentials;
import at.uibk.dps.rm.entity.deployment.DeploymentPath;
import at.uibk.dps.rm.entity.dto.deployment.DeployResourcesDAO;
import at.uibk.dps.rm.entity.dto.deployment.TerminateResourcesDAO;
import at.uibk.dps.rm.testutil.mockprovider.Mockprovider;
import at.uibk.dps.rm.testutil.objectprovider.TestConfigProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestRequestProvider;
import io.reactivex.rxjava3.core.Completable;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.core.Vertx;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * Implements tests for the {@link TerraformSetupService} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class TerraformSetupServiceTest {

    private final JsonObject config = TestConfigProvider.getConfig();

    @Test
    void setupTFModuleDirs(Vertx vertx, VertxTestContext testContext) {
        DeployResourcesDAO deployRequest = TestRequestProvider.createDeployRequest();
        DeploymentPath deploymentPath = new DeploymentPath(1L, config);
        DeploymentCredentials deploymentCredentials = new DeploymentCredentials();
        TerraformSetupService service = new TerraformSetupService(vertx, deployRequest, deploymentPath,
            deploymentCredentials);
        System.setProperty("os.name", "Linux");
        try (MockedConstruction<RegionFaasFileService> ignored = Mockprovider
            .mockRegionFaasFileService(Completable.complete())) {
                service.setUpTFModuleDirs()
                    .subscribe(result -> testContext.verify(() -> {
                            assertThat(result.size()).isEqualTo(2);
                            assertThat(result.get(0).getModuleName()).isEqualTo("aws_us_east_1");
                            assertThat(result.get(1).getModuleName()).isEqualTo("container");
                            assertThat(deploymentCredentials.getCloudCredentials().size()).isEqualTo(1);
                            assertThat(deploymentCredentials.getCloudCredentials().get(0).getResourceProvider()
                                .getProvider()).isEqualTo("aws");
                            assertThat(deploymentCredentials.getOpenFaasCredentialsString())
                                .isEqualTo("openfaas_login_data={r3={auth_user=\"user\",auth_pw=\"pw\"}}");
                            testContext.completeNow();
                        }),
                        throwable -> testContext.verify(() -> fail("method has thrown exception"))
                    );
        }
    }

    @Test
    void setupTFModuleDirsDeployRequestNull(Vertx vertx, VertxTestContext testContext) {
        TerminateResourcesDAO terminateRequest = TestRequestProvider.createTerminateRequest();
        DeploymentPath deploymentPath = new DeploymentPath(1L, config);
        DeploymentCredentials deploymentCredentials = new DeploymentCredentials();
        TerraformSetupService service = new TerraformSetupService(vertx, terminateRequest, deploymentPath,
            deploymentCredentials);

        service.setUpTFModuleDirs()
            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(IllegalStateException.class);
                    assertThat(throwable.getMessage()).isEqualTo("deployRequest must not be null");
                    testContext.completeNow();
                })
            );
    }

    @ParameterizedTest
    @ValueSource(strings = {"Windows", "Linux"})
    void getDeploymentCredentials(String os, Vertx vertx, VertxTestContext testContext) {
        TerminateResourcesDAO terminateRequest = TestRequestProvider.createTerminateRequest();
        DeploymentPath deploymentPath = new DeploymentPath(1L, config);
        DeploymentCredentials deploymentCredentials = new DeploymentCredentials();
        TerraformSetupService service = new TerraformSetupService(vertx, terminateRequest, deploymentPath,
            deploymentCredentials);
        System.setProperty("os.name", os);
        String expectedEdgeOutput = os.equals("Windows") ? "openfaas_login_data={r3={auth_user=\\\"user\\\"," +
            "auth_pw=\\\"pw\\\"}}" : "openfaas_login_data={r3={auth_user=\"user\",auth_pw=\"pw\"}}";
        service.getTerminationCredentials()
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.getCloudCredentials().size()).isEqualTo(1);
                    assertThat(result.getCloudCredentials().get(0).getResourceProvider().getProvider()).isEqualTo("aws");
                    assertThat(result.getOpenFaasCredentialsString())
                        .isEqualTo(expectedEdgeOutput);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void getDeploymentCredentialsTerminateRequestNull(Vertx vertx, VertxTestContext testContext) {
        DeployResourcesDAO deployRequest = TestRequestProvider.createDeployRequest();
        DeploymentPath deploymentPath = new DeploymentPath(1L, config);
        DeploymentCredentials deploymentCredentials = new DeploymentCredentials();
        TerraformSetupService service = new TerraformSetupService(vertx, deployRequest, deploymentPath,
            deploymentCredentials);

        service.getTerminationCredentials()
            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(IllegalStateException.class);
                    assertThat(throwable.getMessage()).isEqualTo("terminateRequest must not be null");
                    testContext.completeNow();
                })
            );
    }
}
