package at.uibk.dps.rm.repository.account;

import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.testutil.integration.DatabaseTest;
import at.uibk.dps.rm.testutil.objectprovider.TestAccountProvider;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.core.Vertx;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;

public class AccountRepositoryTest extends DatabaseTest {

    private final AccountRepository repository = new AccountRepository();

    @Override
    public void fillDB(Vertx vertx, VertxTestContext testContext) {
        super.fillDB(vertx, testContext);

        smProvider.withTransactionSingle(sessionManager -> {
            Role defaultRole = TestAccountProvider.createRoleDefault();
            Account accountLocked = new Account();
            accountLocked.setUsername("locked");
            accountLocked.setIsActive(false);
            accountLocked.setPassword("");
            accountLocked.setRole(defaultRole);
            return sessionManager.persist(accountLocked);
        }).blockingSubscribe(res -> {}, testContext::failNow);
    }

    @ParameterizedTest
    @CsvSource({
        "1, true",
        "3, false"
    })
    void findByAccountIdAndProviderId(long accountId, boolean exists, VertxTestContext testContext) {
        smProvider.withTransactionMaybe(sessionManager -> repository.findByIdAndActive(sessionManager, accountId))
            .subscribe(result -> testContext.verify(() -> {
                if (exists) {
                    assertThat(result.getAccountId()).isEqualTo(accountId);
                    testContext.completeNow();
                } else {
                    testContext.failNow("method did not throw exception");
                }
            }), throwable -> {
                assertThat(exists).isEqualTo(false);
                assertThat(throwable.getCause()).isInstanceOf(NoSuchElementException.class);
                testContext.completeNow();
            });
    }

    @ParameterizedTest
    @CsvSource({
        "user1, 1",
        "user2, 2"
    })
    void findByUsername(String username, Long accountId, VertxTestContext testContext) {
        smProvider.withTransactionMaybe(sessionManager -> repository.findByUsername(sessionManager, username))
            .subscribe(result -> testContext.verify(() -> {
                assertThat(result.getAccountId()).isEqualTo(accountId);
                assertThat(result.getUsername()).isEqualTo(username);
                testContext.completeNow();
            }), throwable -> testContext.failNow("method has thrown exception"));
    }
}
