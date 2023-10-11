package at.uibk.dps.rm.repository.account;

import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.testutil.DatabaseTest;
import at.uibk.dps.rm.testutil.objectprovider.*;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AccountNamespaceRepositoryTest extends DatabaseTest {

    private final AccountNamespaceRepository repository = new AccountNamespaceRepository();

    @Override
    public void fillDB(VertxTestContext testContext) {
        super.fillDB(testContext);

        smProvider.withTransactionSingle(sessionManager -> {
            Region reg1 = TestResourceProviderProvider.createRegion(1L, "us-east-1");
            Platform p1 = TestPlatformProvider.createPlatformFaas(1L, "lambda");
            Resource r1 = TestResourceProvider.createResource(null, p1, reg1);
            K8sNamespace n1 = TestResourceProviderProvider.createNamespace(null, "ns1", r1);
            AccountNamespace an1 = TestAccountProvider.createAccountNamespace(null, accountAdmin, n1);
            return sessionManager.persist(r1)
                .flatMap(result -> sessionManager.persist(n1))
                .flatMap(result -> sessionManager.persist(an1));
        }).blockingSubscribe(res -> {}, testContext::failNow);
    }

    @Test
    void findByAccountIdAndProviderId(VertxTestContext testContext) {
        smProvider.withTransactionMaybe(sessionManager -> repository
                .findByAccountIdAndNamespaceId(sessionManager, 1L, 1L))
            .subscribe(result -> testContext.verify(() -> {
                assertThat(result.getAccountNamespaceId()).isEqualTo(1L);
                assertThat(result.getAccount().getAccountId()).isEqualTo(1L);
                assertThat(result.getNamespace().getNamespaceId()).isEqualTo(1L);
                testContext.completeNow();
            }));
    }

    @Test
    void findByAccountIdAndResourceId(VertxTestContext testContext) {
        smProvider.withTransactionSingle(sessionManager -> repository
                .findByAccountIdAndResourceId(sessionManager, 1L, 1L))
            .subscribe(result -> testContext.verify(() -> {
                assertThat(result.getAccountNamespaceId()).isEqualTo(1L);
                assertThat(result.getAccount().getAccountId()).isEqualTo(1L);
                assertThat(result.getNamespace().getNamespaceId()).isEqualTo(1L);
                testContext.completeNow();
            }));
    }
}
