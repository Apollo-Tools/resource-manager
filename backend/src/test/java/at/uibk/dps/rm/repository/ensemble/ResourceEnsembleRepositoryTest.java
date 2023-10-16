package at.uibk.dps.rm.repository.ensemble;

import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.testutil.DatabaseTest;
import at.uibk.dps.rm.testutil.objectprovider.TestEnsembleProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestPlatformProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestResourceProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestResourceProviderProvider;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.core.Vertx;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;

public class ResourceEnsembleRepositoryTest extends DatabaseTest {

    private final ResourceEnsembleRepository repository = new ResourceEnsembleRepository();

    @Override
    public void fillDB(Vertx vertx, VertxTestContext testContext) {
        super.fillDB(vertx, testContext);

        smProvider.withTransactionCompletable(sessionManager -> {
            Ensemble e1 = TestEnsembleProvider.createEnsemble(null, 1L, "e1", true);
            Ensemble e2 = TestEnsembleProvider.createEnsemble(null, 2L, "e2", false);
            Region reg1 = TestResourceProviderProvider.createRegion(1L, "us-east-1");
            Platform p1 = TestPlatformProvider.createPlatformContainer(1L, "lambda");
            Resource r1 = TestResourceProvider.createResource(null, "r1", p1, reg1);
            Resource r2 = TestResourceProvider.createResource(null, "r2", p1, reg1);
            Resource r3 = TestResourceProvider.createResource(null, "r3", p1, reg1);
            Resource r4 = TestResourceProvider.createResource(null, "r4", p1, reg1);
            ResourceEnsemble re1 = TestEnsembleProvider.createResourceEnsemble(null, e1, r1);
            ResourceEnsemble re2 = TestEnsembleProvider.createResourceEnsemble(null, e1, r2);
            ResourceEnsemble re3 = TestEnsembleProvider.createResourceEnsemble(null, e2, r1);
            ResourceEnsemble re4 = TestEnsembleProvider.createResourceEnsemble(null, e2, r3);
            return sessionManager.persist(e1)
                .flatMap(res -> sessionManager.persist(e2))
                .flatMap(res -> sessionManager.persist(r1))
                .flatMap(res -> sessionManager.persist(r2))
                .flatMap(res -> sessionManager.persist(r3))
                .flatMap(res -> sessionManager.persist(r4))
                .flatMapCompletable(res -> sessionManager.persist(new ResourceEnsemble[]{re1, re2, re3, re4}));
        }).blockingSubscribe(() -> {}, testContext::failNow);
    }

    @ParameterizedTest
    @CsvSource({
        "1, 1, 1, true, 1",
        "1, 1, 2, true, 2",
        "1, 1, 3, false, -1",
        "1, 2, 1, false, -1",
        "1, 2, 3, false, -1",
        "1, 3, 1, false, -1",
        "2, 2, 1, true, 3",
        "2, 2, 2, false, -1",
        "2, 2, 3, true, 4",
        "2, 2, 4, false, -1",
        "3, 3, 1, false, -1"
    })
    void findByIdAndAccountId(long accountId, long ensembleId, long resourceId, boolean exists, long resourceEnsembleId,
            VertxTestContext testContext) {
        smProvider.withTransactionMaybe(sessionManager -> repository
                .findByEnsembleIdAndResourceId(sessionManager, accountId, ensembleId, resourceId))
            .subscribe(result -> testContext.verify(() -> {
                if (exists) {
                    assertThat(result.getResourceEnsembleId()).isEqualTo(resourceEnsembleId);
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
}
