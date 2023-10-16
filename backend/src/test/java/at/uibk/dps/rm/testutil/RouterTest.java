package at.uibk.dps.rm.testutil;

import at.uibk.dps.rm.Main;
import at.uibk.dps.rm.entity.dto.config.ConfigDTO;
import at.uibk.dps.rm.testutil.mockprovider.Mockprovider;
import at.uibk.dps.rm.testutil.objectprovider.TestConfigProvider;
import at.uibk.dps.rm.util.configuration.ConfigUtility;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.web.client.WebClient;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public abstract class RouterTest extends DatabaseTest {

    public static String jwtAdmin, jwtDefault;

    public final static String API_URL = "localhost";

    public final static int API_PORT = 8888;

    @Override
    public void fillDB(Vertx vertx, VertxTestContext testContext) {
        super.fillDB(vertx, testContext);
        String address = postgres.getHost();
        Integer port = postgres.getFirstMappedPort();
        ConfigDTO config = TestConfigProvider.getConfigDTO();
        config.setDbHost(address);
        config.setDbPort(port);
        config.setDbUser("root");
        config.setDbPassword("root");
        config.setJwtAlgorithm("HS256");
        try (MockedConstruction<ConfigUtility> ignore = Mockprovider.mockConfig(config)) {
            Main.main(null);
            WebClient client = WebClient.create(vertx);
            loginAccount(client, "user1", "Password1!", true, testContext);
            loginAccount(client, "user2", "Password1!", false, testContext);
        }
    }

    private void loginAccount(WebClient client, String username, String password, boolean isAdmin,
            VertxTestContext testContext) {
        JsonObject login = new JsonObject();
        login.put("username", username);
        login.put("password", password);
        client.post(API_PORT, API_URL, "/api/accounts/login")
            .sendJsonObject(login)
            .blockingSubscribe(result -> {
                if (result.statusCode() != 200) {
                    testContext.failNow("login failed");
                }
                if (isAdmin) {
                    jwtAdmin = "Bearer " + result.bodyAsJsonObject().getString("token");
                } else {
                    jwtDefault = "Bearer " + result.bodyAsJsonObject().getString("token");
                }
            }, throwable -> testContext.failNow("login failed"));
    }
}
