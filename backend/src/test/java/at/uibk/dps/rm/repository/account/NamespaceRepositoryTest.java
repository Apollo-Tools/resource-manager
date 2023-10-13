package at.uibk.dps.rm.repository.account;

import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.testutil.DatabaseTest;
import at.uibk.dps.rm.testutil.objectprovider.TestAccountProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestPlatformProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestResourceProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestResourceProviderProvider;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class NamespaceRepositoryTest extends DatabaseTest {

    private final NamespaceRepository repository = new NamespaceRepository();

    @Override
    public void fillDB(VertxTestContext testContext) {
        super.fillDB(testContext);

        smProvider.withTransactionSingle(sessionManager -> {
            Region reg1 = TestResourceProviderProvider.createRegion(1L, "us-east-1");
            Region reg2 = TestResourceProviderProvider.createRegion(2L, "us-west-2");
            Platform p1 = TestPlatformProvider.createPlatformContainer(4L, "k8s");
            Resource r1 = TestResourceProvider.createResource(null, "r1", p1, reg1);
            Resource r2 = TestResourceProvider.createResource(null, "r2", p1, reg2);
            K8sNamespace n1 = TestResourceProviderProvider.createNamespace(null, "ns1", r1);
            K8sNamespace n2 = TestResourceProviderProvider.createNamespace(null, "ns2", r1);
            K8sNamespace n3 = TestResourceProviderProvider.createNamespace(null, "ns3", r2);
            AccountNamespace an1 = TestAccountProvider.createAccountNamespace(null, accountAdmin, n1);
            AccountNamespace an2 = TestAccountProvider.createAccountNamespace(null, accountDefault, n2);
            AccountNamespace an3 = TestAccountProvider.createAccountNamespace(null, accountAdmin, n3);
            return sessionManager.persist(r1)
                .flatMap(res -> sessionManager.persist(r2))
                .flatMap(res -> sessionManager.persist(n1))
                .flatMap(res -> sessionManager.persist(n2))
                .flatMap(res -> sessionManager.persist(n3))
                .flatMap(res -> sessionManager.persist(an1))
                .flatMap(res -> sessionManager.persist(an2))
                .flatMap(res -> sessionManager.persist(an3));
        }).blockingSubscribe(res -> {}, testContext::failNow);
    }

    @Test
    void findAllAndFetch(VertxTestContext testContext) {
        smProvider.withTransactionSingle(repository::findAllAndFetch)
            .subscribe(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(3);
                assertThat(result.get(0).getNamespaceId()).isEqualTo(1L);
                assertThat(result.get(0).getResource().getName()).isEqualTo("r1");
                assertThat(result.get(1).getNamespaceId()).isEqualTo(2L);
                assertThat(result.get(1).getResource().getName()).isEqualTo("r1");
                assertThat(result.get(2).getNamespaceId()).isEqualTo(3L);
                assertThat(result.get(2).getResource().getName()).isEqualTo("r2");
                testContext.completeNow();
            }), throwable -> testContext.failNow("method has thrown exception"));
    }

    @Test
    void findAllByAccountIdAndFetch(VertxTestContext testContext) {
        smProvider.withTransactionSingle(sessionManager -> repository
                .findAllByAccountIdAndFetch(sessionManager, 1L))
            .subscribe(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(2);
                assertThat(result.get(0).getNamespaceId()).isEqualTo(1L);
                assertThat(result.get(0).getResource().getName()).isEqualTo("r1");
                assertThat(result.get(1).getNamespaceId()).isEqualTo(3L);
                assertThat(result.get(1).getResource().getName()).isEqualTo("r2");
                testContext.completeNow();
            }), throwable -> testContext.failNow("method has thrown exception"));
    }

    @Test
    void findAllByClusterName(VertxTestContext testContext) {
        smProvider.withTransactionSingle(sessionManager -> repository
                .findAllByClusterName(sessionManager, "r1"))
            .subscribe(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(2);
                assertThat(result.get(0).getNamespaceId()).isEqualTo(1L);
                assertThat(result.get(1).getNamespaceId()).isEqualTo(2L);
                testContext.completeNow();
            }), throwable -> testContext.failNow("method has thrown exception"));
    }
}
