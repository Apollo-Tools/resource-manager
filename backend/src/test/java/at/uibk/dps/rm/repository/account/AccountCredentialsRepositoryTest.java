package at.uibk.dps.rm.repository.account;

import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.testutil.DatabaseTest;
import at.uibk.dps.rm.testutil.objectprovider.TestAccountProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestResourceProviderProvider;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AccountCredentialsRepositoryTest extends DatabaseTest {

    private final AccountCredentialsRepository repository = new AccountCredentialsRepository();

    @Override
    public void fillDB(VertxTestContext testContext) {
        super.fillDB(testContext);

        smProvider.withTransactionSingle(sessionManager -> {
            ResourceProvider rp1 = TestResourceProviderProvider.createResourceProvider(1L);
            Credentials c1 = TestAccountProvider.createCredentials(null, rp1, "1",
                "2", "3");
            AccountCredentials ac1 = TestAccountProvider
                .createAccountCredentials(null, accountAdmin, c1);
            return sessionManager.persist(c1)
                .flatMap(result -> sessionManager.persist(ac1));
        }).blockingSubscribe(res -> {}, testContext::failNow);
    }

    @Test
    void findByAccountAndProviderId(VertxTestContext testContext) {
        smProvider.withTransactionMaybe(sessionManager -> repository
                .findByAccountAndProvider(sessionManager, 1L, 1L))
            .subscribe(result -> testContext.verify(() -> {
                assertThat(result.getAccountCredentialsId()).isEqualTo(1L);
                assertThat(result.getAccount().getAccountId()).isEqualTo(1L);
                assertThat(result.getCredentials().getCredentialsId()).isEqualTo(1L);
                testContext.completeNow();
            }), throwable -> testContext.failNow("method has thrown exception"));
    }
}
