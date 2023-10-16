package at.uibk.dps.rm.router.account;

import at.uibk.dps.rm.testutil.RouterTest;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.web.client.WebClient;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

public class AccountRouteTest extends RouterTest {

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void listAccounts(boolean isAdmin, Vertx vertx, VertxTestContext testContext) {
        WebClient client = WebClient.create(vertx);
        String token = isAdmin ? jwtAdmin : jwtDefault;
        client.get(API_PORT, API_URL, "/api/accounts")
            .putHeader("Authorization", token)
            .send()
            .subscribe(result -> {
                if (result.statusCode() == 200 && isAdmin) {
                    assertThat(result.bodyAsJsonArray().size()).isEqualTo(2);
                    assertThat(result.bodyAsJsonArray().getJsonObject(0).getString("username"))
                        .isEqualTo("user1");
                    assertThat(result.bodyAsJsonArray().getJsonObject(1).getString("username"))
                        .isEqualTo("user2");
                    testContext.completeNow();
                } else if (result.statusCode() == 403 && !isAdmin) {
                    testContext.completeNow();
                } else {
                    testContext.failNow("operation failed");
                }
            }, throwable -> testContext.failNow("method has thrown exception"));
    }
}
