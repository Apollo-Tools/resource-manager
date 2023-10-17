package at.uibk.dps.rm.router.ensemble;

import at.uibk.dps.rm.entity.model.Ensemble;
import at.uibk.dps.rm.testutil.integration.RouterTest;
import at.uibk.dps.rm.testutil.objectprovider.TestEnsembleProvider;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.web.client.WebClient;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class EnsembleRouteTest extends RouterTest {

    @Override
    public void fillDB(Vertx vertx, VertxTestContext testContext) {
        super.fillDB(vertx, testContext);

        smProvider.withTransactionSingle(sessionManager -> {
            Ensemble e1 = TestEnsembleProvider.createEnsemble(null, 1L, "e1", true);
            return sessionManager.persist(e1);
        }).blockingSubscribe(res -> {}, testContext::failNow);
    }

    @Test
    void getFunctionType(Vertx vertx, VertxTestContext testContext) {
        WebClient client = WebClient.create(vertx);
        client.get(API_PORT, API_URL, "/api/ensembles/1")
            .putHeader("Authorization", jwtAdmin)
            .send()
            .subscribe(result -> {
                if (result.statusCode() == 200) {
                    JsonObject resultBody = result.bodyAsJsonObject();
                    assertThat(resultBody.getLong("ensemble_id")).isEqualTo(1L);
                    assertThat(resultBody.getString("name")).isEqualTo("e1");
                    assertThat(resultBody.getJsonArray("resources").size()).isEqualTo(0);
                    JsonArray slos = resultBody.getJsonArray("slos");
                    assertThat(slos.size()).isEqualTo(5);
                    assertThat(slos.getJsonObject(0).getString("name")).isEqualTo("environment");
                    assertThat(slos.getJsonObject(1).getString("name")).isEqualTo("resource_type");
                    assertThat(slos.getJsonObject(2).getString("name")).isEqualTo("platform");
                    assertThat(slos.getJsonObject(3).getString("name")).isEqualTo("region");
                    assertThat(slos.getJsonObject(4).getString("name")).isEqualTo("resource_provider");
                    testContext.completeNow();
                } else {
                    testContext.failNow("operation failed");
                }
            }, throwable -> testContext.failNow("method has thrown exception"));
    }
}
