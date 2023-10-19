package at.uibk.dps.rm.handler.ensemble;

import at.uibk.dps.rm.entity.dto.CreateEnsembleRequest;
import at.uibk.dps.rm.entity.dto.ensemble.ResourceEnsembleStatus;
import at.uibk.dps.rm.entity.model.Account;
import at.uibk.dps.rm.entity.model.Ensemble;
import at.uibk.dps.rm.service.rxjava3.database.ensemble.EnsembleService;
import at.uibk.dps.rm.testutil.RoutingContextMockHelper;
import at.uibk.dps.rm.testutil.objectprovider.TestAccountProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestDTOProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestEnsembleProvider;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.reactivex.rxjava3.core.Completable;
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
    private EnsembleService ensembleService;

    @Mock
    private RoutingContext rc;

    private long ensembleId, accountId;
    private Account account;
    private Ensemble ensemble;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        ensembleHandler = new EnsembleHandler(ensembleService);
        ensembleId = 1L;
        accountId = 2L;
        account = TestAccountProvider.createAccount(accountId);
        ensemble = TestEnsembleProvider.createEnsemble(ensembleId, accountId);
    }

    @Test
    void getOneFromAccount(VertxTestContext testContext) {
        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        when(rc.pathParam("id")).thenReturn(String.valueOf(ensembleId));
        when(ensembleService.findOneByIdAndAccountId(ensembleId, accountId))
            .thenReturn(Single.just(JsonObject.mapFrom(ensemble)));

        ensembleHandler.getOneFromAccount(rc)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.getLong("ensemble_id")).isEqualTo(1L);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void validateNewResourceEnsembleSLOs(VertxTestContext testContext) {
        CreateEnsembleRequest request = TestDTOProvider.createCreateEnsembleRequest(1L);
        JsonObject body = JsonObject.mapFrom(request);

        RoutingContextMockHelper.mockBody(rc, body);
        when(ensembleService.validateCreateEnsembleRequest(body))
            .thenReturn(Completable.complete());

        ensembleHandler.validateNewResourceEnsembleSLOs(rc)
            .subscribe(() -> testContext.verify(testContext::completeNow),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void validateExistingEnsemble(VertxTestContext testContext) {
        ResourceEnsembleStatus res = new ResourceEnsembleStatus(1L, true);
        JsonArray jsonResult = new JsonArray(List.of(JsonObject.mapFrom(res)));

        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        when(rc.pathParam("id")).thenReturn(String.valueOf(ensembleId));
        when(ensembleService.validateExistingEnsemble(accountId, ensembleId)).thenReturn(Single.just(jsonResult));

        ensembleHandler.validateExistingEnsemble(rc)
            .subscribe(result -> {
                    assertThat(result.getJsonObject(0).getLong("resource_id")).isEqualTo(1L);
                    assertThat(result.getJsonObject(0).getBoolean("is_valid")).isEqualTo(true);
                    testContext.verify(testContext::completeNow);
                },
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void validateAllExistingEnsembles(VertxTestContext testContext) {
        when(ensembleService.validateAllExistingEnsembles()).thenReturn(Completable.complete());

        ensembleHandler.validateAllExistingEnsembles()
            .subscribe(() -> testContext.verify(testContext::completeNow),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }
}
