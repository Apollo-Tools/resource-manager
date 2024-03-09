package at.uibk.dps.rm.handler.account;

import at.uibk.dps.rm.entity.model.Account;
import at.uibk.dps.rm.exception.BadInputException;
import at.uibk.dps.rm.service.rxjava3.database.account.AccountService;
import at.uibk.dps.rm.testutil.RoutingContextMockHelper;
import at.uibk.dps.rm.testutil.objectprovider.TestAccountProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestConfigProvider;
import at.uibk.dps.rm.util.configuration.JWTAuthProvider;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import at.uibk.dps.rm.util.misc.PasswordUtility;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.authentication.TokenCredentials;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.web.RoutingContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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

    private long accountId;
    private Account account, accountAdmin;
    private String currPassword, newPassword;

    @BeforeEach
    void initTest(Vertx vertx) {
        JsonMapperConfig.configJsonMapper();
        JsonObject config = JsonObject.mapFrom(TestConfigProvider.getConfigDTO());
        jwtAuthProvider = new JWTAuthProvider(vertx, config.getString("jwt_algorithm"),
            config.getString("jwt_secret"), config.getInteger("token_minutes_valid"));
        accountHandler = new AccountHandler(accountService, jwtAuthProvider.getJwtAuth());
        accountId = 1L;
        currPassword = "oldpassword";
        newPassword = "newpassword";
        account = TestAccountProvider.createAccount(accountId, "user1",
            passwordUtility.hashPassword(currPassword.toCharArray()), "default");
        accountAdmin = TestAccountProvider.createAccount(2L, "user2",
            passwordUtility.hashPassword(currPassword.toCharArray()), "admin");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getOne(boolean userPrincipal, VertxTestContext testContext) {
        if (userPrincipal) {
            RoutingContextMockHelper.mockUserPrincipal(rc, account);
        } else {
            when(rc.pathParam("id")).thenReturn(String.valueOf(accountId));
        }
        when(accountService.findOne(accountId)).thenReturn(Single.just(JsonObject.mapFrom(account)));

        accountHandler.getOne(rc, userPrincipal)
            .subscribe(result -> {
                    assertThat(result.getLong("account_id")).isEqualTo(1L);
                    testContext.completeNow();
                },
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void updateOne(VertxTestContext testContext) {
        JsonObject requestBody = new JsonObject("{\n" +
            "\"old_password\": \"" + currPassword + "\",\n" +
            "\"new_password\": \"" + newPassword + "\"\n" +
            "}");

        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        RoutingContextMockHelper.mockBody(rc, requestBody);
        when(accountService.update(accountId, requestBody)).thenReturn(Completable.complete());

        accountHandler.updateOne(rc)
            .blockingSubscribe(() -> {},
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
        testContext.completeNow();
    }

    @Test
    void loginValid(VertxTestContext testContext) {
        JsonObject entityJson = JsonObject.mapFrom(account);
        JsonObject requestBody = new JsonObject("{\n" +
            "\"username\": \"" + account.getUsername() + "\",\n" +
            "\"password\": \"" + currPassword + "\"\n" +
            "}");

        RoutingContextMockHelper.mockBody(rc, requestBody);
        when(accountService.loginAccount(account.getUsername(), currPassword))
            .thenReturn(Single.just(entityJson));

        accountHandler.login(rc)
            .flatMap(result -> {
                TokenCredentials credentials = new TokenCredentials(result);
                return jwtAuthProvider.getJwtAuth().authenticate(credentials);
            })
            .subscribe(user -> testContext.verify(() -> {
                assertThat(user.principal().getLong("account_id")).isEqualTo(accountId);
                assertThat(user.principal().getString("username")).isEqualTo(account.getUsername());
                assertThat(user.principal().getJsonArray("role").getString(0)).isEqualTo("default");
                testContext.completeNow();
            }),
            throwable -> testContext.verify(() -> fail("method has thrown exception")));
    }

    @Test
    void lockAccount(VertxTestContext testContext) {
        when(rc.pathParam("id")).thenReturn(String.valueOf(accountId));
        RoutingContextMockHelper.mockUserPrincipal(rc, accountAdmin);
        when(accountService.setAccountActive(accountId, false)).thenReturn(Completable.complete());

        accountHandler.lockAccount(rc)
            .blockingSubscribe(() -> {},
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
        testContext.completeNow();
    }

    @Test
    void lockAccountSelf(VertxTestContext testContext) {
        when(rc.pathParam("id")).thenReturn(String.valueOf(accountId));
        RoutingContextMockHelper.mockUserPrincipal(rc, account);

        accountHandler.lockAccount(rc)
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(BadInputException.class);
                    assertThat(throwable.getMessage()).isEqualTo("can't lock own account");
                    testContext.completeNow();
                })
            );
    }

    @Test
    void unlockAccount(VertxTestContext testContext) {
        when(rc.pathParam("id")).thenReturn(String.valueOf(accountId));
        when(accountService.setAccountActive(accountId, true)).thenReturn(Completable.complete());

        accountHandler.unlockAccount(rc)
            .blockingSubscribe(() -> {},
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
        testContext.completeNow();
    }
}
