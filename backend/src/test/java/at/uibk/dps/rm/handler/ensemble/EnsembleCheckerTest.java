package at.uibk.dps.rm.handler.ensemble;

import at.uibk.dps.rm.entity.model.Ensemble;
import at.uibk.dps.rm.exception.AlreadyExistsException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.service.rxjava3.database.ensemble.EnsembleService;
import at.uibk.dps.rm.testutil.SingleHelper;
import at.uibk.dps.rm.testutil.objectprovider.TestEnsembleProvider;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.when;

/**
 * Implements tests for the {@link EnsembleChecker} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class EnsembleCheckerTest {

    private EnsembleChecker ensembleChecker;

    @Mock
    private EnsembleService ensembleService;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        ensembleChecker = new EnsembleChecker(ensembleService);
    }

    @Test
    void checkFindOneExists(VertxTestContext testContext) {
        long ensembleId = 1L, accountId = 2L;
        Ensemble ensemble = TestEnsembleProvider.createEnsemble(ensembleId, accountId);

        when(ensembleService.findOneByIdAndAccountId(ensembleId, accountId))
                .thenReturn(Single.just(JsonObject.mapFrom(ensemble)));

        ensembleChecker.checkFindOne(ensembleId, accountId)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.getLong("ensemble_id")).isEqualTo(1L);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void checkFindOneNotFound(VertxTestContext testContext) {
        long ensembleId = 1L, accountId = 2L;
        Single<JsonObject> handler = SingleHelper.getEmptySingle();

        when(ensembleService.findOneByIdAndAccountId(ensembleId, accountId)).thenReturn(handler);

        ensembleChecker.checkFindOne(ensembleId, accountId)
            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
            }));
    }

    @Test
    void checkExistsOneTrue(VertxTestContext testContext) {
        long accountId = 2L;
        String name = "ensemble";

        when(ensembleService.existsOneByNameAndAccountId(name, accountId)).thenReturn(Single.just(false));

        ensembleChecker.checkExistsOneByName(name, accountId)
            .blockingSubscribe(() -> {},
                    throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
        testContext.completeNow();
    }

    @Test
    void checkExistsOneFalse(VertxTestContext testContext) {
        long accountId = 2L;
        String name = "ensemble";

        when(ensembleService.existsOneByNameAndAccountId(name, accountId)).thenReturn(Single.just(true));

        ensembleChecker.checkExistsOneByName(name, accountId)
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(AlreadyExistsException.class);
                    testContext.completeNow();
            }));
    }

    @Test
    void checkFindAll(VertxTestContext testContext) {
        long accountId = 2L;
        JsonObject e1 = JsonObject.mapFrom(TestEnsembleProvider.createEnsemble(1L, accountId));
        JsonObject e2 = JsonObject.mapFrom(TestEnsembleProvider.createEnsemble(2L, accountId));
        JsonObject e3 = JsonObject.mapFrom(TestEnsembleProvider.createEnsemble(3L, accountId));
        JsonArray ensemblesJson = new JsonArray(List.of(e1, e2, e3));

        when(ensembleService.findAllByAccountId(accountId)).thenReturn(Single.just(ensemblesJson));

        ensembleChecker.checkFindAll(accountId)
            .subscribe(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(3);
                for (int i = 0; i < 3; i++) {
                    assertThat(result.getJsonObject(i).getLong("ensemble_id")).isEqualTo(i+1);
                }
                testContext.completeNow();
            }),
            throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void checkFindAllEmpty(VertxTestContext testContext) {
        long accountId = 2L;
        JsonArray ensemblesJson = new JsonArray();

        when(ensembleService.findAllByAccountId(accountId)).thenReturn(Single.just(ensemblesJson));

        ensembleChecker.checkFindAll(accountId)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.size()).isEqualTo(0);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }
}
