package at.uibk.dps.rm.repository.account;

import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.testutil.DatabaseTest;
import at.uibk.dps.rm.testutil.objectprovider.TestAccountProvider;
import io.reactivex.rxjava3.core.Maybe;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

public class AccountRepositoryTest extends DatabaseTest {

    private final AccountRepository repository = new AccountRepository();

    @Override
    public void fillDB(VertxTestContext testContext) {
        super.fillDB(testContext);

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
        smProvider.withTransactionMaybe(sessionManager -> repository.findByIdAndActive(sessionManager, accountId)
                .switchIfEmpty(Maybe.fromAction(() -> {
                    assertThat(exists).isEqualTo(false);
                    testContext.completeNow();
                }))
                .map(result -> {
                    assertThat(result.getAccountId()).isEqualTo(accountId);
                    testContext.completeNow();
                    return result;
                })
            ).subscribe();
    }

    @ParameterizedTest
    @CsvSource({
        "user1, 1",
        "user2, 2"
    })
    void findByUsername(String username, Long accountId, VertxTestContext testContext) {
        smProvider.withTransactionMaybe(sessionManager -> repository.findByUsername(sessionManager, username)
                .map(result -> {
                    assertThat(result.getAccountId()).isEqualTo(accountId);
                    assertThat(result.getUsername()).isEqualTo(username);
                    testContext.completeNow();
                    return result;
                })
            ).subscribe();
    }
}
