package at.uibk.dps.rm.handler.ensemble;

import at.uibk.dps.rm.entity.dto.CreateEnsembleRequest;
import at.uibk.dps.rm.entity.dto.config.ConfigDTO;
import at.uibk.dps.rm.entity.dto.ensemble.GetOneEnsemble;
import at.uibk.dps.rm.entity.dto.ensemble.ResourceEnsembleStatus;
import at.uibk.dps.rm.entity.model.Account;
import at.uibk.dps.rm.entity.model.Ensemble;
import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.service.database.util.EnsembleUtility;
import at.uibk.dps.rm.service.rxjava3.database.ensemble.EnsembleService;
import at.uibk.dps.rm.service.rxjava3.database.metric.MetricService;
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
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.*;

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
    private ResourceService resourceService;

    @Mock
    private MetricService metricService;

    @Mock
    private MetricQueryService metricQueryService;

    @Mock
    private RoutingContext rc;

    @Mock
    private Vertx vertx;

    @Mock
    private Context context;

    private long ensembleId, accountId;
    private Account account;
    private Ensemble e1, e2;
    private ConfigDTO config;
    private Resource r1, r2;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        ensembleHandler = new EnsembleHandler(ensembleService, resourceService, metricService, metricQueryService);
        ensembleId = 1L;
        accountId = 2L;
        account = TestAccountProvider.createAccount(accountId);
        e1 = TestEnsembleProvider.createEnsemble(ensembleId, accountId);
        e2 = TestEnsembleProvider.createEnsemble(ensembleId + 1, accountId);
        config = TestConfigProvider.getConfigDTO();
        r1 = TestResourceProvider.createResource(1L);
        r2 = TestResourceProvider.createResource(2L);
    }

    @Test
    void getOneFromAccount(VertxTestContext testContext) {
        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        when(rc.pathParam("id")).thenReturn(String.valueOf(ensembleId));
        when(ensembleService.findOneByIdAndAccountId(ensembleId, accountId))
            .thenReturn(Single.just(JsonObject.mapFrom(e1)));

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
        JsonObject r1Json = JsonObject.mapFrom(r1);
        JsonObject r2Json = JsonObject.mapFrom(r2);
        JsonArray filterResources = new JsonArray(List.of(r1Json, r2Json));

        RoutingContextMockHelper.mockBody(rc, body);
        when(metricService.checkMetricTypeForSLOs(body)).thenReturn(Completable.complete());
        when(resourceService.findAllByNonMonitoredSLOs(body)).thenReturn(Single.just(filterResources));

        try (MockedStatic<Vertx> mockedVertx = mockStatic(Vertx.class);
                MockedConstruction<ConfigUtility> ignoreConfig = Mockprovider.mockConfig(config);
                MockedConstruction<SLOValidator> ignoreSLOValidator = SLOMockProvider
                    .mockSLOValidatorFilter(request, Set.of(r1, r2))) {
            mockedVertx.when(Vertx::currentContext).thenReturn(context);
            when(context.owner()).thenReturn(vertx);
            ensembleHandler.validateNewResourceEnsembleSLOs(rc)
                .subscribe(testContext::completeNow, throwable -> testContext.failNow("method has thrown exception"));
        }
    }

    @Test
    void validateExistingEnsemble(VertxTestContext testContext) {
        ResourceEnsembleStatus res1 = new ResourceEnsembleStatus(1L, true);
        ResourceEnsembleStatus res2 = new ResourceEnsembleStatus(2L, false);
        JsonArray jsonResult = new JsonArray(List.of(JsonObject.mapFrom(res1), JsonObject.mapFrom(res2)));
        GetOneEnsemble getOneEnsemble = TestDTOProvider.createGetOneEnsemble();
        JsonObject getOneEnsembleJson = JsonObject.mapFrom(getOneEnsemble);
        JsonObject r1Json = JsonObject.mapFrom(r1);
        JsonObject r2Json = JsonObject.mapFrom(r2);
        JsonArray filterResources = new JsonArray(List.of(r1Json, r2Json));

        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        when(rc.pathParam("id")).thenReturn(String.valueOf(ensembleId));
        when(ensembleService.findOneByIdAndAccountId(ensembleId, accountId))
            .thenReturn(Single.just(getOneEnsembleJson));
        when(resourceService.findAllByNonMonitoredSLOs(getOneEnsembleJson)).thenReturn(Single.just(filterResources));
        doReturn(Completable.complete()).when(ensembleService).updateEnsembleStatus(1L, jsonResult);

        try (MockedStatic<Vertx> mockedVertx = mockStatic(Vertx.class);
             MockedConstruction<ConfigUtility> ignoreConfig = Mockprovider.mockConfig(config);
             MockedConstruction<SLOValidator> ignoreSLOValidator = SLOMockProvider
                    .mockSLOValidatorFilter(getOneEnsemble, Set.of(r1, r2));
             MockedStatic<EnsembleUtility> ensembleUtilMock = mockStatic(EnsembleUtility.class)) {
            ensembleUtilMock.when(() -> EnsembleUtility.getResourceEnsembleStatus(anyList(), anyList()))
                .thenReturn(List.of(res1, res2));
            mockedVertx.when(Vertx::currentContext).thenReturn(context);
            when(context.owner()).thenReturn(vertx);
            ensembleHandler.validateExistingEnsemble(rc)
                .subscribe(result -> {
                        assertThat(result.getJsonObject(0).getLong("resource_id")).isEqualTo(1L);
                        assertThat(result.getJsonObject(0).getBoolean("is_valid")).isEqualTo(true);
                        assertThat(result.getJsonObject(1).getLong("resource_id")).isEqualTo(2L);
                        assertThat(result.getJsonObject(1).getBoolean("is_valid")).isEqualTo(false);
                        testContext.completeNow();
                    },
                    throwable -> testContext.failNow("method has thrown exception")
                );
        }
    }

    @Test
    void validateAllExistingEnsembles(VertxTestContext testContext) {
        JsonArray ensembles = new JsonArray(List.of(JsonObject.mapFrom(e1), JsonObject.mapFrom(e2)));
        GetOneEnsemble getOneEnsemble1 = TestDTOProvider.createGetOneEnsemble();
        GetOneEnsemble getOneEnsemble2 = TestDTOProvider.createGetOneEnsemble();
        getOneEnsemble2.setEnsembleId(2L);
        JsonObject getOneEnsembleJson1 = JsonObject.mapFrom(getOneEnsemble1);
        JsonObject getOneEnsembleJson2 = JsonObject.mapFrom(getOneEnsemble2);
        ResourceEnsembleStatus res1 = new ResourceEnsembleStatus(1L, true);
        ResourceEnsembleStatus res2 = new ResourceEnsembleStatus(2L, false);
        JsonArray jsonResult1 = new JsonArray(List.of(JsonObject.mapFrom(res1), JsonObject.mapFrom(res2)));
        JsonArray jsonResult2 = new JsonArray(List.of(JsonObject.mapFrom(res1)));
        JsonObject r1Json = JsonObject.mapFrom(r1);
        JsonObject r2Json = JsonObject.mapFrom(r2);
        JsonArray filterResources1 = new JsonArray(List.of(r1Json, r2Json));
        JsonArray filterResources2 = new JsonArray(List.of(r1Json));
        when(ensembleService.findAll()).thenReturn(Single.just(ensembles));
        when(ensembleService.findOneByIdAndAccountId(1L, 2L)).thenReturn(Single.just(getOneEnsembleJson1));
        when(ensembleService.findOneByIdAndAccountId(2L, 2L)).thenReturn(Single.just(getOneEnsembleJson2));
        when(resourceService.findAllByNonMonitoredSLOs(getOneEnsembleJson1)).thenReturn(Single.just(filterResources1));
        when(resourceService.findAllByNonMonitoredSLOs(getOneEnsembleJson2)).thenReturn(Single.just(filterResources2));
        when(ensembleService.updateEnsembleStatusMap(Map.of("1", jsonResult1, "2", jsonResult2)))
            .thenReturn(Completable.complete());

        try (MockedStatic<Vertx> mockedVertx = mockStatic(Vertx.class);
             MockedConstruction<ConfigUtility> ignoreConfig = Mockprovider.mockConfig(config);
             MockedConstruction<SLOValidator> ignoreSLOValidator1 = SLOMockProvider
                    .mockSLOValidatorFilter(List.of(getOneEnsemble1, getOneEnsemble2), List.of(Set.of(r1, r2),
                        Set.of(r1)));
             MockedStatic<EnsembleUtility> ensembleUtilMock = mockStatic(EnsembleUtility.class)) {
            ensembleUtilMock.when(() -> EnsembleUtility.getResourceEnsembleStatus(anyList(),
                anyList())).thenReturn(List.of(res1, res2)).thenReturn(List.of(res1));
            mockedVertx.when(Vertx::currentContext).thenReturn(context);
            when(context.owner()).thenReturn(vertx);
            ensembleHandler.validateAllExistingEnsembles()
                .subscribe(testContext::completeNow, throwable -> testContext.failNow("method has thrown exception"));
        }
    }
}
