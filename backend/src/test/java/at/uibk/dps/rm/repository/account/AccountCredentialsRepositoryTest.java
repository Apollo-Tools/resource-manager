package at.uibk.dps.rm.repository.account;

import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.testutil.integration.DatabaseTest;
import at.uibk.dps.rm.testutil.objectprovider.TestAccountProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestResourceProviderProvider;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.core.Vertx;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Implements tests for the {@link AccountCredentialsRepository} class.
 *
 * @author matthi-g
 */
public class AccountCredentialsRepositoryTest extends DatabaseTest {

    private final AccountCredentialsRepository repository = new AccountCredentialsRepository();

    @Override
    public void fillDB(Vertx vertx, VertxTestContext testContext) {
        super.fillDB(vertx, testContext);

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
