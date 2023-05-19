package at.uibk.dps.rm.handler.ensemble;

import at.uibk.dps.rm.service.rxjava3.database.ensemble.EnsembleSLOService;
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
 * Implements tests for the {@link EnsembleSLOChecker} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class EnsembleSLOCheckerTest {

    private EnsembleSLOChecker checker;

    @Mock
    private EnsembleSLOService service;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        checker = new EnsembleSLOChecker(service);
    }

    @Test
    void checkFindAllByEnsemble(VertxTestContext testContext) {
        long ensembleId = 1L;
        JsonObject e1 = JsonObject.mapFrom(TestEnsembleProvider.createEnsembleSLO(1L, "availability",
            ensembleId));
        JsonObject e2 = JsonObject.mapFrom(TestEnsembleProvider.createEnsembleSLO(2L, "latency",
            ensembleId));
        JsonObject e3 = JsonObject.mapFrom(TestEnsembleProvider.createEnsembleSLO(3L, "bandwidth",
            ensembleId));
        JsonArray ensemblesJson = new JsonArray(List.of(e1, e2, e3));

        when(service.findAllByEnsembleId(ensembleId)).thenReturn(Single.just(ensemblesJson));

        checker.checkFindAllByEnsemble(ensembleId)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.size()).isEqualTo(3);
                    for (int i = 0; i < 3; i++) {
                        assertThat(result.getJsonObject(i).getLong("ensemble_slo_id")).isEqualTo(i+1);
                    }
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void checkFindAllByEnsembleEmpty(VertxTestContext testContext) {
        long ensembleId = 1L;
        JsonArray ensemblesJson = new JsonArray();

        when(service.findAllByEnsembleId(ensembleId)).thenReturn(Single.just(ensemblesJson));

        checker.checkFindAllByEnsemble(ensembleId)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.size()).isEqualTo(0);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }
}
