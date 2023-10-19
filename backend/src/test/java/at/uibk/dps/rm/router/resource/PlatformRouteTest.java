package at.uibk.dps.rm.router.resource;

import at.uibk.dps.rm.testutil.integration.RouterTest;
import io.vertx.core.json.JsonArray;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.web.client.WebClient;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Implements tests for the {@link PlatformRoute} class.
 *
 * @author matthi-g
 */
public class PlatformRouteTest extends RouterTest {

    @Test
    void listPlatforms(Vertx vertx, VertxTestContext testContext) {
        WebClient client = WebClient.create(vertx);
        client.get(API_PORT, API_URL, "/api/platforms")
            .putHeader("Authorization", jwtAdmin)
            .send()
            .subscribe(result -> {
                if (result.statusCode() == 200) {
                    JsonArray resultBody = result.bodyAsJsonArray();
                    assertThat(resultBody.size()).isEqualTo(4);
                    assertThat(resultBody.getJsonObject(0).getString("platform")).isEqualTo("lambda");
                    assertThat(resultBody.getJsonObject(1).getString("platform")).isEqualTo("ec2");
                    assertThat(resultBody.getJsonObject(2).getString("platform"))
                        .isEqualTo("openfaas");
                    assertThat(resultBody.getJsonObject(3).getString("platform")).isEqualTo("k8s");
                    testContext.completeNow();
                } else {
                    testContext.failNow("operation failed");
                }
            }, throwable -> testContext.failNow("method has thrown exception"));
    }
}
