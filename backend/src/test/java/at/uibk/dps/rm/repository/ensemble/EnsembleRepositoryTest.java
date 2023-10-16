package at.uibk.dps.rm.repository.ensemble;

import at.uibk.dps.rm.entity.model.Ensemble;
import at.uibk.dps.rm.testutil.DatabaseTest;
import at.uibk.dps.rm.testutil.objectprovider.TestEnsembleProvider;
import io.reactivex.rxjava3.core.Maybe;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.core.Vertx;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class EnsembleRepositoryTest extends DatabaseTest {

    private final EnsembleRepository repository = new EnsembleRepository();

    @Override
    public void fillDB(Vertx vertx, VertxTestContext testContext) {
        super.fillDB(vertx, testContext);

        smProvider.withTransactionSingle(sessionManager -> {
            Ensemble e1 = TestEnsembleProvider.createEnsemble(null, 1L, "e1", true);
            Ensemble e2 = TestEnsembleProvider.createEnsemble(null, 1L, "e2", false);
            Ensemble e3 = TestEnsembleProvider.createEnsemble(null, 2L, "e3", true);
            return sessionManager.persist(e1)
                .flatMap(res -> sessionManager.persist(e2))
                .flatMap(res -> sessionManager.persist(e3));
        }).blockingSubscribe(res -> {}, testContext::failNow);
    }

    private static Stream<Arguments> provideFindAllByAccount() {
        return Stream.of(
            Arguments.of(1L, List.of("e1", "e2")),
            Arguments.of(2L, List.of("e3")),
            Arguments.of(3L, List.of())
        );
    }

    @ParameterizedTest
    @MethodSource("provideFindAllByAccount")
    void findAllByAccount(long accountId, List<String> ensembles, VertxTestContext testContext) {
        smProvider.withTransactionSingle(sessionManager -> repository.findAllByAccountId(sessionManager, accountId))
            .subscribe(result -> testContext.verify(() -> {
                assertThat(result.stream().map(Ensemble::getName).collect(Collectors.toList()))
                    .isEqualTo(ensembles);
                testContext.completeNow();
            }), throwable -> testContext.failNow("method has thrown exception"));
    }

    @ParameterizedTest
    @CsvSource({
        "1, 1, true, e1",
        "1, 2, false, none",
        "1, 3, false, none",
        "2, 1, true, e2",
        "2, 2, false, none",
        "3, 1, false, none",
        "3, 2, true, e3",
    })
    void findByIdAndAccountId(long ensembleId, long accountId, boolean exists, String ensemble,
            VertxTestContext testContext) {
        smProvider.withTransactionMaybe(sessionManager -> repository
                .findByIdAndAccountId(sessionManager, ensembleId, accountId))
            .subscribe(result -> testContext.verify(() -> {
                if (exists) {
                    assertThat(result.getEnsembleId()).isEqualTo(ensembleId);
                    assertThat(result.getName()).isEqualTo(ensemble);
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
        "e1, 1, true, 1",
        "e1, 2, false, -1",
        "e1, 3, false, -1",
        "e2, 1, true, 2",
        "e3, 1, false, -1",
        "e3, 2, true, 3",
        "e3, 3, false, -1",
    })
    void findByNameAndAccountId(String name, long accountId, boolean exists, long ensembleId,
            VertxTestContext testContext) {
        smProvider.withTransactionMaybe(sessionManager -> repository
                .findByNameAndAccountId(sessionManager, name, accountId))
            .subscribe(result -> testContext.verify(() -> {
                if (exists) {
                    assertThat(result.getEnsembleId()).isEqualTo(ensembleId);
                    assertThat(result.getName()).isEqualTo(name);
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
        "1, true",
        "1, false",
        "2, true",
        "2, false",
        "3, true",
        "3, false",
    })
    void updateTriggerUrl(long ensembleId, boolean validity, VertxTestContext testContext) {
        smProvider.withTransactionMaybe(sessionManager -> repository
                .updateValidity(sessionManager, ensembleId, validity)
                .andThen(Maybe.defer(() -> sessionManager.find(Ensemble.class, ensembleId))))
            .subscribe(result -> testContext.verify(() -> {
                assertThat(result.getIsValid()).isEqualTo(validity);
                testContext.completeNow();
            }), throwable -> testContext.failNow("method has thrown exception"));
    }
}
