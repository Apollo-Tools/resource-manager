package at.uibk.dps.rm.handler.account;

import at.uibk.dps.rm.entity.model.Account;
import at.uibk.dps.rm.service.rxjava3.database.account.AccountService;
import at.uibk.dps.rm.testutil.objectprovider.TestAccountProvider;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
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
        String username = "user", password = "password";
        Account account = TestAccountProvider.createAccount(1L, username, password);

        when(accountService.loginAccount(username, password))
            .thenReturn(Single.just(JsonObject.mapFrom(account)));

        accountChecker.checkLoginAccount(username, password)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.getLong("account_id")).isEqualTo(1L);
                    assertThat(result.getString("username")).isEqualTo(username);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }
}
