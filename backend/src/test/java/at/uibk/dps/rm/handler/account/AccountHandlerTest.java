package at.uibk.dps.rm.handler.account;

import at.uibk.dps.rm.entity.model.Account;
import at.uibk.dps.rm.service.rxjava3.database.account.AccountService;
import at.uibk.dps.rm.testutil.RoutingContextMockHelper;
import at.uibk.dps.rm.testutil.objectprovider.TestAccountProvider;
import at.uibk.dps.rm.util.configuration.JWTAuthProvider;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import at.uibk.dps.rm.util.misc.PasswordUtility;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.config.ConfigRetriever;
import io.vertx.rxjava3.ext.web.RoutingContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.when;

/**
 * Implements tests for the {@link AccountHandler} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class AccountHandlerTest {

    private AccountHandler accountHandler;

    private final PasswordUtility passwordUtility = new PasswordUtility();

    private JWTAuthProvider jwtAuthProvider;

    @Mock
    private AccountService accountService;

    @Mock
    private RoutingContext rc;

    @BeforeEach
    void initTest(Vertx vertx) {
        JsonMapperConfig.configJsonMapper();
        ConfigStoreOptions fileStore = new ConfigStoreOptions()
            .setType("file")
            .setFormat("json")
            .setConfig(new JsonObject().put("path", "./conf/config.test.json"));
        ConfigRetriever retriever = ConfigRetriever.create(vertx,
            new ConfigRetrieverOptions().addStore(fileStore));
        JsonObject config = retriever.getConfig().blockingGet();
        jwtAuthProvider = new JWTAuthProvider(vertx, config.getString("jwt_algorithm"),
            config.getString("jwt_secret"), config.getInteger("token_minutes_valid"));
        accountHandler = new AccountHandler(accountService, jwtAuthProvider.getJwtAuth());
    }

    @Test
    void updateOne(VertxTestContext testContext) {
        long entityId = 1L;
        String oldPassword = "oldpassword";
        String newPassword = "newpassword";
        Account entity = TestAccountProvider.createAccount(entityId, "user",
            passwordUtility.hashPassword(oldPassword.toCharArray()));
        JsonObject requestBody = new JsonObject("{\n" +
            "\"old_password\": \"" + oldPassword + "\",\n" +
            "\"new_password\": \"" + newPassword + "\"\n" +
            "}");

        RoutingContextMockHelper.mockUserPrincipal(rc, entity);
        RoutingContextMockHelper.mockBody(rc, requestBody);
        when(accountService.update(entityId, requestBody)).thenReturn(Completable.complete());

        accountHandler.updateOne(rc)
            .blockingSubscribe(() -> {},
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
        testContext.completeNow();
    }

    @Test
    void loginValid(VertxTestContext testContext) {
        long accountId = 1L;
        String username = "user";
        String password = "password";
        Account entity = TestAccountProvider.createAccount(accountId, username, password, "default");
        JsonObject entityJson = JsonObject.mapFrom(entity);
        JsonObject requestBody = new JsonObject("{\n" +
            "\"username\": \"" + username + "\",\n" +
            "\"password\": \"" + password + "\"\n" +
            "}");

        RoutingContextMockHelper.mockBody(rc, requestBody);
        when(accountService.loginAccount(entity.getUsername(), entity.getPassword()))
            .thenReturn(Single.just(entityJson));

        accountHandler.login(rc)
            .flatMap(result -> jwtAuthProvider.getJwtAuth().authenticate(result))
            .subscribe(user -> testContext.verify(() -> {
                assertThat(user.principal().getLong("account_id")).isEqualTo(accountId);
                assertThat(user.principal().getString("username")).isEqualTo(username);
                assertThat(user.principal().getJsonArray("role").getString(0)).isEqualTo("default");
                testContext.completeNow();
            }),
            throwable -> testContext.verify(() -> fail("method has thrown exception")));
    }
}
