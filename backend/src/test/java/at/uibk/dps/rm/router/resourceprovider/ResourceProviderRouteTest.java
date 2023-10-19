package at.uibk.dps.rm.router.resourceprovider;

import at.uibk.dps.rm.testutil.integration.RouterTest;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.web.client.WebClient;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Implements tests for the {@link ResourceProviderRoute} class.
 *
 * @author matthi-g
 */
public class ResourceProviderRouteTest extends RouterTest {

    @Test
    void getResourceProvider(Vertx vertx, VertxTestContext testContext) {
        WebClient client = WebClient.create(vertx);
        client.get(API_PORT, API_URL, "/api/resource-providers/1")
            .putHeader("Authorization", jwtAdmin)
            .send()
            .subscribe(result -> {
                if (result.statusCode() == 200) {
                    JsonObject resultBody = result.bodyAsJsonObject();
                    assertThat(resultBody.getLong("provider_id")).isEqualTo(1L);
                    assertThat(resultBody.getString("provider")).isEqualTo("aws");
                    assertThat(resultBody.getJsonObject("environment").getString("environment"))
                        .isEqualTo("cloud");
                    testContext.completeNow();
                } else {
                    testContext.failNow("operation failed");
                }
            }, throwable -> testContext.failNow("method has thrown exception"));
    }
}
