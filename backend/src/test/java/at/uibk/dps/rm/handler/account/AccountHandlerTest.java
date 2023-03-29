package at.uibk.dps.rm.handler.account;

import at.uibk.dps.rm.entity.model.Account;
import at.uibk.dps.rm.exception.AlreadyExistsException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.exception.UnauthorizedException;
import at.uibk.dps.rm.testutil.RoutingContextMockHelper;
import at.uibk.dps.rm.testutil.TestObjectProvider;
import at.uibk.dps.rm.util.JWTAuthProvider;
import at.uibk.dps.rm.util.JsonMapperConfig;
import at.uibk.dps.rm.util.PasswordUtility;
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

@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class AccountHandlerTest {

    private AccountHandler accountHandler;

    private final PasswordUtility passwordUtility = new PasswordUtility();

    private JWTAuthProvider jwtAuthProvider;

    @Mock
    private AccountChecker accountChecker;

    @Mock
    private RoutingContext rc;

    @BeforeEach
    void initTest(Vertx vertx) {
        JsonMapperConfig.configJsonMapper();
        ConfigStoreOptions fileStore = new ConfigStoreOptions()
            .setType("file")
            .setFormat("json")
            .setConfig(new JsonObject().put("path", "../conf/config.test.json"));
        ConfigRetriever retriever = ConfigRetriever.create(vertx,
            new ConfigRetrieverOptions().addStore(fileStore));
        JsonObject config = retriever.getConfig().blockingGet();
        jwtAuthProvider = new JWTAuthProvider(vertx, config.getString("jwt_algorithm"),
            config.getString("jwt_secret"), config.getInteger("token_minutes_valid"));
        accountHandler = new AccountHandler(accountChecker, jwtAuthProvider.getJwtAuth());
    }

    @Test
    void getOneExists(VertxTestContext testContext) {
        long accountId = 1L;
        Account entity = TestObjectProvider.createAccount(1L, "user", "password");

        RoutingContextMockHelper.mockUserPrincipal(rc, entity);
        when(accountChecker.checkFindOne(accountId)).thenReturn(Single.just(JsonObject.mapFrom(entity)));

        accountHandler.getOne(rc)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.getLong("account_id")).isEqualTo(accountId);
                    assertThat(result.getString("username")).isEqualTo("user");
                    assertThat(result.containsKey("password")).isEqualTo(false);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method did throw exception"))
            );
    }

    @Test
    void getOneNotFound(VertxTestContext testContext) {
        long accountId = 1L;
        Account account = TestObjectProvider.createAccount(1L, "user", "password");

        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        when(accountChecker.checkFindOne(accountId)).thenReturn(Single.error(NotFoundException::new));

        accountHandler.getOne(rc)
            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void postOne(VertxTestContext testContext) {
        Account entity = TestObjectProvider.createAccount(1L, "user", "password");
        JsonObject requestBody = JsonObject.mapFrom(entity);

        RoutingContextMockHelper.mockBody(rc, requestBody);
        when(accountChecker.checkForDuplicateEntity(requestBody)).thenReturn(Completable.complete());
        when(accountChecker.hashAccountPassword(requestBody)).thenReturn(requestBody);
        when(accountChecker.submitCreate(requestBody)).thenReturn(Single.just(requestBody));

        accountHandler.postOne(rc)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.getLong("account_id")).isEqualTo(1L);
                    assertThat(result.getString("username")).isEqualTo("user");
                    assertThat(result.containsKey("password")).isEqualTo(false);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method did throw exception"))
            );
    }

    @Test
    void postOneAlreadyExists(VertxTestContext testContext) {
        Account entity = TestObjectProvider.createAccount(1L, "user", "password");
        JsonObject requestBody = JsonObject.mapFrom(entity);

        RoutingContextMockHelper.mockBody(rc, requestBody);
        when(accountChecker.checkForDuplicateEntity(requestBody))
            .thenReturn(Completable.error(AlreadyExistsException::new));

        accountHandler.postOne(rc)
            .subscribe(result -> testContext.verify(() -> fail("method did throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(AlreadyExistsException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void updateOneExists(VertxTestContext testContext) {
        long entityId = 1L;
        String oldPassword = "oldpassword";
        String newPassword = "newpassword";
        Account entity = TestObjectProvider.createAccount(entityId, "user",
            passwordUtility.hashPassword(oldPassword.toCharArray()));
        JsonObject entityJson = JsonObject.mapFrom(entity);
        JsonObject requestBody = new JsonObject("{\n" +
            "\"old_password\": \"" + oldPassword + "\",\n" +
            "\"new_password\": \"" + newPassword + "\"\n" +
            "}");

        RoutingContextMockHelper.mockUserPrincipal(rc, entity);
        RoutingContextMockHelper.mockBody(rc, requestBody);
        when(accountChecker.checkFindLoginAccount(entity.getUsername())).thenReturn(Single.just(entityJson));
        when(accountChecker.checkComparePasswords(entityJson, oldPassword.toCharArray())).thenReturn(entityJson);
        when(accountChecker.hashAccountPassword(entityJson)).thenReturn(entityJson);
        when(accountChecker.submitUpdate(entityJson)).thenReturn(Completable.complete());

        accountHandler.updateOne(rc)
            .blockingSubscribe(() -> {},
                throwable -> testContext.verify(() -> fail("method did throw exception"))
            );
        testContext.completeNow();
    }

    @Test
    void updateOneNotFound(VertxTestContext testContext) {
        long entityId = 1L;
        String oldPassword = "oldpassword";
        String newPassword = "newpassword";
        Account entity = TestObjectProvider.createAccount(entityId, "user",
            passwordUtility.hashPassword(oldPassword.toCharArray()));
        JsonObject requestBody = new JsonObject("{\n" +
            "\"old_password\": \"" + oldPassword + "\",\n" +
            "\"new_password\": \"" + newPassword + "\"\n" +
            "}");

        RoutingContextMockHelper.mockUserPrincipal(rc, entity);
        RoutingContextMockHelper.mockBody(rc, requestBody);
        when(accountChecker.checkFindLoginAccount(entity.getUsername()))
            .thenReturn(Single.error(UnauthorizedException::new));

        accountHandler.updateOne(rc)
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(UnauthorizedException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void updateOneOldPasswordsNotMatching(VertxTestContext testContext) {
        long entityId = 1L;
        String oldPassword = "oldpassword";
        String newPassword = "newpassword";
        Account entity = TestObjectProvider.createAccount(entityId, "user",
            passwordUtility.hashPassword(oldPassword.toCharArray()));
        JsonObject entityJson = JsonObject.mapFrom(entity);
        JsonObject requestBody = new JsonObject("{\n" +
            "\"old_password\": \"" + oldPassword + 1234 + "\",\n" +
            "\"new_password\": \"" + newPassword + "\"\n" +
            "}");

        RoutingContextMockHelper.mockUserPrincipal(rc, entity);
        RoutingContextMockHelper.mockBody(rc, requestBody);
        when(accountChecker.checkFindLoginAccount(entity.getUsername())).thenReturn(Single.just(entityJson));
        when(accountChecker.checkComparePasswords(entityJson, requestBody.getString("old_password").toCharArray()))
            .thenThrow(new UnauthorizedException());

        accountHandler.updateOne(rc)
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(UnauthorizedException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void loginValid(VertxTestContext testContext) {
        long accountId = 1L;
        String username = "user";
        String password = "password";
        Account entity = TestObjectProvider.createAccount(accountId, username,
            passwordUtility.hashPassword(password.toCharArray()));
        JsonObject entityJson = JsonObject.mapFrom(entity);
        JsonObject requestBody = new JsonObject("{\n" +
            "\"username\": \"" + username + "\",\n" +
            "\"password\": \"" + password + "\"\n" +
            "}");

        RoutingContextMockHelper.mockBody(rc, requestBody);
        when(accountChecker.checkFindLoginAccount(entity.getUsername()))
            .thenReturn(Single.just(entityJson));
        when(accountChecker.checkComparePasswords(entityJson, requestBody.getString("password").toCharArray()))
            .thenReturn(entityJson);

        accountHandler.login(rc)
            .flatMap(result -> jwtAuthProvider.getJwtAuth().authenticate(result))
            .subscribe(user -> testContext.verify(() -> {
                assertThat(user.principal().getLong("account_id")).isEqualTo(accountId);
                assertThat(user.principal().getString("username")).isEqualTo(username);
                testContext.completeNow();
            }),
            throwable -> testContext.verify(() -> fail("method did throw exception")));
    }



    @Test
    void loginAccountNotExists(VertxTestContext testContext) {
        long accountId = 1L;
        String username = "user";
        String password = "password";
        Account entity = TestObjectProvider.createAccount(accountId, username,
            passwordUtility.hashPassword(password.toCharArray()));
        JsonObject requestBody = new JsonObject("{\n" +
            "\"username\": \"" + username + "\",\n" +
            "\"password\": \"" + password + "\"\n" +
            "}");

        RoutingContextMockHelper.mockBody(rc, requestBody);
        when(accountChecker.checkFindLoginAccount(entity.getUsername()))
            .thenReturn(Single.error(UnauthorizedException::new));

        accountHandler.login(rc)
            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(UnauthorizedException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void loginPasswordInvalid(VertxTestContext testContext) {
        long accountId = 1L;
        String username = "user";
        String password = "password";
        Account entity = TestObjectProvider.createAccount(accountId, username,
            passwordUtility.hashPassword(password.toCharArray()));
        JsonObject entityJson = JsonObject.mapFrom(entity);
        JsonObject requestBody = new JsonObject("{\n" +
            "\"username\": \"" + username + "\",\n" +
            "\"password\": \"" + password + 1234 + "\"\n" +
            "}");

        RoutingContextMockHelper.mockBody(rc, requestBody);
        when(accountChecker.checkFindLoginAccount(entity.getUsername()))
            .thenReturn(Single.just(entityJson));
        when(accountChecker.checkComparePasswords(entityJson, requestBody.getString("password").toCharArray()))
            .thenThrow(new UnauthorizedException());

        accountHandler.login(rc)
            .flatMap(result -> jwtAuthProvider.getJwtAuth().authenticate(result))
            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(UnauthorizedException.class);
                    testContext.completeNow();
                })
            );
    }
}
