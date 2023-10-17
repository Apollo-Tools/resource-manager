package at.uibk.dps.rm.router.resourceprovider;

import at.uibk.dps.rm.testutil.integration.RouterTest;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.web.client.WebClient;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RegionRouteTest extends RouterTest {

    @Test
    void getRegion(Vertx vertx, VertxTestContext testContext) {
        WebClient client = WebClient.create(vertx);
        client.get(API_PORT, API_URL, "/api/regions/1")
            .putHeader("Authorization", jwtAdmin)
            .send()
            .subscribe(result -> {
                if (result.statusCode() == 200) {
                    JsonObject resultBody = result.bodyAsJsonObject();
                    assertThat(resultBody.getLong("region_id")).isEqualTo(1L);
                    assertThat(resultBody.getString("name")).isEqualTo("us-east-1");
                    assertThat(resultBody.getJsonObject("resource_provider")
                        .getString("provider")).isEqualTo("aws");
                    testContext.completeNow();
                } else {
                    testContext.failNow("operation failed");
                }
            }, throwable -> testContext.failNow("method has thrown exception"));
    }
}
