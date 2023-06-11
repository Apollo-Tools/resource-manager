package at.uibk.dps.rm.handler.deployment;

import at.uibk.dps.rm.entity.model.FunctionDeployment;
import at.uibk.dps.rm.entity.model.Deployment;
import at.uibk.dps.rm.service.rxjava3.database.deployment.FunctionDeploymentService;
import at.uibk.dps.rm.testutil.objectprovider.TestDeploymentProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestFunctionProvider;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.when;

/**
 * Implements tests for the {@link FunctionDeploymentChecker} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class FunctionDeploymentCheckerTest {

    private FunctionDeploymentChecker checker;

    @Mock
    private FunctionDeploymentService service;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        checker = new FunctionDeploymentChecker(service);
    }



    @Test
    void checkFindAll(VertxTestContext testContext) {
        Deployment deployment = TestDeploymentProvider.createDeployment(1L);
        FunctionDeployment fr1 = TestFunctionProvider.createFunctionDeployment(1L, deployment);
        FunctionDeployment fr2 = TestFunctionProvider.createFunctionDeployment(2L, deployment);
        JsonArray deployments = new JsonArray(List.of(JsonObject.mapFrom(fr1), JsonObject.mapFrom(fr2)));

        when(service.findAllByDeploymentId(deployment.getDeploymentId())).thenReturn(Single.just(deployments));

        checker.checkFindAllByDeploymentId(deployment.getDeploymentId())
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.size()).isEqualTo(2);
                    assertThat(result.getJsonObject(0).getLong("resource_deployment_id")).isEqualTo(1L);
                    assertThat(result.getJsonObject(1).getLong("resource_deployment_id")).isEqualTo(2L);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void checkFindAllEmptyList(VertxTestContext testContext) {
        Deployment deployment = TestDeploymentProvider.createDeployment(1L);
        JsonArray deployments = new JsonArray();

        when(service.findAllByDeploymentId(deployment.getDeploymentId())).thenReturn(Single.just(deployments));

        checker.checkFindAllByDeploymentId(deployment.getDeploymentId())
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.size()).isEqualTo(0);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }
}
