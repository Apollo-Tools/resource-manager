package at.uibk.dps.rm.repository.ensemble;

import at.uibk.dps.rm.entity.model.Ensemble;
import at.uibk.dps.rm.entity.model.EnsembleSLO;
import at.uibk.dps.rm.testutil.integration.DatabaseTest;
import at.uibk.dps.rm.testutil.objectprovider.TestEnsembleProvider;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.core.Vertx;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Implements tests for the {@link EnsembleSLORepository} class.
 *
 * @author matthi-g
 */
public class EnsembleSLORepositoryTest extends DatabaseTest {

    private final EnsembleSLORepository repository = new EnsembleSLORepository();

    @Override
    public void fillDB(Vertx vertx, VertxTestContext testContext) {
        super.fillDB(vertx, testContext);

        smProvider.withTransactionSingle(sessionManager -> {
            Ensemble e1 = TestEnsembleProvider.createEnsemble(null, 1L, "e1", true);
            Ensemble e2 = TestEnsembleProvider.createEnsemble(null, 1L, "e2", false);
            Ensemble e3 = TestEnsembleProvider.createEnsemble(null, 2L, "e3", true);
            EnsembleSLO slo1 = TestEnsembleProvider.createEnsembleSLOGT(null, "availability", e1,
                0.94);
            EnsembleSLO slo2 = TestEnsembleProvider.createEnsembleSLOGT(null, "cpu", e1,
                2);
            EnsembleSLO slo3 = TestEnsembleProvider.createEnsembleSLOGT(null, "memory", e1,
                1024);
            EnsembleSLO slo4 = TestEnsembleProvider.createEnsembleSLOGT(null, "availability", e2,
                0.9);
            return sessionManager.persist(e1)
                .flatMap(res -> sessionManager.persist(e2))
                .flatMap(res -> sessionManager.persist(e3))
                .flatMap(res -> sessionManager.persist(slo1))
                .flatMap(res -> sessionManager.persist(slo2))
                .flatMap(res -> sessionManager.persist(slo3))
                .flatMap(res -> sessionManager.persist(slo4));
        }).blockingSubscribe(res -> {}, testContext::failNow);
    }

    private static Stream<Arguments> provideFindAllByAccount() {
        return Stream.of(
            Arguments.of(1L, List.of("availability", "cpu", "memory")),
            Arguments.of(2L, List.of("availability")),
            Arguments.of(3L, List.of()),
            Arguments.of(4L, List.of())
        );
    }

    @ParameterizedTest
    @MethodSource("provideFindAllByAccount")
    void findAllByAccount(long ensembleId, List<String> slos, VertxTestContext testContext) {
        smProvider.withTransactionSingle(sessionManager -> repository.findAllByEnsembleId(sessionManager, ensembleId))
            .subscribe(result -> testContext.verify(() -> {
                assertThat(result.stream().map(EnsembleSLO::getName).collect(Collectors.toList())).isEqualTo(slos);
                testContext.completeNow();
            }), throwable -> testContext.failNow("method has thrown exception"));
    }
}
