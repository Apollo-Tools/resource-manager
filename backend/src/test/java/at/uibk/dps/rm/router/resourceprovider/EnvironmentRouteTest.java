package at.uibk.dps.rm.router.resourceprovider;

import at.uibk.dps.rm.testutil.integration.RouterTest;
import io.vertx.core.json.JsonArray;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.web.client.WebClient;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Implements tests for the {@link EnvironmentRoute} class.
 *
 * @author matthi-g
 */
public class EnvironmentRouteTest extends RouterTest {

    @Test
    void listEnvironments(Vertx vertx, VertxTestContext testContext) {
        WebClient client = WebClient.create(vertx);
        client.get(API_PORT, API_URL, "/api/environments")
            .putHeader("Authorization", jwtAdmin)
            .send()
            .subscribe(result -> {
                if (result.statusCode() == 200) {
                    JsonArray resultBody = result.bodyAsJsonArray();
                    assertThat(resultBody.size()).isEqualTo(2);
                    assertThat(resultBody.getJsonObject(0).getLong("environment_id")).isEqualTo(1L);
                    assertThat(resultBody.getJsonObject(0).getString("environment"))
                        .isEqualTo("cloud");
                    assertThat(resultBody.getJsonObject(1).getLong("environment_id")).isEqualTo(2L);
                    assertThat(resultBody.getJsonObject(1).getString("environment"))
                        .isEqualTo("edge");
                    testContext.completeNow();
                } else {
                    testContext.failNow("operation failed");
                }
            }, throwable -> testContext.failNow("method has thrown exception"));
    }
}
