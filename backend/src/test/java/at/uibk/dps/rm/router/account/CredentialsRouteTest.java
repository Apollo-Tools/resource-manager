package at.uibk.dps.rm.router.account;

import at.uibk.dps.rm.testutil.integration.RouterTest;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.web.client.WebClient;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Implements tests for the {@link CredentialsRoute} class.
 *
 * @author matthi-g
 */
public class CredentialsRouteTest extends RouterTest {

    @Test
    void addCredentials(Vertx vertx, VertxTestContext testContext) {
        WebClient client = WebClient.create(vertx);
        JsonObject newCredentials = new JsonObject("{\"resource_provider\": {\"provider_id\": 1},  " +
            "\"access_key\": \"accesskey1234\", \"secret_access_key\": \"secretaccesskey1234\", \"session_token\": " +
            "\"sessiontoken1234\"}");
        client.post(API_PORT, API_URL, "/api/credentials")
            .putHeader("Authorization", jwtAdmin)
            .sendJsonObject(newCredentials)
            .subscribe(result -> {
                if (result.statusCode() == 201) {
                    JsonObject resultBody = result.bodyAsJsonObject();
                    assertThat(resultBody.getLong("credentials_id")).isEqualTo(1L);
                    assertThat(resultBody.getString("access_key")).isEqualTo("accesskey1234");
                    assertThat(resultBody.getString("secret_access_key")).isEqualTo("secretaccesskey1234");
                    assertThat(resultBody.getString("session_token")).isEqualTo("sessiontoken1234");
                    assertThat(resultBody.getJsonObject("resource_provider").getLong("provider_id"))
                        .isEqualTo(1L);
                    testContext.completeNow();
                } else {
                    testContext.failNow("operation failed");
                }
            }, throwable -> testContext.failNow("method has thrown exception"));
    }
}
