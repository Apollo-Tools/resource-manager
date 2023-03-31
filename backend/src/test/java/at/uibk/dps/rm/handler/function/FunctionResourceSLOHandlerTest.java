package at.uibk.dps.rm.handler.function;

import at.uibk.dps.rm.entity.dto.GetResourcesBySLOsRequest;
import at.uibk.dps.rm.entity.dto.slo.ExpressionType;
import at.uibk.dps.rm.entity.dto.slo.ServiceLevelObjective;
import at.uibk.dps.rm.entity.model.Function;
import at.uibk.dps.rm.entity.model.MetricValue;
import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.exception.BadInputException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.handler.metric.MetricChecker;
import at.uibk.dps.rm.handler.metric.MetricValueChecker;
import at.uibk.dps.rm.handler.resource.ResourceChecker;
import at.uibk.dps.rm.testutil.RoutingContextMockHelper;
import at.uibk.dps.rm.testutil.objectprovider.*;
import at.uibk.dps.rm.util.JsonMapperConfig;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.ext.web.RoutingContext;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class FunctionResourceSLOHandlerTest {

    private FunctionResourceSLOHandler handler;

    @Mock
    private FunctionChecker functionChecker;

    @Mock
    private ResourceChecker resourceChecker;

    @Mock
    private MetricChecker metricChecker;

    @Mock
    private MetricValueChecker metricValueChecker;

    @Mock
    private RoutingContext rc;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        handler = new FunctionResourceSLOHandler(functionChecker, resourceChecker, metricChecker, metricValueChecker);
    }

    @Test
    void getResourceBySLOs(VertxTestContext testContext) {
        long functionId = 1L;
        Function f1 = TestFunctionProvider.createFunction(1L);
        Resource resource1 = TestResourceProvider.createResource(1L);
        MetricValue mv1 = TestMetricProvider.createMetricValue(1L, 1L, "latency", 25.0);
        MetricValue mv2 = TestMetricProvider.createMetricValue(2L, 2L, "availability", 0.995);
        Resource resource2 = TestResourceProvider.createResource(2L);
        MetricValue mv3 = TestMetricProvider.createMetricValue(3L, 2L, "availability", 0.8);
        Resource resource3 = TestResourceProvider.createResource(3L);
        MetricValue mv4 = TestMetricProvider.createMetricValue(4L, 3L, "bandwidth", 1000);
        MetricValue mv5 = TestMetricProvider.createMetricValue(5L, 2L, "availability", 0.999);
        JsonArray resourcesJson = new JsonArray(List.of(JsonObject.mapFrom(resource1), JsonObject.mapFrom(resource2),
            JsonObject.mapFrom(resource3)));
        resourcesJson.getJsonObject(0).put("metric_values", new JsonArray(List.of(JsonObject.mapFrom(mv1), JsonObject.mapFrom(mv2))));
        resourcesJson.getJsonObject(1).put("metric_values", new JsonArray(List.of(JsonObject.mapFrom(mv3))));
        resourcesJson.getJsonObject(2).put("metric_values", new JsonArray(List.of(JsonObject.mapFrom(mv4), JsonObject.mapFrom(mv5))));
        ServiceLevelObjective slo = TestDTOProvider.createServiceLevelObjective("availability", ExpressionType.GT, 0.80);
        List<ServiceLevelObjective> serviceLevelObjectives = List.of(slo);
        GetResourcesBySLOsRequest request = TestRequestProvider.createResourceBySLOsRequest(serviceLevelObjectives, 2,
            List.of(1L, 2L), List.of(2L, 3L), List.of("US", "EU"));
        JsonObject body = JsonObject.mapFrom(request);

        RoutingContextMockHelper.mockBody(rc, body);
        when(metricChecker.checkServiceLevelObjectives(any(ServiceLevelObjective.class)))
            .thenReturn(Completable.complete());
        when(rc.pathParam("id")).thenReturn(String.valueOf(functionId));
        when(functionChecker.checkFindOne(functionId)).thenReturn(Single.just(JsonObject.mapFrom(f1)));
        when(resourceChecker.checkFindAllBySLOs(functionId, List.of(slo.getName()), request.getRegions(),
            request.getProviders(), request.getResourceTypes()))
            .thenReturn(Single.just(resourcesJson));
        when(metricValueChecker.checkFindOne(mv1.getMetricValueId())).thenReturn(Single.just(JsonObject.mapFrom(mv1)));
        when(metricValueChecker.checkFindOne(mv2.getMetricValueId())).thenReturn(Single.just(JsonObject.mapFrom(mv2)));
        when(metricValueChecker.checkFindOne(mv3.getMetricValueId())).thenReturn(Single.just(JsonObject.mapFrom(mv3)));
        when(metricValueChecker.checkFindOne(mv4.getMetricValueId())).thenReturn(Single.just(JsonObject.mapFrom(mv4)));
        when(metricValueChecker.checkFindOne(mv5.getMetricValueId())).thenReturn(Single.just(JsonObject.mapFrom(mv5)));

        handler.getFunctionResourceBySLOs(rc)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.size()).isEqualTo(2);
                    assertThat(result.getJsonObject(0).getLong("resource_id")).isEqualTo(3L);
                    assertThat(result.getJsonObject(1).getLong("resource_id")).isEqualTo(1L);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void getResourceBySLOsMetricValueNotExists(VertxTestContext testContext) {
        long functionId = 1L;
        Function f1 = TestFunctionProvider.createFunction(1L);
        Resource resource1 = TestResourceProvider.createResource(1L);
        MetricValue mv1 = TestMetricProvider.createMetricValue(1L, 1L, "latency", 25.0);
        MetricValue mv2 = TestMetricProvider.createMetricValue(2L, 2L, "availability", 0.995);
        JsonArray resourcesJson = new JsonArray(List.of(JsonObject.mapFrom(resource1)));
        resourcesJson.getJsonObject(0).put("metric_values", new JsonArray(List.of(JsonObject.mapFrom(mv1), JsonObject.mapFrom(mv2))));
        ServiceLevelObjective slo = TestDTOProvider.createServiceLevelObjective("availability", ExpressionType.GT, 0.80);
        List<ServiceLevelObjective> serviceLevelObjectives = List.of(slo);
        GetResourcesBySLOsRequest request = TestRequestProvider.createResourceBySLOsRequest(serviceLevelObjectives, 2,
            List.of(1L, 2L), List.of(2L, 3L), List.of("US", "EU"));
        JsonObject body = JsonObject.mapFrom(request);

        RoutingContextMockHelper.mockBody(rc, body);
        when(metricChecker.checkServiceLevelObjectives(any(ServiceLevelObjective.class)))
            .thenReturn(Completable.complete());
        when(rc.pathParam("id")).thenReturn(String.valueOf(functionId));
        when(functionChecker.checkFindOne(functionId)).thenReturn(Single.just(JsonObject.mapFrom(f1)));
        when(resourceChecker.checkFindAllBySLOs(functionId, List.of(slo.getName()), request.getRegions(),
            request.getProviders(), request.getResourceTypes()))
            .thenReturn(Single.just(resourcesJson));
        when(metricValueChecker.checkFindOne(mv1.getMetricValueId())).thenReturn(Single.error(NotFoundException::new));

        handler.getFunctionResourceBySLOs(rc)
            .subscribe(result -> testContext.verify(() -> Assertions.fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void getResourceBySLOsResourcesNotExists(VertxTestContext testContext) {
        long functionId = 1L;
        Function f1 = TestFunctionProvider.createFunction(1L);
        ServiceLevelObjective slo = TestDTOProvider.createServiceLevelObjective("availability", ExpressionType.GT, 0.80);
        List<ServiceLevelObjective> serviceLevelObjectives = List.of(slo);
        GetResourcesBySLOsRequest request = TestRequestProvider.createResourceBySLOsRequest(serviceLevelObjectives, 2,
            List.of(1L, 2L), List.of(2L, 3L), List.of("US", "EU"));
        JsonObject body = JsonObject.mapFrom(request);

        RoutingContextMockHelper.mockBody(rc, body);
        when(metricChecker.checkServiceLevelObjectives(any(ServiceLevelObjective.class)))
            .thenReturn(Completable.complete());
        when(rc.pathParam("id")).thenReturn(String.valueOf(functionId));
        when(functionChecker.checkFindOne(functionId)).thenReturn(Single.just(JsonObject.mapFrom(f1)));
        when(resourceChecker.checkFindAllBySLOs(functionId, List.of(slo.getName()), request.getRegions(),
            request.getProviders(), request.getResourceTypes()))
            .thenReturn(Single.error(NotFoundException::new));

        handler.getFunctionResourceBySLOs(rc)
            .subscribe(result -> testContext.verify(() -> Assertions.fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void getResourceBySLOsFunctionNotExists(VertxTestContext testContext) {
        long functionId = 1L;
        ServiceLevelObjective slo = TestDTOProvider.createServiceLevelObjective("availability", ExpressionType.GT, 0.80);
        List<ServiceLevelObjective> serviceLevelObjectives = List.of(slo);
        GetResourcesBySLOsRequest request = TestRequestProvider.createResourceBySLOsRequest(serviceLevelObjectives, 2,
            List.of(1L, 2L), List.of(2L, 3L), List.of("US", "EU"));
        JsonObject body = JsonObject.mapFrom(request);

        RoutingContextMockHelper.mockBody(rc, body);
        when(metricChecker.checkServiceLevelObjectives(any(ServiceLevelObjective.class)))
            .thenReturn(Completable.complete());
        when(rc.pathParam("id")).thenReturn(String.valueOf(functionId));
        when(functionChecker.checkFindOne(functionId)).thenReturn(Single.error(NotFoundException::new));

        handler.getFunctionResourceBySLOs(rc)
            .subscribe(result -> testContext.verify(() -> Assertions.fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void getResourceBySLOsSLOsNotValid(VertxTestContext testContext) {
        long functionId = 1L;
        ServiceLevelObjective slo = TestDTOProvider.createServiceLevelObjective("availability", ExpressionType.GT, 0.80);
        List<ServiceLevelObjective> serviceLevelObjectives = List.of(slo);
        GetResourcesBySLOsRequest request = TestRequestProvider.createResourceBySLOsRequest(serviceLevelObjectives, 2,
            List.of(1L, 2L), List.of(2L, 3L), List.of("US", "EU"));
        JsonObject body = JsonObject.mapFrom(request);

        RoutingContextMockHelper.mockBody(rc, body);
        when(metricChecker.checkServiceLevelObjectives(any(ServiceLevelObjective.class)))
            .thenReturn(Completable.error(BadInputException::new));
        when(rc.pathParam("id")).thenReturn(String.valueOf(functionId));

        handler.getFunctionResourceBySLOs(rc)
            .subscribe(result -> testContext.verify(() -> Assertions.fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
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

        when(metricValueChecker.checkFindOne(1L)).thenReturn(Single.just(metric1));
        when(metricValueChecker.checkFindOne(2L)).thenReturn(Single.just(metric2));
        when(metricValueChecker.checkFindOne(4L)).thenReturn(Single.just(metric3));

        handler.mapMetricValuesToResources(resources)
            .subscribe(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(2);
                assertThat(result.get(0).getLong("resource_id")).isEqualTo(1L);
                assertThat(result.get(0).getJsonArray("metric_values").size()).isEqualTo(2);
                assertThat(result.get(1).getLong("resource_id")).isEqualTo(2L);
                assertThat(result.get(1).getJsonArray("metric_values").size()).isEqualTo(1);
                testContext.completeNow();
            }),
            throwable -> testContext.verify(() -> fail("method has thrown exception"))
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

        handler.mapMetricValuesToResources(resources)
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
    void mapMetricValuesToResourcesMetricValueNotFound(VertxTestContext testContext) {
        JsonObject metric1 = new JsonObject("{\"metric_value_id\": 1}");
        JsonObject resource1 = new JsonObject("{\"resource_id\": 1}");
        JsonArray jsonArray1 = new JsonArray();
        jsonArray1.add(metric1);
        resource1.put("metric_values", jsonArray1);
        List<JsonObject> resourceList = new ArrayList<>();
        resourceList.add(resource1);
        JsonArray resources = new JsonArray(resourceList);

        when(metricValueChecker.checkFindOne(1L)).thenReturn(Single.error(NotFoundException::new));

        handler.mapMetricValuesToResources(resources)
            .subscribe(result -> testContext.verify(() -> Assertions.fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void filterAndSortResultList() {
        Resource resource1 = TestResourceProvider.createResource(1L);
        MetricValue mv1 = TestMetricProvider.createMetricValue(1L, 1L, "latency", 25.0);
        MetricValue mv2 = TestMetricProvider.createMetricValue(2L, 2L, "availability", 0.995);
        resource1.setMetricValues(Set.of(mv1, mv2));
        Resource resource2 = TestResourceProvider.createResource(2L);
        MetricValue mv3 = TestMetricProvider.createMetricValue(3L, 2L, "availability", 0.8);
        Set<MetricValue> metricValues = Set.of(mv3);
        resource2.setMetricValues(metricValues);
        Resource resource3 = TestResourceProvider.createResource(3L);
        MetricValue mv4 = TestMetricProvider.createMetricValue(4L, 3L, "bandwidth", 1000);
        MetricValue mv5 = TestMetricProvider.createMetricValue(5L, 2L, "availability", 0.999);
        resource3.setMetricValues(Set.of(mv4, mv5));
        List<Resource> resources = List.of(resource1, resource2, resource3);
        ServiceLevelObjective slo = TestDTOProvider.createServiceLevelObjective("availability", ExpressionType.GT, 0.80);
        List<ServiceLevelObjective> serviceLevelObjectives = List.of(slo);
        int limit = 2;

        JsonArray result = handler.filterAndSortResultList(resources, serviceLevelObjectives, limit);

        assertThat(result.size()).isEqualTo(2);
        assertThat(result.getJsonObject(0).getLong("resource_id")).isEqualTo(3L);
        assertThat(result.getJsonObject(1).getLong("resource_id")).isEqualTo(1L);
    }

    @Test
    void filterAndSortResultListNoMatch() {
        Resource resource1 = TestResourceProvider.createResource(1L);
        MetricValue mv1 = TestMetricProvider.createMetricValue(1L, 1L, "latency", 25.0);
        MetricValue mv2 = TestMetricProvider.createMetricValue(2L, 2L, "availability", 0.995);
        resource1.setMetricValues(Set.of(mv1, mv2));
        List<Resource> resources = List.of(resource1);
        ServiceLevelObjective slo = TestDTOProvider.createServiceLevelObjective("availability", ExpressionType.GT, 0.999);
        List<ServiceLevelObjective> serviceLevelObjectives = List.of(slo);
        int limit = 2;

        JsonArray result = handler.filterAndSortResultList(resources, serviceLevelObjectives, limit);

        assertThat(result.size()).isEqualTo(0);
    }

    @Test
    void resourceFilterBySLOValueTypeValid() {
        Resource resource = TestResourceProvider.createResource(1L);
        MetricValue mv1 = TestMetricProvider.createMetricValue(1L, 1L, "latency", 25.0);
        MetricValue mv2 = TestMetricProvider.createMetricValue(2L, 2L, "availability", "high");
        Set<MetricValue> metricValues = Set.of(mv1, mv2);
        resource.setMetricValues(metricValues);
        ServiceLevelObjective slo = TestDTOProvider.createServiceLevelObjective("availability", ExpressionType.EQ, "high");
        List<ServiceLevelObjective> serviceLevelObjectives = List.of(slo);

        boolean result = handler.resourceFilterBySLOValueType(resource, serviceLevelObjectives);

        assertThat(result).isTrue();
    }

    @Test
    void resourceFilterBySLOValueTypeNoValueTypeMatch() {
        Resource resource = TestResourceProvider.createResource(1L);
        MetricValue mv1 = TestMetricProvider.createMetricValue(1L, 1L, "latency", 25.0);
        MetricValue mv2 = TestMetricProvider.createMetricValue(2L, 2L, "availability", 0.995);
        Set<MetricValue> metricValues = Set.of(mv1, mv2);
        resource.setMetricValues(metricValues);
        ServiceLevelObjective slo = TestDTOProvider.createServiceLevelObjective("availability", ExpressionType.EQ, "high");
        List<ServiceLevelObjective> serviceLevelObjectives = List.of(slo);

        boolean result = handler.resourceFilterBySLOValueType(resource, serviceLevelObjectives);

        assertThat(result).isFalse();
    }

    @Test
    void resourceFilterBySLOValueTypeNoNameMatch() {
        Resource resource = TestResourceProvider.createResource(1L);
        MetricValue mv1 = TestMetricProvider.createMetricValue(1L, 1L, "latency", 25.0);
        MetricValue mv2 = TestMetricProvider.createMetricValue(2L, 2L, "availability", 0.995);
        Set<MetricValue> metricValues = Set.of(mv1, mv2);
        resource.setMetricValues(metricValues);
        ServiceLevelObjective slo = TestDTOProvider.createServiceLevelObjective("bandwidth", ExpressionType.EQ, "high");
        List<ServiceLevelObjective> serviceLevelObjectives = List.of(slo);

        boolean result = handler.resourceFilterBySLOValueType(resource, serviceLevelObjectives);

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
        Resource resource1 = TestResourceProvider.createResource(1L);
        MetricValue mv1 = TestMetricProvider.createMetricValue(1L, 1L, "latency", v1);
        MetricValue mv2 = TestMetricProvider.createMetricValue(2L, 2L, "availability", v2);
        resource1.setMetricValues(Set.of(mv1, mv2));
        Resource resource2 = TestResourceProvider.createResource(2L);
        MetricValue mv3 = TestMetricProvider.createMetricValue(3L, 2L, "availability", v3);
        MetricValue mv4 = TestMetricProvider.createMetricValue(4L, 1L, "latency", v4);
        Set<MetricValue> metricValues = Set.of(mv3, mv4);
        resource2.setMetricValues(metricValues);
        ServiceLevelObjective slo1 = TestDTOProvider.createServiceLevelObjective("availability",
            ExpressionType.fromString(symbol), 0.80);
        ServiceLevelObjective slo2 = TestDTOProvider.createServiceLevelObjective("latency",
            ExpressionType.fromString(symbol), 0.80);
        List<ServiceLevelObjective> serviceLevelObjectives = List.of(slo1, slo2);

        int result = handler.sortResourceBySLO(resource1, resource2, serviceLevelObjectives);

        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void sortResourceBySLONonNumberSLO() {
        Resource resource1 = TestResourceProvider.createResource(1L);
        MetricValue mv1 = TestMetricProvider.createMetricValue(1L, 1L, "region", "eu-west");
        MetricValue mv2 = TestMetricProvider.createMetricValue(2L, 2L, "availability", 0.99);
        resource1.setMetricValues(Set.of(mv1, mv2));
        Resource resource2 = TestResourceProvider.createResource(2L);
        MetricValue mv3 = TestMetricProvider.createMetricValue(3L, 2L, "region", "eu-west");
        Set<MetricValue> metricValues = Set.of(mv3);
        resource2.setMetricValues(metricValues);
        ServiceLevelObjective slo = TestDTOProvider.createServiceLevelObjective("region",
            ExpressionType.EQ, "eu-west");
        List<ServiceLevelObjective> serviceLevelObjectives = List.of(slo);

        int result = handler.sortResourceBySLO(resource1, resource2, serviceLevelObjectives);

        assertThat(result).isEqualTo(0);
    }
}
