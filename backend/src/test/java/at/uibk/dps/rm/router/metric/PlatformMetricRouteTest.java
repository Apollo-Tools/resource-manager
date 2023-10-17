package at.uibk.dps.rm.router.metric;

import at.uibk.dps.rm.testutil.integration.RouterTest;
import io.vertx.core.json.JsonArray;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.web.client.WebClient;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PlatformMetricRouteTest extends RouterTest {

    @Test
    void listPlatformMetrics(Vertx vertx, VertxTestContext testContext) {
        WebClient client = WebClient.create(vertx);
        client.get(API_PORT, API_URL, "/api/platforms/1/metrics")
            .putHeader("Authorization", jwtAdmin)
            .send()
            .subscribe(result -> {
                if (result.statusCode() == 200) {
                    JsonArray resultBody = result.bodyAsJsonArray();
                    assertThat(resultBody.size()).isEqualTo(6);
                    testContext.completeNow();
                } else {
                    testContext.failNow("operation failed");
                }
            }, throwable -> testContext.failNow("method has thrown exception"));
    }
}
