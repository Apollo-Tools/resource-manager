package at.uibk.dps.rm.handler.deployment;

import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.service.rxjava3.database.deployment.DeploymentService;
import at.uibk.dps.rm.testutil.SingleHelper;
import at.uibk.dps.rm.testutil.objectprovider.TestAccountProvider;
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
 * Implements tests for the {@link DeploymentChecker} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class DeploymentCheckerTest {

    private DeploymentChecker deploymentChecker;

    @Mock
    private DeploymentService deploymentService;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        deploymentChecker = new DeploymentChecker(deploymentService);
    }

    @Test
    void checkFindAll(VertxTestContext testContext) {
        Account account = TestAccountProvider.createAccount(1L);
        Deployment r1 = TestReservationProvider.createReservation(1L, true, account);
        Deployment r2 = TestReservationProvider.createReservation(2L, true, account);
        Deployment r3 = TestReservationProvider.createReservation(3L, true, account);
        JsonArray deployments = new JsonArray(List.of(JsonObject.mapFrom(r1),
            JsonObject.mapFrom(r2), JsonObject.mapFrom(r3)));

        when(deploymentService.findAllByAccountId(account.getAccountId())).thenReturn(Single.just(deployments));

        deploymentChecker.checkFindAll(account.getAccountId())
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.size()).isEqualTo(3);
                    assertThat(result.getJsonObject(0).getLong("deployment_id")).isEqualTo(1L);
                    assertThat(result.getJsonObject(1).getLong("deployment_id")).isEqualTo(2L);
                    assertThat(result.getJsonObject(2).getLong("deployment_id")).isEqualTo(3L);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void checkFindAllNotFound(VertxTestContext testContext) {
        Account account = TestAccountProvider.createAccount(1L);
        Single<JsonArray> handler = SingleHelper.getEmptySingle();

        when(deploymentService.findAllByAccountId(account.getAccountId())).thenReturn(handler);

        deploymentChecker.checkFindAll(account.getAccountId())
            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
            }));
    }

    @Test
    void checkFindOne(VertxTestContext testContext) {
        long accountId = 2L, deploymentId = 1L;
        Deployment r1 = TestReservationProvider.createReservation(deploymentId);

        when(deploymentService.findOneByIdAndAccountId(deploymentId, accountId))
            .thenReturn(Single.just(JsonObject.mapFrom(r1)));

        deploymentChecker.checkFindOne(deploymentId, accountId)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.getLong("deployment_id")).isEqualTo(1L);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void checkFindOneNotFound(VertxTestContext testContext) {
        long accountId = 2L, deploymentId = 1L;
        Single<JsonObject> handler = SingleHelper.getEmptySingle();

        when(deploymentService.findOneByIdAndAccountId(deploymentId, accountId))
            .thenReturn(handler);

        deploymentChecker.checkFindOne(deploymentId, accountId)
            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                }));
    }

    @Test
    void submitCreateDeployment(VertxTestContext testContext) {
        long accountId = 1L;
        Deployment deployment = new Deployment();
        deployment.setIsActive(true);
        Account account = new Account();
        account.setAccountId(accountId);
        deployment.setCreatedBy(account);
        JsonObject persistedDeployment = JsonObject.mapFrom(TestReservationProvider.createReservation(1L));

        when(deploymentService.save(JsonObject.mapFrom(deployment)))
            .thenReturn(Single.just(persistedDeployment));

        deploymentChecker.submitCreateDeployment(accountId)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.getLong("deployment_id")).isEqualTo(1L);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }
}
