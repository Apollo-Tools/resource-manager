package at.uibk.dps.rm.handler.resource;

import at.uibk.dps.rm.entity.dto.slo.ExpressionType;
import at.uibk.dps.rm.entity.dto.slo.SLOValue;
import at.uibk.dps.rm.entity.dto.slo.SLOValueType;
import at.uibk.dps.rm.entity.dto.slo.ServiceLevelObjective;
import at.uibk.dps.rm.entity.model.Metric;
import at.uibk.dps.rm.entity.model.MetricValue;
import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.exception.BadInputException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.service.rxjava3.database.metric.MetricService;
import at.uibk.dps.rm.service.rxjava3.database.metric.MetricValueService;
import at.uibk.dps.rm.service.rxjava3.database.resource.ResourceService;
import at.uibk.dps.rm.service.rxjava3.database.resource.ResourceTypeService;
import at.uibk.dps.rm.testutil.RoutingContextMockHelper;
import at.uibk.dps.rm.testutil.SingleHelper;
import at.uibk.dps.rm.util.JsonMapperConfig;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.ext.web.RoutingContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class ResourceHandlerTest {

    private ResourceHandler resourceHandler;

    @Mock
    private ResourceService resourceService;

    @Mock
    private ResourceTypeService resourceTypeService;

    @Mock
    private MetricService metricService;

    @Mock
    private MetricValueService metricValueService;

    @Mock
    private RoutingContext rc;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        resourceHandler = new ResourceHandler(resourceService, resourceTypeService, metricService, metricValueService);
    }

    @Test
    void postOneValid(VertxTestContext testContext) {
        JsonObject jsonObject = new JsonObject("{\"resource_type\": {\"type_id\": 1}, \"is_self_managed\": false}");

        RoutingContextMockHelper.mockBody(rc, jsonObject);
        when(resourceTypeService.existsOneById(1L)).thenReturn(Single.just(true));
        when(resourceService.save(jsonObject)).thenReturn(Single.just(jsonObject));

        resourceHandler.postOne(rc)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.getJsonObject("resource_type").getLong("type_id")).isEqualTo(1L);
                    assertThat(result.getBoolean("is_self_managed")).isEqualTo(false);
                    verify(resourceTypeService).existsOneById(1L);
                    verify(resourceService).save(jsonObject);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method did throw exception"))
            );
    }

    @Test
    void postOneResourceTypeNotFound(VertxTestContext testContext) {
        JsonObject jsonObject = new JsonObject("{\"resource_type\": {\"type_id\": 1}, \"is_self_managed\": false}");

        RoutingContextMockHelper.mockBody(rc, jsonObject);
        when(resourceTypeService.existsOneById(1L)).thenReturn(Single.just(false));

        resourceHandler.postOne(rc)
            .subscribe(result -> testContext.verify(() -> fail("method did throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }

    @ParameterizedTest
    @ValueSource(strings = {"{\"resource_type\": {\"type_id\": 1}, \"is_self_managed\": false}",
        "{\"resource_type\": {\"type_id\": 1}}",
        "{\"is_self_managed\": false}"})
    void updateOneValid(String jsonInput, VertxTestContext testContext) {
        long entityId = 1L;
        JsonObject jsonObject = new JsonObject(jsonInput);

        RoutingContextMockHelper.mockBody(rc, jsonObject);
        when(rc.pathParam("id")).thenReturn(String.valueOf(entityId));
        when(resourceService.findOne(entityId)).thenReturn(Single.just(jsonObject));
        if (jsonObject.containsKey("resource_type")) {
            when(resourceTypeService.existsOneById(1L)).thenReturn(Single.just(true));
        }
        when(resourceService.update(jsonObject)).thenReturn(Completable.complete());

        resourceHandler.updateOne(rc)
            .andThen(Maybe.just(testContext.verify(testContext::completeNow)))
            .blockingSubscribe(result -> {},
                throwable -> testContext.verify(() -> fail("method did throw exception"))
            );

        verify(resourceService).findOne(entityId);
        if (jsonObject.containsKey("resource_type")) {
            verify(resourceTypeService).existsOneById(1L);
        }
        verify(resourceService).update(jsonObject);
    }

    @Test
    void updateOneEntityNotFound(VertxTestContext testContext) {
        long entityId = 1L;
        Single<JsonObject> handler = new SingleHelper<JsonObject>().getEmptySingle();
        JsonObject jsonObject = new JsonObject("{\"resource_type\": {\"type_id\": 1}, \"is_self_managed\": false}");

        RoutingContextMockHelper.mockBody(rc, jsonObject);
        when(rc.pathParam("id")).thenReturn(String.valueOf(entityId));
        when(resourceService.findOne(entityId)).thenReturn(handler);

        resourceHandler.updateOne(rc)
            .andThen(Maybe.empty())
            .blockingSubscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void updateOneResourceTypeNotFound(VertxTestContext testContext) {
        long entityId = 1L;
        JsonObject jsonObject = new JsonObject("{\"resource_type\": {\"type_id\": 1}, \"is_self_managed\": false}");

        RoutingContextMockHelper.mockBody(rc, jsonObject);
        when(rc.pathParam("id")).thenReturn(String.valueOf(entityId));
        when(resourceService.findOne(entityId)).thenReturn(Single.just(jsonObject));
        when(resourceTypeService.existsOneById(1L)).thenReturn(Single.just(false));

        resourceHandler.updateOne(rc)
            .andThen(Maybe.just(testContext.verify(testContext::completeNow)))
            .blockingSubscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }

    //TODO: getResourceBySLOs
    @Disabled
    @Test
    void getResourceBySLOs(VertxTestContext testContext) {
        // TODO: implement
    }

    @Test
    void checkUpdateResourceTypeExists(VertxTestContext testContext) {
        long typeId = 1L;
        Resource entity = new Resource();
        entity.setResourceId(1L);
        JsonObject entityJson = JsonObject.mapFrom(entity);
        JsonObject requestBody = new JsonObject("{\"resource_type\": {\"type_id\": " + typeId + "}}");

        when(resourceTypeService.existsOneById(typeId)).thenReturn(Single.just(true));

        resourceHandler.checkUpdateResourceTypeExists(requestBody, entityJson)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.getLong("resource_id")).isEqualTo(1L);
                    verify(resourceTypeService).existsOneById(typeId);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method did throw exception"))
            );
    }

    @Test
    void checkUpdateResourceTypeNotExists(VertxTestContext testContext) {
        long typeId = 1L;
        Resource entity = new Resource();
        entity.setResourceId(1L);
        JsonObject entityJson = JsonObject.mapFrom(entity);
        JsonObject requestBody = new JsonObject("{\"resource_type\": {\"type_id\": " + typeId + "}}");

        when(resourceTypeService.existsOneById(typeId)).thenReturn(Single.just(false));

        resourceHandler.checkUpdateResourceTypeExists(requestBody, entityJson)
            .subscribe(result -> testContext.verify(() -> fail("method did throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void checkUpdateResourceTypeNotInRequestBody(VertxTestContext testContext) {
        long typeId = 1L;
        Resource entity = new Resource();
        entity.setResourceId(1L);
        JsonObject entityJson = JsonObject.mapFrom(entity);
        JsonObject requestBody = new JsonObject("{\"is_managed\": true}");

        resourceHandler.checkUpdateResourceTypeExists(requestBody, entityJson)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.getLong("resource_id")).isEqualTo(1L);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method did throw exception"))
            );
    }

    @Test
    void checkServiceLevelObjectivesValid(VertxTestContext testContext) {
        String metricName = "availability";
        List<SLOValue> sloValues = new ArrayList<>();
        SLOValue sloValue = new SLOValue();
        sloValue.setSloValueType(SLOValueType.NUMBER);
        sloValues.add(sloValue);
        ServiceLevelObjective slo = new ServiceLevelObjective(metricName,
            ExpressionType.GT, sloValues);
        JsonObject metric = new JsonObject("{\"metric_id\": 1, " +
            "\"metric\": \"" + metricName + "\", \"metric_type\": {\"type\": \"number\"}}");

        when(metricService.findOneByMetric(metricName)).thenReturn(Single.just(metric));

        resourceHandler.checkServiceLevelObjectives(slo)
            .andThen(Maybe.just(testContext.verify(testContext::completeNow)))
            .blockingSubscribe(result -> {},
                throwable -> testContext.verify(() -> fail("method did throw exception"))
            );

        verify(metricService).findOneByMetric(metricName);
    }

    @Test
    void checkServiceLevelObjectivesMetricNotExists(VertxTestContext testContext) {
        String metricName = "availability";
        ServiceLevelObjective slo = new ServiceLevelObjective(metricName,
            ExpressionType.GT, new ArrayList<>());
        Single<JsonObject> handler = new SingleHelper<JsonObject>().getEmptySingle();
        when(metricService.findOneByMetric(metricName)).thenReturn(handler);

        resourceHandler.checkServiceLevelObjectives(slo)
            .andThen(Maybe.empty())
            .blockingSubscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    verify(metricService).findOneByMetric(metricName);
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void checkServiceLevelObjectivesUnequalValueTypes(VertxTestContext testContext) {
        String metricName = "availability";
        List<SLOValue> sloValues = new ArrayList<>();
        SLOValue sloValue = new SLOValue();
        sloValue.setSloValueType(SLOValueType.BOOLEAN);
        sloValues.add(sloValue);
        ServiceLevelObjective slo = new ServiceLevelObjective(metricName,
            ExpressionType.GT, sloValues);
        JsonObject metric = new JsonObject("{\"metric_id\": 1, " +
            "\"metric\": \"" + metricName + "\", \"metric_type\": {\"type\": \"number\"}}");

        when(metricService.findOneByMetric(metricName)).thenReturn(Single.just(metric));

        resourceHandler.checkServiceLevelObjectives(slo)
            .andThen(Maybe.empty())
            .blockingSubscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    verify(metricService).findOneByMetric(metricName);
                    assertThat(throwable).isInstanceOf(BadInputException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void mapMetricValuesToResources(VertxTestContext testContext) {
        JsonObject metric1 = new JsonObject("{\"metric_value_id\": 1}");
        JsonObject metric2 = new JsonObject("{\"metric_value_id\": 2}");
        JsonObject metric3 = new JsonObject("{\"metric_value_id\": 4}");
        JsonObject resource1 = new JsonObject("{\"resource_id\": 1}");
        JsonArray jsonArray1 = new JsonArray();
        jsonArray1.add(metric1);
        jsonArray1.add(metric3);
        resource1.put("metric_values", jsonArray1);
        JsonObject resource2 = new JsonObject("{\"resource_id\": 2}");
        JsonArray jsonArray2 = new JsonArray();
        jsonArray2.add(metric2);
        resource2.put("metric_values", jsonArray2);
        List<JsonObject> resourceList = new ArrayList<>();
        resourceList.add(resource1);
        resourceList.add(resource2);
        JsonArray resources = new JsonArray(resourceList);

        when(metricValueService.findOne(1L)).thenReturn(Single.just(metric1));
        when(metricValueService.findOne(2L)).thenReturn(Single.just(metric2));
        when(metricValueService.findOne(4L)).thenReturn(Single.just(metric3));

        resourceHandler.mapMetricValuesToResources(resources)
            .subscribe(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(2);
                assertThat(result.get(0).getLong("resource_id")).isEqualTo(1L);
                assertThat(result.get(0).getJsonArray("metric_values").size()).isEqualTo(2);
                assertThat(result.get(1).getLong("resource_id")).isEqualTo(2L);
                assertThat(result.get(1).getJsonArray("metric_values").size()).isEqualTo(1);
                testContext.completeNow();
            }),
            throwable -> testContext.verify(() -> fail("method did throw exception " + throwable.getMessage()))
        );
    }

    @Test
    void mapMetricValuesToResourcesEmptyMetricValues(VertxTestContext testContext) {
        JsonObject resource1 = new JsonObject("{\"resource_id\": 1}");
        JsonArray jsonArray1 = new JsonArray();
        resource1.put("metric_values", jsonArray1);
        List<JsonObject> resourceList = new ArrayList<>();
        resourceList.add(resource1);
        JsonArray resources = new JsonArray(resourceList);

        resourceHandler.mapMetricValuesToResources(resources)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.size()).isEqualTo(1);
                    assertThat(result.get(0).getLong("resource_id")).isEqualTo(1L);
                    assertThat(result.get(0).getJsonArray("metric_values").size()).isEqualTo(0);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method did throw exception " + throwable.getMessage()))
            );
    }

    @Test
    void resourceFilterBySLOValid() {
        Resource resource = new Resource();
        resource.setResourceId(1L);
        Set<MetricValue> metricValues = new HashSet<>();
        MetricValue mv1 = new MetricValue();
        mv1.setMetricValueId(1L);
        Metric metric1 = new Metric();
        metric1.setMetric("latency");
        mv1.setMetric(metric1);
        MetricValue mv2 = new MetricValue();
        mv2.setMetricValueId(2L);
        Metric metric2 = new Metric();
        metric2.setMetric("availability");
        mv2.setMetric(metric2);
        metricValues.add(mv1);
        metricValues.add(mv2);
        resource.setMetricValues(metricValues);
        String metricName = "availability";
        List<SLOValue> sloValues = new ArrayList<>();
        SLOValue sloValue1 = new SLOValue();
        sloValue1.setSloValueType(SLOValueType.NUMBER);
        sloValues.add(sloValue1);
        ServiceLevelObjective slo = new ServiceLevelObjective(metricName,
            ExpressionType.GT, sloValues);
        List<ServiceLevelObjective> serviceLevelObjectives = new ArrayList<>();
        serviceLevelObjectives.add(slo);

        boolean result = resourceHandler.resourceFilterBySLO(resource, serviceLevelObjectives);

        assertThat(result).isFalse();
    }
}
