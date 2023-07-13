package at.uibk.dps.rm.handler.ensemble;

import at.uibk.dps.rm.entity.model.Account;
import at.uibk.dps.rm.entity.model.Ensemble;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.handler.resource.ResourceChecker;
import at.uibk.dps.rm.testutil.RoutingContextMockHelper;
import at.uibk.dps.rm.testutil.objectprovider.TestAccountProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestEnsembleProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestResourceProvider;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.ext.web.RoutingContext;
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
 * Implements tests for the {@link EnsembleHandler} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class EnsembleHandlerTest {

    private EnsembleHandler ensembleHandler;

    @Mock
    private EnsembleChecker ensembleChecker;

    @Mock
    private EnsembleSLOChecker ensembleSLOChecker;

    @Mock
    private ResourceChecker resourceChecker;

    @Mock
    private RoutingContext rc;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        ensembleHandler = new EnsembleHandler(ensembleChecker, ensembleSLOChecker, resourceChecker);
    }

    @Test
    void getOne(VertxTestContext testContext) {
        long ensembleId = 1L;
        Account account = TestAccountProvider.createAccount(1L);
        Ensemble ensemble = TestEnsembleProvider.createEnsemble(1L, 1L);
        JsonObject r1 = JsonObject.mapFrom(TestResourceProvider.createResource(1L));
        JsonObject r2 = JsonObject.mapFrom(TestResourceProvider.createResource(2L));
        JsonObject slo1 = JsonObject.mapFrom(TestEnsembleProvider
            .createEnsembleSLO(1L, "os", ensembleId, "ubuntu"));
        JsonObject slo2 = JsonObject.mapFrom(TestEnsembleProvider
            .createEnsembleSLO(2L, "online", ensembleId, true));
        JsonObject slo3 = JsonObject.mapFrom(TestEnsembleProvider
            .createEnsembleSLO(3L, "availability", ensembleId));

        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        when(rc.pathParam("id")).thenReturn(String.valueOf(ensembleId));
        when(ensembleChecker.checkFindOne(ensembleId,account.getAccountId()))
            .thenReturn(Single.just(JsonObject.mapFrom(ensemble)));
        when(resourceChecker.checkFindAllByEnsemble(ensembleId))
            .thenReturn(Single.just(new JsonArray(List.of(r1, r2))));
        when(ensembleSLOChecker.checkFindAllByEnsemble(ensembleId))
            .thenReturn(Single.just(new JsonArray(List.of(slo1, slo2, slo3))));

        ensembleHandler.getOne(rc)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.getLong("ensemble_id")).isEqualTo(1L);
                    assertThat(result.getJsonArray("resources").size()).isEqualTo(2);
                    assertThat(result.getJsonArray("slos").size()).isEqualTo(8);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void getOneNotFound(VertxTestContext testContext) {
        long ensembleId = 1L;
        Account account = TestAccountProvider.createAccount(1L);

        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        when(rc.pathParam("id")).thenReturn(String.valueOf(ensembleId));
        when(ensembleChecker.checkFindOne(ensembleId,account.getAccountId()))
            .thenReturn(Single.error(NotFoundException::new));

        ensembleHandler.getOne(rc)
            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
            }));
    }
}
