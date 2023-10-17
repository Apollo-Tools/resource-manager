package at.uibk.dps.rm.router.deployment;

import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.testutil.integration.RouterTest;
import at.uibk.dps.rm.testutil.objectprovider.*;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.web.client.WebClient;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ResourceDeploymentRouteTest extends RouterTest {

    @Override
    public void fillDB(Vertx vertx, VertxTestContext testContext) {
        super.fillDB(vertx, testContext);

        smProvider.withTransactionSingle(sessionManager -> {
            Deployment d1 = TestDeploymentProvider.createDeployment(null, true, accountAdmin);
            return sessionManager.persist(d1);
        }).blockingSubscribe(res -> {}, testContext::failNow);
    }

    @Test
    void startResourceDeployment(Vertx vertx, VertxTestContext testContext) {
        WebClient client = WebClient.create(vertx);
        client.post(API_PORT, API_URL, "/api/deployments/1/2/startup")
            .putHeader("Authorization", jwtAdmin)
            .send()
            .subscribe(result -> {
                if (result.statusCode() == 404) {
                    assertThat(result.bodyAsString()).isEqualTo("ServiceDeployment not found");
                    testContext.completeNow();
                } else {
                    testContext.failNow("operation failed");
                }
            }, throwable -> testContext.failNow("method has thrown exception"));
    }
}
