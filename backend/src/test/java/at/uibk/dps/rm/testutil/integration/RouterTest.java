package at.uibk.dps.rm.testutil.integration;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * This abstract class can be used for tests which need a running instance of the resource manager.
 *
 * @author matthi-g
 */
@Testcontainers
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public abstract class RouterTest extends DatabaseTest {

    public static String jwtAdmin, jwtDefault;

    public final static String API_URL = "localhost";

    public static int API_PORT = 8888;

    /**
     * Start up the resource manager using the given vertx instance.
     *
     * @param vertx a vertx instance
     * @param testContext the test context
     */
    private void startupRM(Vertx vertx, VertxTestContext testContext) {
        String address = postgres.getHost();
        Integer port = postgres.getFirstMappedPort();
        ConfigDTO config = TestConfigProvider.getConfigDTO();
        config.setDbHost(address);
        config.setDbPort(port);
        config.setDbUser("root");
        config.setApiPort(API_PORT);
        config.setDbPassword("root");
        config.setJwtAlgorithm("HS256");
        try (MockedConstruction<ConfigUtility> ignore = Mockprovider.mockConfig(config);
                MockedStatic<Vertx> vertxMock = Mockito.mockStatic(Vertx.class)) {
            vertxMock.when(Vertx::vertx).thenReturn(vertx);
            Main.main(null);
            if (!isInitialized) {
                WebClient client = WebClient.create(vertx);
                loginAccount(client, "user1", "Password1!", true, testContext);
                loginAccount(client, "user2", "Password1!", false, testContext);
            }
        }
    }

    /**
     * Login an account.
     *
     * @param client an active web client
     * @param username the username
     * @param password the password
     * @param isAdmin whether the account is an admin
     * @param testContext the test context
     */
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

    @BeforeEach
    void initTests(Vertx vertx, VertxTestContext testContext) {
        initDB(vertx, testContext);
        startupRM(vertx, testContext);
        isInitialized = true;
        testContext.completeNow();
    }
}
