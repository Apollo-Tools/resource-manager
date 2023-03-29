package at.uibk.dps.rm.handler.resource;

import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.entity.model.ResourceType;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.testutil.RoutingContextMockHelper;
import at.uibk.dps.rm.testutil.TestObjectProvider;
import at.uibk.dps.rm.util.JsonMapperConfig;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.ext.web.RoutingContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.when;

@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class ResourceHandlerTest {

    private ResourceHandler resourceHandler;

    @Mock
    private ResourceChecker resourceChecker;

    @Mock
    private ResourceTypeChecker resourceTypeChecker;

    @Mock
    private RoutingContext rc;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        resourceHandler = new ResourceHandler(resourceChecker, resourceTypeChecker);
    }

    @Test
    void postOneValid(VertxTestContext testContext) {
        ResourceType rt = TestObjectProvider.createResourceType(1L, "vm");
        Resource resource = TestObjectProvider.createResource(1L, rt);
        JsonObject requestBody = JsonObject.mapFrom(resource);
        requestBody.remove("resource_id");

        RoutingContextMockHelper.mockBody(rc, requestBody);
        when(resourceTypeChecker.checkExistsOne(1L)).thenReturn(Completable.complete());
        when(resourceChecker.submitCreate(requestBody)).thenReturn(Single.just(JsonObject.mapFrom(resource)));

        resourceHandler.postOne(rc)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.getJsonObject("resource_type").getLong("type_id")).isEqualTo(1L);
                    assertThat(result.getBoolean("is_self_managed")).isEqualTo(false);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method did throw exception"))
            );
    }

    @Test
    void postOneResourceTypeNotFound(VertxTestContext testContext) {
        JsonObject jsonObject = new JsonObject("{\"resource_type\": {\"type_id\": 1}, \"is_self_managed\": false}");

        RoutingContextMockHelper.mockBody(rc, jsonObject);
        when(resourceTypeChecker.checkExistsOne(1L)).thenReturn(Completable.error(NotFoundException::new));

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
        JsonObject r1 = JsonObject.mapFrom(TestObjectProvider.createResource(1L));
        JsonObject requestBody = new JsonObject(jsonInput);

        RoutingContextMockHelper.mockBody(rc, requestBody);
        when(rc.pathParam("id")).thenReturn(String.valueOf(entityId));
        when(resourceChecker.checkFindOne(entityId)).thenReturn(Single.just(r1));
        if (requestBody.containsKey("resource_type")) {
            when(resourceTypeChecker.checkExistsOne(1L)).thenReturn(Completable.complete());
        }
        when(resourceChecker.submitUpdate(requestBody, r1)).thenReturn(Completable.complete());

        resourceHandler.updateOne(rc)
            .blockingSubscribe(() -> {},
                throwable -> testContext.verify(() -> fail("method did throw exception"))
            );
        testContext.completeNow();
    }

    @Test
    void updateOneEntityNotFound(VertxTestContext testContext) {
        long entityId = 1L;
        JsonObject requestBody = new JsonObject("{\"resource_type\": {\"type_id\": 1}, \"is_self_managed\": false}");

        RoutingContextMockHelper.mockBody(rc, requestBody);
        when(rc.pathParam("id")).thenReturn(String.valueOf(entityId));
        when(resourceChecker.checkFindOne(entityId)).thenReturn(Single.error(NotFoundException::new));

        resourceHandler.updateOne(rc)
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void updateOneResourceTypeNotFound(VertxTestContext testContext) {
        long entityId = 1L;
        JsonObject r1 = JsonObject.mapFrom(TestObjectProvider.createResource(1L));
        JsonObject requestBody = new JsonObject("{\"resource_type\": {\"type_id\": 1}, \"is_self_managed\": false}");

        RoutingContextMockHelper.mockBody(rc, requestBody);
        when(rc.pathParam("id")).thenReturn(String.valueOf(entityId));
        when(resourceChecker.checkFindOne(entityId)).thenReturn(Single.just(r1));
        when(resourceTypeChecker.checkExistsOne(1L)).thenReturn(Completable.error(NotFoundException::new));

        resourceHandler.updateOne(rc)
           .blockingSubscribe(() -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }

    /*
    @Test
    void getResourceBySLOs(VertxTestContext testContext) {
        Resource resource1 = TestObjectProvider.createResource(1L);
        MetricValue mv1 = TestObjectProvider.createMetricValue(1L, 1L, "latency", 25.0);
        MetricValue mv2 = TestObjectProvider.createMetricValue(2L, 2L, "availability", 0.995);
        Resource resource2 = TestObjectProvider.createResource(2L);
        MetricValue mv3 = TestObjectProvider.createMetricValue(3L, 2L, "availability", 0.8);
        Resource resource3 = TestObjectProvider.createResource(3L);
        MetricValue mv4 = TestObjectProvider.createMetricValue(4L, 3L, "bandwidth", 1000);
        MetricValue mv5 = TestObjectProvider.createMetricValue(5L, 2L, "availability", 0.999);
        JsonArray resourcesJson = new JsonArray(List.of(JsonObject.mapFrom(resource1), JsonObject.mapFrom(resource2),
            JsonObject.mapFrom(resource3)));
        resourcesJson.getJsonObject(0).put("metric_values", new JsonArray(List.of(JsonObject.mapFrom(mv1), JsonObject.mapFrom(mv2))));
        resourcesJson.getJsonObject(1).put("metric_values", new JsonArray(List.of(JsonObject.mapFrom(mv3))));
        resourcesJson.getJsonObject(2).put("metric_values", new JsonArray(List.of(JsonObject.mapFrom(mv4), JsonObject.mapFrom(mv5))));
        ServiceLevelObjective slo = TestObjectProvider.createServiceLevelObjective("availability", ExpressionType.GT, 0.80);
        List<ServiceLevelObjective> serviceLevelObjectives = List.of(slo);
        GetResourcesBySLOsRequest request = TestObjectProvider.createResourceBySLOsRequest(serviceLevelObjectives, 2);
        JsonObject body = JsonObject.mapFrom(request);

        RoutingContextMockHelper.mockBody(rc, body);
        JsonObject metric1 = new JsonObject("{\"metric_id\": 1, " +
            "\"metric\": \"availability\", \"metric_type\": {\"type\": \"number\"}}");
        when(metricService.findOneByMetric("availability")).thenReturn(Single.just(metric1));
        when(resourceService.findAllByMultipleMetrics(List.of("availability"))).thenReturn(Single.just(resourcesJson));
        when(metricValueService.findOne(1L)).thenReturn(Single.just(JsonObject.mapFrom(mv1)));
        when(metricValueService.findOne(2L)).thenReturn(Single.just(JsonObject.mapFrom(mv2)));
        when(metricValueService.findOne(3L)).thenReturn(Single.just(JsonObject.mapFrom(mv3)));
        when(metricValueService.findOne(4L)).thenReturn(Single.just(JsonObject.mapFrom(mv4)));
        when(metricValueService.findOne(5L)).thenReturn(Single.just(JsonObject.mapFrom(mv5)));

        resourceHandler.getResourceBySLOs(rc)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.size()).isEqualTo(2);
                    assertThat(result.getJsonObject(0).getLong("resource_id")).isEqualTo(3L);
                    assertThat(result.getJsonObject(1).getLong("resource_id")).isEqualTo(1L);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method did throw exception"))
            );
    }*/

    @Test
    void checkUpdateResourceTypeExists(VertxTestContext testContext) {
        long typeId = 1L;
        ResourceType rt = TestObjectProvider.createResourceType(typeId, "vm");
        Resource r1 = TestObjectProvider.createResource(1L, rt);
        JsonObject r1Json = JsonObject.mapFrom(r1);
        JsonObject requestBody = new JsonObject("{\"resource_type\": {\"type_id\": " + typeId + "}}");

        when(resourceTypeChecker.checkExistsOne(typeId)).thenReturn(Completable.complete());

        resourceHandler.checkUpdateResourceTypeExists(requestBody, r1Json)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.getLong("resource_id")).isEqualTo(1L);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method did throw exception"))
            );
    }

    @Test
    void checkUpdateResourceTypeNotExists(VertxTestContext testContext) {
        long typeId = 1L;
        ResourceType rt = TestObjectProvider.createResourceType(typeId, "vm");
        Resource r1 = TestObjectProvider.createResource(1L, rt);
        JsonObject r1Json = JsonObject.mapFrom(r1);
        JsonObject requestBody = new JsonObject("{\"resource_type\": {\"type_id\": " + typeId + "}}");

        when(resourceTypeChecker.checkExistsOne(typeId)).thenReturn(Completable.error(NotFoundException::new));

        resourceHandler.checkUpdateResourceTypeExists(requestBody, r1Json)
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
        ResourceType rt = TestObjectProvider.createResourceType(typeId, "vm");
        Resource r1 = TestObjectProvider.createResource(1L, rt);
        JsonObject r1Json = JsonObject.mapFrom(r1);
        JsonObject requestBody = new JsonObject("{\"is_managed\": true}");

        resourceHandler.checkUpdateResourceTypeExists(requestBody, r1Json)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.getLong("resource_id")).isEqualTo(1L);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method did throw exception"))
            );
    }

    /*
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
            .blockingSubscribe(() -> {},
                throwable -> testContext.verify(() -> fail("method did throw exception"))
            );

        verify(metricService).findOneByMetric(metricName);
        testContext.completeNow();
    }

    @Test
    void checkServiceLevelObjectivesMetricNotExists(VertxTestContext testContext) {
        String metricName = "availability";
        ServiceLevelObjective slo = new ServiceLevelObjective(metricName,
            ExpressionType.GT, new ArrayList<>());
        Single<JsonObject> handler = new SingleHelper<JsonObject>().getEmptySingle();
        when(metricService.findOneByMetric(metricName)).thenReturn(handler);

        resourceHandler.checkServiceLevelObjectives(slo)
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did not throw exception")),
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
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did not throw exception")),
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
    void mapJsonListToResourceList() {
        JsonObject resource1 = new JsonObject("{\"resource_id\": 1}");
        JsonObject resource2 = new JsonObject("{\"resource_id\": 2}");
        List<JsonObject> resourceList = new ArrayList<>();
        resourceList.add(resource1);
        resourceList.add(resource2);

        List<Resource> result = resourceHandler.mapJsonListToResourceList(resourceList);

        assertThat(result.size()).isEqualTo(2);
        assertThat(result.get(0).getResourceId()).isEqualTo(1L);
        assertThat(result.get(1).getResourceId()).isEqualTo(2L);
    }

    @Test
    void mapJsonListToResourceListEmptyList() {
        List<JsonObject> resourceList = new ArrayList<>();

        List<Resource> result = resourceHandler.mapJsonListToResourceList(resourceList);

        assertThat(result.size()).isEqualTo(0);
    }

    @Test
    void filterAndSortResultList() {
        Resource resource1 = TestObjectProvider.createResource(1L);
        MetricValue mv1 = TestObjectProvider.createMetricValue(1L, 1L, "latency", 25.0);
        MetricValue mv2 = TestObjectProvider.createMetricValue(2L, 2L, "availability", 0.995);
        resource1.setMetricValues(Set.of(mv1, mv2));
        Resource resource2 = TestObjectProvider.createResource(2L);
        MetricValue mv3 = TestObjectProvider.createMetricValue(3L, 2L, "availability", 0.8);
        Set<MetricValue> metricValues = Set.of(mv3);
        resource2.setMetricValues(metricValues);
        Resource resource3 = TestObjectProvider.createResource(3L);
        MetricValue mv4 = TestObjectProvider.createMetricValue(4L, 3L, "bandwidth", 1000);
        MetricValue mv5 = TestObjectProvider.createMetricValue(5L, 2L, "availability", 0.999);
        resource3.setMetricValues(Set.of(mv4, mv5));
        List<Resource> resources = List.of(resource1, resource2, resource3);
        ServiceLevelObjective slo = TestObjectProvider.createServiceLevelObjective("availability", ExpressionType.GT, 0.80);
        List<ServiceLevelObjective> serviceLevelObjectives = List.of(slo);
        int limit = 2;

        JsonArray result = resourceHandler.filterAndSortResultList(resources, serviceLevelObjectives, limit);

        assertThat(result.size()).isEqualTo(2);
        assertThat(result.getJsonObject(0).getLong("resource_id")).isEqualTo(3L);
        assertThat(result.getJsonObject(1).getLong("resource_id")).isEqualTo(1L);
    }

    @Test
    void filterAndSortResultListNoMatch() {
        Resource resource1 = TestObjectProvider.createResource(1L);
        MetricValue mv1 = TestObjectProvider.createMetricValue(1L, 1L, "latency", 25.0);
        MetricValue mv2 = TestObjectProvider.createMetricValue(2L, 2L, "availability", 0.995);
        resource1.setMetricValues(Set.of(mv1, mv2));
        List<Resource> resources = List.of(resource1);
        ServiceLevelObjective slo = TestObjectProvider.createServiceLevelObjective("availability", ExpressionType.GT, 0.999);
        List<ServiceLevelObjective> serviceLevelObjectives = List.of(slo);
        int limit = 2;

        JsonArray result = resourceHandler.filterAndSortResultList(resources, serviceLevelObjectives, limit);

        assertThat(result.size()).isEqualTo(0);
    }

    @Test
    void resourceFilterBySLOValueTypeValid() {
        Resource resource = TestObjectProvider.createResource(1L);
        MetricValue mv1 = TestObjectProvider.createMetricValue(1L, 1L, "latency", 25.0);
        MetricValue mv2 = TestObjectProvider.createMetricValue(2L, 2L, "availability", "high");
        Set<MetricValue> metricValues = Set.of(mv1, mv2);
        resource.setMetricValues(metricValues);
        ServiceLevelObjective slo = TestObjectProvider.createServiceLevelObjective("availability", ExpressionType.EQ, "high");
        List<ServiceLevelObjective> serviceLevelObjectives = List.of(slo);

        boolean result = resourceHandler.resourceFilterBySLOValueType(resource, serviceLevelObjectives);

        assertThat(result).isTrue();
    }

    @Test
    void resourceFilterBySLOValueTypeNoValueTypeMatch() {
        Resource resource = TestObjectProvider.createResource(1L);
        MetricValue mv1 = TestObjectProvider.createMetricValue(1L, 1L, "latency", 25.0);
        MetricValue mv2 = TestObjectProvider.createMetricValue(2L, 2L, "availability", 0.995);
        Set<MetricValue> metricValues = Set.of(mv1, mv2);
        resource.setMetricValues(metricValues);
        ServiceLevelObjective slo = TestObjectProvider.createServiceLevelObjective("availability", ExpressionType.EQ, "high");
        List<ServiceLevelObjective> serviceLevelObjectives = List.of(slo);

        boolean result = resourceHandler.resourceFilterBySLOValueType(resource, serviceLevelObjectives);

        assertThat(result).isFalse();
    }

    @Test
    void resourceFilterBySLOValueTypeNoNameMatch() {
        Resource resource = TestObjectProvider.createResource(1L);
        MetricValue mv1 = TestObjectProvider.createMetricValue(1L, 1L, "latency", 25.0);
        MetricValue mv2 = TestObjectProvider.createMetricValue(2L, 2L, "availability", 0.995);
        Set<MetricValue> metricValues = Set.of(mv1, mv2);
        resource.setMetricValues(metricValues);
        ServiceLevelObjective slo = TestObjectProvider.createServiceLevelObjective("bandwidth", ExpressionType.EQ, "high");
        List<ServiceLevelObjective> serviceLevelObjectives = List.of(slo);

        boolean result = resourceHandler.resourceFilterBySLOValueType(resource, serviceLevelObjectives);

        assertThat(result).isTrue();
    }

    @ParameterizedTest
    @CsvSource({
        "0, 2, 1, 0, >, -1",
        "0, 1, 2, 0, >, 1",
        "2, 2, 2, 1, >, -1",
        "1, 2, 2, 2, >, 1",
        "0, 2, 2, 0, >, 0",
        "0, 2, 1, 0, <, 1",
        "0, 1, 2, 0, <, -1",
        "2, 2, 2, 1, <, 1",
        "1, 2, 2, 2, <, -1",
        "0, 2, 2, 0, <, 0",
    })
    void sortResourceBySLONumberSLOs(double v1, double v2, double v3, double v4, String symbol, int expectedResult) {
        Resource resource1 = TestObjectProvider.createResource(1L);
        MetricValue mv1 = TestObjectProvider.createMetricValue(1L, 1L, "latency", v1);
        MetricValue mv2 = TestObjectProvider.createMetricValue(2L, 2L, "availability", v2);
        resource1.setMetricValues(Set.of(mv1, mv2));
        Resource resource2 = TestObjectProvider.createResource(2L);
        MetricValue mv3 = TestObjectProvider.createMetricValue(3L, 2L, "availability", v3);
        MetricValue mv4 = TestObjectProvider.createMetricValue(4L, 1L, "latency", v4);
        Set<MetricValue> metricValues = Set.of(mv3, mv4);
        resource2.setMetricValues(metricValues);
        ServiceLevelObjective slo1 = TestObjectProvider.createServiceLevelObjective("availability",
            ExpressionType.fromString(symbol), 0.80);
        ServiceLevelObjective slo2 = TestObjectProvider.createServiceLevelObjective("latency",
            ExpressionType.fromString(symbol), 0.80);
        List<ServiceLevelObjective> serviceLevelObjectives = List.of(slo1, slo2);

        int result = resourceHandler.sortResourceBySLO(resource1, resource2, serviceLevelObjectives);

        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void sortResourceBySLONonNumberSLO() {
        Resource resource1 = TestObjectProvider.createResource(1L);
        MetricValue mv1 = TestObjectProvider.createMetricValue(1L, 1L, "region", "eu-west");
        MetricValue mv2 = TestObjectProvider.createMetricValue(2L, 2L, "availability", 0.99);
        resource1.setMetricValues(Set.of(mv1, mv2));
        Resource resource2 = TestObjectProvider.createResource(2L);
        MetricValue mv3 = TestObjectProvider.createMetricValue(3L, 2L, "region", "eu-west");
        Set<MetricValue> metricValues = Set.of(mv3);
        resource2.setMetricValues(metricValues);
        ServiceLevelObjective slo = TestObjectProvider.createServiceLevelObjective("region",
            ExpressionType.EQ, "eu-west");
        List<ServiceLevelObjective> serviceLevelObjectives = List.of(slo);

        int result = resourceHandler.sortResourceBySLO(resource1, resource2, serviceLevelObjectives);

        assertThat(result).isEqualTo(0);
    } */
}
