package at.uibk.dps.rm.handler.account;

import at.uibk.dps.rm.entity.model.Account;
import at.uibk.dps.rm.exception.AlreadyExistsException;
import at.uibk.dps.rm.exception.UnauthorizedException;
import at.uibk.dps.rm.service.rxjava3.database.account.AccountService;
import at.uibk.dps.rm.testutil.SingleHelper;
import at.uibk.dps.rm.testutil.objectprovider.TestAccountProvider;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import at.uibk.dps.rm.util.misc.PasswordUtility;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Implements tests for the {@link AccountChecker} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class AccountCheckerTest {

    AccountChecker accountChecker;

    @Mock
    AccountService accountService;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        accountChecker = new AccountChecker(accountService);
    }

    @Test
    void checkFindLoginAccountExists(VertxTestContext testContext) {
        String username = "user";
        Account account = TestAccountProvider.createAccount(1L, username, "password");

        when(accountService.findOneByUsername(username))
            .thenReturn(Single.just(JsonObject.mapFrom(account)));

        accountChecker.checkLoginAccount(username)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.getLong("account_id")).isEqualTo(1L);
                    assertThat(result.getString("username")).isEqualTo(username);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void checkFindLoginAccountNotExists(VertxTestContext testContext) {
        String username = "user";
        Single<JsonObject> handler = SingleHelper.getEmptySingle();

        when(accountService.findOneByUsername(username)).thenReturn(handler);

        accountChecker.checkLoginAccount(username)
            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(UnauthorizedException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void checkForDuplicateEntityFalse(VertxTestContext testContext) {
        String username = "user";
        Account account = TestAccountProvider.createAccount(1L, username, "password");

        when(accountService.existsOneByUsername(username, false))
            .thenReturn(Single.just(false));

        accountChecker.checkForDuplicateEntity(JsonObject.mapFrom(account))
            .blockingSubscribe(() -> {
                },
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );

        verify(accountService).existsOneByUsername(username, false);
        testContext.completeNow();
    }

    @Test
    void checkForDuplicateEntityTrue(VertxTestContext testContext) {
        String username = "user";
        Account account = TestAccountProvider.createAccount(1L, username, "password");

        when(accountService.existsOneByUsername(username, false))
            .thenReturn(Single.just(true));

        accountChecker.checkForDuplicateEntity(JsonObject.mapFrom(account))
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(AlreadyExistsException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void checkComparePasswordsValid() {
        String givenPassword = "password";
        PasswordUtility passwordUtility = new PasswordUtility();
        String hashPassword = passwordUtility.hashPassword(givenPassword.toCharArray());
        Account account = TestAccountProvider.createAccount(1L, "user", hashPassword);

        assertThatNoException().isThrownBy(() -> accountChecker
            .checkComparePasswords(JsonObject.mapFrom(account), givenPassword.toCharArray()));
    }

    @Test
    void checkComparePasswordsInvalid() {
        String givenPassword = "password";
        PasswordUtility passwordUtility = new PasswordUtility();
        String hashPassword = passwordUtility.hashPassword(givenPassword.toCharArray());
        Account account = TestAccountProvider.createAccount(1L, "user", hashPassword);

        assertThrows(UnauthorizedException.class, () -> accountChecker
            .checkComparePasswords(JsonObject.mapFrom(account), (givenPassword + 1).toCharArray()));
    }

    @Test
    void hashAccountPassword() {
        PasswordUtility passwordUtility = new PasswordUtility();
        String givenPassword = "password";
        Account account = TestAccountProvider.createAccount(1L, "user", givenPassword);

        JsonObject result = accountChecker.hashAccountPassword(JsonObject.mapFrom(account));

        assertThat(result.getLong("account_id")).isEqualTo(1L);
        assertThat(passwordUtility.verifyPassword(result.getString("password"),
            givenPassword.toCharArray())).isEqualTo(true);
    }
}
