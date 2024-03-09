package at.uibk.dps.rm.handler.resource;

import at.uibk.dps.rm.entity.dto.SLORequest;
import at.uibk.dps.rm.entity.dto.config.ConfigDTO;
import at.uibk.dps.rm.entity.dto.resource.SubResourceDTO;
import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.service.rxjava3.database.metric.MetricService;
import at.uibk.dps.rm.service.rxjava3.database.resource.ResourceService;
import at.uibk.dps.rm.service.rxjava3.monitoring.metricquery.MetricQueryService;
import at.uibk.dps.rm.testutil.RoutingContextMockHelper;
import at.uibk.dps.rm.testutil.mockprovider.Mockprovider;
import at.uibk.dps.rm.testutil.mockprovider.SLOMockProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestConfigProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestDTOProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestResourceProvider;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * Implements tests for the {@link ResourceHandler} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class ResourceHandlerTest {

    private ResourceHandler resourceHandler;

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

    private Resource rMain, rSub1, rSub2, rSub3;
    private ConfigDTO config;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        resourceHandler = new ResourceHandler(resourceService, metricService, metricQueryService);
        rMain = TestResourceProvider.createClusterWithoutNodes(1L, "main");
        rSub1 = new SubResourceDTO(TestResourceProvider.createSubResource(2L, "sub1", rMain.getMain()));
        rSub2 = new SubResourceDTO(TestResourceProvider.createSubResource(3L, "sub2", rMain.getMain()));
        rSub3 = TestResourceProvider.createSubResource(4L, "sub3", rMain.getMain());
        rMain.setIsLocked(false);
        rSub1.setIsLocked(false);
        rSub2.setIsLocked(false);
        rSub3.setIsLocked(false);
        config = TestConfigProvider.getConfigDTO();
    }

    @Test
    void getAllSubResourcesByMainResource(VertxTestContext testContext) {
        JsonArray jsonResult = new JsonArray(List.of(JsonObject.mapFrom(rSub1), JsonObject.mapFrom(rSub2)));

        when(rc.pathParam("id")).thenReturn(String.valueOf(rMain.getResourceId()));
        when(resourceService.findAllSubResources(rMain.getResourceId())).thenReturn(Single.just(jsonResult));

        resourceHandler.getAllSubResourcesByMainResource(rc)
            .subscribe(result -> testContext.verify(() -> {
                assertThat(result.getJsonObject(0).getLong("resource_id")).isEqualTo(2L);
                assertThat(result.getJsonObject(1).getLong("resource_id")).isEqualTo(3L);
                testContext.completeNow();
            }));
    }

    @Test
    void getAllBySLOsMainAndSub(VertxTestContext testContext) {
        SLORequest request = TestDTOProvider.createSLORequest();
        JsonObject body = JsonObject.mapFrom(request);
        JsonArray filterResources = new JsonArray(List.of(JsonObject.mapFrom(rMain), JsonObject.mapFrom(rSub1),
            JsonObject.mapFrom(rSub2)));

        RoutingContextMockHelper.mockBody(rc, body);
        when(metricService.checkMetricTypeForSLOs(body)).thenReturn(Completable.complete());
        when(resourceService.findAllByNonMonitoredSLOs(body)).thenReturn(Single.just(filterResources));

        try (MockedStatic<Vertx> mockedVertx = mockStatic(Vertx.class);
                MockedConstruction<ConfigUtility> ignoreConfig = Mockprovider.mockConfig(config);
                MockedConstruction<SLOValidator> ignoreSLOValidator = SLOMockProvider
                    .mockSLOValidatorFilterAndSort(request, rMain, rSub2, 1)) {
            mockedVertx.when(Vertx::currentContext).thenReturn(context);
            when(context.owner()).thenReturn(vertx);
            resourceHandler.getAllBySLOs(rc)
                .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.getJsonObject(0).getLong("resource_id")).isEqualTo(3L);
                    assertThat(result.getJsonObject(1).getLong("resource_id")).isEqualTo(1L);
                    testContext.completeNow();
                }),
                    throwable -> testContext.failNow("method has thrown exception"));
        }
    }

    @Test
    void getAllBySLOsSubOnly(VertxTestContext testContext) {
        SLORequest request = TestDTOProvider.createSLORequest();
        JsonObject body = JsonObject.mapFrom(request);
        JsonArray filterResources = new JsonArray(List.of(JsonObject.mapFrom(rSub1), JsonObject.mapFrom(rSub2),
            JsonObject.mapFrom(rSub3)));

        RoutingContextMockHelper.mockBody(rc, body);
        when(metricService.checkMetricTypeForSLOs(body)).thenReturn(Completable.complete());
        when(resourceService.findAllByNonMonitoredSLOs(body)).thenReturn(Single.just(filterResources));

        try (MockedStatic<Vertx> mockedVertx = mockStatic(Vertx.class);
                MockedConstruction<ConfigUtility> ignoreConfig = Mockprovider.mockConfig(config);
                MockedConstruction<SLOValidator> ignoreSLOValidator = SLOMockProvider
                    .mockSLOValidatorFilterAndSort(request, rSub3, rSub1, -1)) {
            mockedVertx.when(Vertx::currentContext).thenReturn(context);
            when(context.owner()).thenReturn(vertx);
            resourceHandler.getAllBySLOs(rc)
                .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.getJsonObject(0).getLong("resource_id")).isEqualTo(4L);
                    assertThat(result.getJsonObject(1).getLong("resource_id")).isEqualTo(2L);
                    testContext.completeNow();
                }),
                    throwable -> testContext.failNow("method has thrown exception"));
        }
    }

    @Test
    void getAllLockedByDeployment(VertxTestContext testContext) {
        rMain.setIsLocked(true);
        rSub1.setIsLocked(true);
        rSub2.setIsLocked(true);
        JsonArray jsonResult = new JsonArray(List.of(JsonObject.mapFrom(rMain), JsonObject.mapFrom(rSub1),
            JsonObject.mapFrom(rSub2)));

        when(rc.pathParam("id")).thenReturn(String.valueOf(rMain.getResourceId()));
        when(resourceService.findAllLockedByDeployment(1L)).thenReturn(Single.just(jsonResult));

        resourceHandler.getAllLockedByDeployment(rc)
            .subscribe(result -> testContext.verify(() -> {
                assertThat(result.getJsonObject(0).getLong("resource_id")).isEqualTo(1L);
                assertThat(result.getJsonObject(1).getLong("resource_id")).isEqualTo(2L);
                assertThat(result.getJsonObject(2).getLong("resource_id")).isEqualTo(3L);
                testContext.completeNow();
            }));
    }
}
