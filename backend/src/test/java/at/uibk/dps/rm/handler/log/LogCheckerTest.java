package at.uibk.dps.rm.handler.log;

import at.uibk.dps.rm.entity.model.Deployment;
import at.uibk.dps.rm.entity.model.DeploymentLog;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.service.rxjava3.database.log.LogService;
import at.uibk.dps.rm.testutil.SingleHelper;
import at.uibk.dps.rm.testutil.objectprovider.TestLogProvider;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.when;

/**
 * Implements tests for the {@link LogChecker} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class LogCheckerTest {

    private LogChecker logChecker;

    @Mock
    private LogService logService;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        logChecker = new LogChecker(logService);
    }

    @Test
    void checkFindAllByDeploymentId(VertxTestContext testContext) {
        long deploymentId = 11L, accountId = 22L;
        Deployment deployment = TestReservationProvider.createReservation(deploymentId);
        DeploymentLog rl1 = TestLogProvider.createReservationLog(1L, deployment);
        DeploymentLog rl2 = TestLogProvider.createReservationLog(2L, deployment);
        JsonArray deploymentLogs = new JsonArray(List.of(JsonObject.mapFrom(rl1), JsonObject.mapFrom(rl2)));

        when(logService.findAllByDeploymentIdAndAccountId(deploymentId, accountId))
            .thenReturn(Single.just(deploymentLogs));

        logChecker.checkFindAllByDeploymentId(deploymentId, accountId)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.size()).isEqualTo(2);
                    assertThat(result.getJsonObject(0).getLong("deployment_log_id")).isEqualTo(1L);
                    assertThat(result.getJsonObject(1).getLong("deployment_log_id")).isEqualTo(2L);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void checkFindAllByDeploymentIdNotFound(VertxTestContext testContext) {
        long deploymentId = 11L, accountId = 22L;
        Single<JsonArray> handler = SingleHelper.getEmptySingle();

        when(logService.findAllByDeploymentIdAndAccountId(deploymentId, accountId))
            .thenReturn(handler);

        logChecker.checkFindAllByDeploymentId(deploymentId, accountId)
            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                }));
    }
}
