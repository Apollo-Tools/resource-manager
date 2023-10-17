package at.uibk.dps.rm.router.metric;

import at.uibk.dps.rm.testutil.integration.RouterTest;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.web.client.WebClient;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MetricRouteTest extends RouterTest {

    @Test
    void getMetric(Vertx vertx, VertxTestContext testContext) {
        WebClient client = WebClient.create(vertx);
        client.get(API_PORT, API_URL, "/api/metrics/1")
            .putHeader("Authorization", jwtAdmin)
            .send()
            .subscribe(result -> {
                if (result.statusCode() == 200) {
                    JsonObject resultBody = result.bodyAsJsonObject();
                    assertThat(resultBody.getLong("metric_id")).isEqualTo(1L);
                    assertThat(resultBody.getString("metric")).isEqualTo("availability");
                    assertThat(resultBody.getString("description")).isEqualTo("the availability of a " +
                        "resource");
                    assertThat(resultBody.getBoolean("is_slo")).isEqualTo(true);
                    assertThat(resultBody.getJsonObject("metric_type").getString("type"))
                        .isEqualTo("number");
                    testContext.completeNow();
                } else {
                    testContext.failNow("operation failed");
                }
            }, throwable -> testContext.failNow("method has thrown exception"));
    }
}
