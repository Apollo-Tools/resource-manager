package at.uibk.dps.rm.handler.ensemble;

import at.uibk.dps.rm.entity.model.Account;
import at.uibk.dps.rm.entity.model.Ensemble;
import at.uibk.dps.rm.entity.model.ResourceEnsemble;
import at.uibk.dps.rm.service.rxjava3.database.ensemble.EnsembleService;
import at.uibk.dps.rm.service.rxjava3.database.ensemble.ResourceEnsembleService;
import at.uibk.dps.rm.service.rxjava3.database.resource.ResourceService;
import at.uibk.dps.rm.service.rxjava3.monitoring.metricquery.MetricQueryService;
import at.uibk.dps.rm.testutil.RoutingContextMockHelper;
import at.uibk.dps.rm.testutil.objectprovider.TestAccountProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestEnsembleProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestResourceProvider;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.ext.web.RoutingContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.when;

/**
 * Implements tests for the {@link ResourceEnsembleHandler} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class ResourceEnsembleHandlerTest {

    private ResourceEnsembleHandler resourceEnsembleHandler;

    @Mock
    private ResourceEnsembleService resourceEnsembleService;

    @Mock
    private EnsembleService ensembleService;

    @Mock
    private ResourceService resourceService;

    @Mock
    private MetricQueryService metricQueryService;

    @Mock
    private RoutingContext rc;

    private long ensembleId, accountId, resourceId;
    private Account account;
    private Ensemble ensemble;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        resourceEnsembleHandler = new ResourceEnsembleHandler(resourceEnsembleService, ensembleService,
            resourceService, metricQueryService);
        ensembleId = 1L;
        accountId = 2L;
        resourceId = 3L;
        account = TestAccountProvider.createAccount(accountId);
        ensemble = TestEnsembleProvider.createEnsemble(ensembleId, accountId);
    }

    @Test
    void postOne(VertxTestContext testContext) {
        ResourceEnsemble re = TestEnsembleProvider.createResourceEnsemble(4L, ensemble,
            TestResourceProvider.createResource(resourceId));
        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        when(rc.pathParam("ensembleId")).thenReturn(String.valueOf(ensembleId));
        when(rc.pathParam("resourceId")).thenReturn(String.valueOf(resourceId));
        when(resourceEnsembleService.saveByEnsembleIdAndResourceId(accountId, ensembleId, resourceId))
            .thenReturn(Single.just(JsonObject.mapFrom(re)));

        resourceEnsembleHandler.postOne(rc)
            .subscribe(result -> {
                    assertThat(result.getLong("resource_ensemble_id")).isEqualTo(4L);
                    testContext.completeNow();
                },
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void deleteOne(VertxTestContext testContext) {
        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        when(rc.pathParam("ensembleId")).thenReturn(String.valueOf(ensembleId));
        when(rc.pathParam("resourceId")).thenReturn(String.valueOf(resourceId));
        when(resourceEnsembleService.deleteByEnsembleIdAndResourceId(accountId, ensembleId, resourceId))
            .thenReturn(Completable.complete());

        resourceEnsembleHandler.deleteOne(rc)
            .subscribe(testContext::completeNow,
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }
}
