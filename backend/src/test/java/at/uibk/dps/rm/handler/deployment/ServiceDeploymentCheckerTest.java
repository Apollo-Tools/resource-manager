package at.uibk.dps.rm.handler.deployment;

import at.uibk.dps.rm.entity.model.Deployment;
import at.uibk.dps.rm.entity.model.ServiceDeployment;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.service.rxjava3.database.deployment.ServiceDeploymentService;
import at.uibk.dps.rm.testutil.objectprovider.TestReservationProvider;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.when;

/**
 * Implements tests for the {@link ServiceDeploymentChecker} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class ServiceDeploymentCheckerTest {

    private ServiceDeploymentChecker checker;

    @Mock
    private ServiceDeploymentService service;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        checker = new ServiceDeploymentChecker(service);
    }

    @Test
    void checkFindAll(VertxTestContext testContext) {
        Deployment deployment = TestReservationProvider.createReservation(1L);
        ServiceDeployment sd1 = TestReservationProvider.createServiceReservation(1L, deployment);
        ServiceDeployment sd2 = TestReservationProvider.createServiceReservation(2L, deployment);
        JsonArray deployments = new JsonArray(List.of(JsonObject.mapFrom(sd1), JsonObject.mapFrom(sd2)));

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
        Deployment deployment = TestReservationProvider.createReservation(1L);
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

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void checkReadyForStartup(boolean isReady, VertxTestContext testContext) {
        long deploymentId = 1L, resourceDeploymentId = 2L, accountId = 3L;

        when(service.existsReadyForContainerStartupAndTermination(deploymentId, resourceDeploymentId, accountId))
            .thenReturn(Single.just(isReady));

        checker.checkReadyForStartup(deploymentId, resourceDeploymentId, accountId)
            .blockingSubscribe(() -> testContext.verify(() -> {
                    if (!isReady) {
                        fail("method did not throw exception");
                    }
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> {
                    if (isReady) {
                        fail("method has thrown exception");
                    } else {
                        assertThat(throwable).isInstanceOf(NotFoundException.class);
                    }

                })
            );

        testContext.completeNow();
    }
}
