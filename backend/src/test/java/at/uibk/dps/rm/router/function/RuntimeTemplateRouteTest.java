package at.uibk.dps.rm.router.function;

import at.uibk.dps.rm.testutil.integration.RouterTest;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.web.client.WebClient;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RuntimeTemplateRouteTest extends RouterTest {

    @Test
    void getRuntime(Vertx vertx, VertxTestContext testContext) {
        WebClient client = WebClient.create(vertx);
        client.get(API_PORT, API_URL, "/api/runtimes/1/template")
            .putHeader("Authorization", jwtAdmin)
            .send()
            .subscribe(result -> {
                if (result.statusCode() == 200) {
                    JsonObject resultBody = result.bodyAsJsonObject();
                    assertThat(resultBody.getString("template"))
                        .startsWith("#########Function implementation#############");
                    testContext.completeNow();
                } else {
                    testContext.failNow("operation failed");
                }
            }, throwable -> testContext.failNow("method has thrown exception"));
    }
}
