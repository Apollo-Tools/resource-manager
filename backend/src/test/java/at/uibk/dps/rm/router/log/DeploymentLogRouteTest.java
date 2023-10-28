package at.uibk.dps.rm.router.log;

import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.testutil.integration.RouterTest;
import at.uibk.dps.rm.testutil.objectprovider.TestDeploymentProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestLogProvider;
import io.vertx.core.json.JsonArray;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.web.client.WebClient;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Implements tests for the {@link DeploymentLogRoute} class.
 *
 * @author matthi-g
 */
public class DeploymentLogRouteTest extends RouterTest {

    @Override
    public void fillDB(Vertx vertx, VertxTestContext testContext) {
        super.fillDB(vertx, testContext);

        smProvider.withTransactionSingle(sessionManager -> {
            Deployment d1 = TestDeploymentProvider.createDeployment(null, accountAdmin);
            Log l1 = TestLogProvider.createLog(null);
            Log l2 = TestLogProvider.createLog(null);
            DeploymentLog dl1 = TestLogProvider.createDeploymentLog(null, d1, l1);
            DeploymentLog dl2 = TestLogProvider.createDeploymentLog(null, d1, l2);
            return sessionManager.persist(d1)
                .flatMap(res -> sessionManager.persist(l1))
                .flatMap(res -> sessionManager.persist(l2))
                .flatMap(res -> sessionManager.persist(dl1))
                .flatMap(res -> sessionManager.persist(dl2));
        }).blockingSubscribe(res -> {}, testContext::failNow);
    }

    @Test
    void listDeploymentLogs(Vertx vertx, VertxTestContext testContext) {
        WebClient client = WebClient.create(vertx);
        client.get(API_PORT, API_URL, "/api/deployments/1/logs")
            .putHeader("Authorization", jwtAdmin)
            .send()
            .subscribe(result -> {
                if (result.statusCode() == 200) {
                    JsonArray resultBody = result.bodyAsJsonArray();
                    assertThat(resultBody.size()).isEqualTo(2);
                    assertThat(resultBody.getJsonObject(0).getLong("log_id"))
                        .isEqualTo(1L);
                    assertThat(resultBody.getJsonObject(1).getLong("log_id"))
                        .isEqualTo(2L);
                    testContext.completeNow();
                } else {
                    testContext.failNow("operation failed");
                }
            }, throwable -> testContext.failNow("method has thrown exception"));
    }
}
