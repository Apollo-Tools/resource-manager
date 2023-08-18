package at.uibk.dps.rm.handler.deployment;

import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.testutil.RoutingContextMockHelper;
import at.uibk.dps.rm.testutil.objectprovider.*;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.ext.web.RoutingContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.when;

/**
 * Implements tests for the {@link DeploymentHandler} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class DeploymentHandlerTest {
    private DeploymentHandler deploymentHandler;

    @Mock
    private DeploymentChecker deploymentChecker;

    @Mock
    private RoutingContext rc;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        deploymentHandler = new DeploymentHandler(deploymentChecker);
    }

    @Test
    void postOneToAccount(VertxTestContext testContext) {
        Account account = TestAccountProvider.createAccount(10L);
        JsonObject requestBody = new JsonObject();
        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        RoutingContextMockHelper.mockBody(rc, requestBody);

        when(deploymentChecker.submitCreate(10L, requestBody))
            .thenReturn(Single.just(JsonObject.mapFrom(TestDeploymentProvider.createDeployment(1L))));

        deploymentHandler.postOneToAccount(rc)
            .subscribe(result -> testContext.verify(() -> {
                assertThat(result.getLong("deployment_id")).isEqualTo(1L);
                testContext.completeNow();
            }), throwable -> testContext.verify(() -> fail("method has thrown exception")));
    }

    @Test
    void cancelDeployment(VertxTestContext testContext) {
        Account account = TestAccountProvider.createAccount(10L);
        long deploymentId = 1L;
        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        when(rc.pathParam("id")).thenReturn(String.valueOf(deploymentId));
        when(deploymentChecker.submitCancelDeployment(1L, 10L))
            .thenReturn(Single.just(JsonObject.mapFrom(TestRequestProvider.createTerminateRequest())));

        deploymentHandler.cancelDeployment(rc)
            .subscribe(result -> testContext.verify(() -> {
                assertThat(result.getJsonObject("deployment").getLong("deployment_id"))
                    .isEqualTo(1L);
                testContext.completeNow();
            }), throwable -> testContext.verify(() -> fail("method has thrown exception")));
    }
}
