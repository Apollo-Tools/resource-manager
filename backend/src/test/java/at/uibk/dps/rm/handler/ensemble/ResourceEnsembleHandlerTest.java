package at.uibk.dps.rm.handler.ensemble;

import at.uibk.dps.rm.entity.dto.config.ConfigDTO;
import at.uibk.dps.rm.entity.dto.ensemble.GetOneEnsemble;
import at.uibk.dps.rm.entity.model.Account;
import at.uibk.dps.rm.entity.model.Ensemble;
import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.entity.model.ResourceEnsemble;
import at.uibk.dps.rm.exception.BadInputException;
import at.uibk.dps.rm.service.rxjava3.database.ensemble.EnsembleService;
import at.uibk.dps.rm.service.rxjava3.database.ensemble.ResourceEnsembleService;
import at.uibk.dps.rm.service.rxjava3.database.resource.ResourceService;
import at.uibk.dps.rm.service.rxjava3.monitoring.metricquery.MetricQueryService;
import at.uibk.dps.rm.testutil.RoutingContextMockHelper;
import at.uibk.dps.rm.testutil.mockprovider.Mockprovider;
import at.uibk.dps.rm.testutil.mockprovider.SLOMockProvider;
import at.uibk.dps.rm.testutil.objectprovider.*;
import at.uibk.dps.rm.util.configuration.ConfigUtility;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import at.uibk.dps.rm.util.validation.SLOValidator;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.core.Context;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.web.RoutingContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;
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

    @Mock
    private Vertx vertx;

    @Mock
    private Context context;

    private long ensembleId, accountId, resourceId;
    private Account account;
    private Ensemble ensemble;
    private ConfigDTO config;
    private Resource r1, r2;

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
        config = TestConfigProvider.getConfigDTO();
        r1 = TestResourceProvider.createResource(3L);
        r2 = TestResourceProvider.createResource(4L);
    }

    @Test
    void postOne(VertxTestContext testContext) {
        GetOneEnsemble getOneEnsemble = TestDTOProvider.createGetOneEnsemble();
        JsonObject getOneEnsembleJson = JsonObject.mapFrom(getOneEnsemble);
        JsonObject r1Json = JsonObject.mapFrom(r1);
        JsonObject r2Json = JsonObject.mapFrom(r2);
        JsonArray filterResources = new JsonArray(List.of(r1Json, r2Json));
        ResourceEnsemble re1 = TestEnsembleProvider.createResourceEnsemble(4L, ensemble,
            r1);
        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        when(rc.pathParam("ensembleId")).thenReturn(String.valueOf(ensembleId));
        when(rc.pathParam("resourceId")).thenReturn(String.valueOf(resourceId));
        when(ensembleService.findOneByIdAndAccountId(ensembleId, accountId))
            .thenReturn(Single.just(getOneEnsembleJson));
        when(resourceService.findAllByNonMonitoredSLOs(getOneEnsembleJson)).thenReturn(Single.just(filterResources));
        when(resourceEnsembleService.saveByEnsembleIdAndResourceId(accountId, ensembleId, r1.getResourceId()))
            .thenReturn(Single.just(JsonObject.mapFrom(re1)));

        try (MockedStatic<Vertx> mockedVertx = mockStatic(Vertx.class);
                MockedConstruction<ConfigUtility> ignoreConfig = Mockprovider.mockConfig(config);
                MockedConstruction<SLOValidator> ignoreSLOValidator = SLOMockProvider
                    .mockSLOValidatorFilter(getOneEnsemble.getServiceLevelObjectives(), Set.of(r1))) {
            mockedVertx.when(Vertx::currentContext).thenReturn(context);
            when(context.owner()).thenReturn(vertx);
            resourceEnsembleHandler.postOne(rc)
                .subscribe(result -> {
                        assertThat(result.getLong("resource_ensemble_id")).isEqualTo(4L);
                        testContext.completeNow();
                    },
                    throwable -> testContext.failNow("method has thrown exception"));
        }
    }

    @Test
    void postOneResourceDoesNotFulfillMonitoredSLOs(VertxTestContext testContext) {
        GetOneEnsemble getOneEnsemble = TestDTOProvider.createGetOneEnsemble();
        JsonObject getOneEnsembleJson = JsonObject.mapFrom(getOneEnsemble);
        JsonObject r1Json = JsonObject.mapFrom(r1);
        JsonObject r2Json = JsonObject.mapFrom(r2);
        JsonArray filterResources = new JsonArray(List.of(r1Json, r2Json));

        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        when(rc.pathParam("ensembleId")).thenReturn(String.valueOf(ensembleId));
        when(rc.pathParam("resourceId")).thenReturn(String.valueOf(resourceId));
        when(ensembleService.findOneByIdAndAccountId(ensembleId, accountId))
            .thenReturn(Single.just(getOneEnsembleJson));
        when(resourceService.findAllByNonMonitoredSLOs(getOneEnsembleJson)).thenReturn(Single.just(filterResources));

        try (MockedStatic<Vertx> mockedVertx = mockStatic(Vertx.class);
                MockedConstruction<ConfigUtility> ignoreConfig = Mockprovider.mockConfig(config);
                MockedConstruction<SLOValidator> ignoreSLOValidator = SLOMockProvider
                    .mockSLOValidatorFilter(getOneEnsemble.getServiceLevelObjectives(), Set.of())) {
            mockedVertx.when(Vertx::currentContext).thenReturn(context);
            when(context.owner()).thenReturn(vertx);
            resourceEnsembleHandler.postOne(rc)
                .subscribe(result -> testContext.failNow("method did not throw exception"),
                    throwable -> testContext.verify(() -> {
                        assertThat(throwable).isInstanceOf(BadInputException.class);
                        assertThat(throwable.getMessage()).isEqualTo("resource does not fulfill service level " +
                            "objectives");
                        testContext.completeNow();
                    }));
        }
    }

    @Test
    void postOneResourceDoesNotFulfillNonMonitoredSLOs(VertxTestContext testContext) {
        GetOneEnsemble getOneEnsemble = TestDTOProvider.createGetOneEnsemble();
        JsonObject getOneEnsembleJson = JsonObject.mapFrom(getOneEnsemble);
        JsonObject r2Json = JsonObject.mapFrom(r2);
        JsonArray filterResources = new JsonArray(List.of(r2Json));

        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        when(rc.pathParam("ensembleId")).thenReturn(String.valueOf(ensembleId));
        when(rc.pathParam("resourceId")).thenReturn(String.valueOf(resourceId));
        when(ensembleService.findOneByIdAndAccountId(ensembleId, accountId))
            .thenReturn(Single.just(getOneEnsembleJson));
        when(resourceService.findAllByNonMonitoredSLOs(getOneEnsembleJson)).thenReturn(Single.just(filterResources));

        try (MockedStatic<Vertx> mockedVertx = mockStatic(Vertx.class);
                MockedConstruction<ConfigUtility> ignoreConfig = Mockprovider.mockConfig(config)) {
            mockedVertx.when(Vertx::currentContext).thenReturn(context);
            when(context.owner()).thenReturn(vertx);
            resourceEnsembleHandler.postOne(rc)
                .subscribe(result -> testContext.failNow("method did not throw exception"),
                    throwable -> testContext.verify(() -> {
                        assertThat(throwable).isInstanceOf(BadInputException.class);
                        assertThat(throwable.getMessage()).isEqualTo("resource does not fulfill service level " +
                            "objectives");
                        testContext.completeNow();
                    }));
        }
    }

    @Test
    void deleteOne(VertxTestContext testContext) {
        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        when(rc.pathParam("ensembleId")).thenReturn(String.valueOf(ensembleId));
        when(rc.pathParam("resourceId")).thenReturn(String.valueOf(resourceId));
        when(resourceEnsembleService.deleteByEnsembleIdAndResourceId(accountId, ensembleId, resourceId))
            .thenReturn(Completable.complete());

        resourceEnsembleHandler.deleteOne(rc)
            .subscribe(testContext::completeNow, throwable -> testContext.failNow("method has thrown exception"));
    }
}
