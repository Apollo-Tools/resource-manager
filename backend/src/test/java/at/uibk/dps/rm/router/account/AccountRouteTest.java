package at.uibk.dps.rm.router.account;

import at.uibk.dps.rm.testutil.integration.RouterTest;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.web.client.WebClient;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

public class AccountRouteTest extends RouterTest {

    @ParameterizedTest
    @CsvSource({
        "1, 2",
        "2, 1"
    })
    void getOtherAccount(long loggedInUserId, long getUserId, Vertx vertx, VertxTestContext testContext) {
        WebClient client = WebClient.create(vertx);
        String token = loggedInUserId == 1 ? jwtAdmin : jwtDefault;
        client.get(API_PORT, API_URL, "/api/accounts/" + getUserId)
            .putHeader("Authorization", token)
            .send()
            .subscribe(result -> {
                if (result.statusCode() == 200 && loggedInUserId == 1) {
                    assertThat(result.bodyAsJsonObject().getLong("account_id")).isEqualTo(getUserId);
                    assertThat(result.bodyAsJsonObject().getString("username")).isEqualTo("user2");
                    assertThat(result.bodyAsJsonObject().containsKey("password")).isEqualTo(false);
                    testContext.completeNow();
                } else if (result.statusCode() == 403 && loggedInUserId == 2) {
                    testContext.completeNow();
                } else {
                    testContext.failNow("operation failed");
                }
            }, throwable -> testContext.failNow("method has thrown exception"));
    }

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

    @ParameterizedTest
    @CsvSource({
        "1, 2, lock",
        "1, 2, unlock",
        "2, 1, lock",
        "2, 1, unlock"
    })
    void lockAccount(long loggedInUserId, long lockUserId, String type, Vertx vertx, VertxTestContext testContext) {
        WebClient client = WebClient.create(vertx);
        String token = loggedInUserId == 1 ? jwtAdmin : jwtDefault;
        client.post(API_PORT, API_URL, "/api/accounts/" + lockUserId + '/' + type)
            .putHeader("Authorization", token)
            .send()
            .subscribe(result -> {
                if (result.statusCode() == 204 && loggedInUserId == 1) {
                    testContext.completeNow();
                } else if (result.statusCode() == 403 && loggedInUserId == 2) {
                    testContext.completeNow();
                } else {
                    testContext.failNow("operation failed");
                }
            }, throwable -> testContext.failNow("method has thrown exception"));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void signup(boolean isAdmin, Vertx vertx, VertxTestContext testContext) {
        WebClient client = WebClient.create(vertx);
        String token = isAdmin ? jwtAdmin : jwtDefault;
        JsonObject newUser = new JsonObject("{\"username\": \"newuser\", \"password\": \"Password2?\"}");
        client.post(API_PORT, API_URL, "/api/accounts/signup")
            .putHeader("Authorization", token)
            .sendJsonObject(newUser)
            .subscribe(result -> {
                if (result.statusCode() == 201 && isAdmin) {
                    JsonObject resultUser = result.bodyAsJsonObject();
                    assertThat(resultUser.getLong("account_id")).isEqualTo(3L);
                    assertThat(resultUser.getString("username")).isEqualTo("newuser");
                    assertThat(resultUser.containsKey("password")).isEqualTo(false);
                    assertThat(resultUser.getJsonObject("role").getString("role"))
                        .isEqualTo("default");
                    assertThat(resultUser.getBoolean("is_active")).isEqualTo(true);
                    testContext.completeNow();
                } else if (result.statusCode() == 403 && !isAdmin) {
                    testContext.completeNow();
                } else {
                    testContext.failNow("operation failed");
                }
            }, throwable -> testContext.failNow("method has thrown exception"));
    }
}
