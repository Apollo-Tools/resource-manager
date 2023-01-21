package at.uibk.dps.rm.handler.metric;

import at.uibk.dps.rm.entity.model.Metric;
import at.uibk.dps.rm.entity.model.MetricValue;
import at.uibk.dps.rm.exception.AlreadyExistsException;
import at.uibk.dps.rm.exception.BadInputException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.service.rxjava3.database.metric.MetricService;
import at.uibk.dps.rm.service.rxjava3.database.metric.MetricValueService;
import at.uibk.dps.rm.service.rxjava3.database.resource.ResourceService;
import at.uibk.dps.rm.testutil.RoutingContextMockHelper;
import at.uibk.dps.rm.testutil.SingleHelper;
import at.uibk.dps.rm.testutil.TestObjectProvider;
import at.uibk.dps.rm.util.JsonMapperConfig;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class MetricValueHandlerTest {

    MetricValueHandler metricValueHandler;

    @Mock
    MetricValueService metricValueService;

    @Mock
    MetricService metricService;

    @Mock
    ResourceService resourceService;

    @Mock
    RoutingContext rc;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        metricValueHandler = new MetricValueHandler(metricValueService, metricService, resourceService);
    }

    @Test
    void getAllValid(VertxTestContext testContext) {
        long resourceId = 1L;
        MetricValue mv1 = TestObjectProvider.createMetricValue(1L, 1L, "latency", 25.0);
        MetricValue mv2 = TestObjectProvider.createMetricValue(2L, 2L, "availability", 0.995);
        MetricValue mv3 = TestObjectProvider.createMetricValue(3L, 3L, "bandwidth", 1000);
        JsonArray metricValues = new JsonArray(List.of(JsonObject.mapFrom(mv1),
            JsonObject.mapFrom(mv2), JsonObject.mapFrom(mv3)));

        when(rc.pathParam("id")).thenReturn(String.valueOf(resourceId));
        when(resourceService.existsOneById(resourceId)).thenReturn(Single.just(true));
        when(metricValueService.findAllByResource(resourceId, true)).thenReturn(Single.just(metricValues));

        metricValueHandler.getAll(rc)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.size()).isEqualTo(3);
                    assertThat(result.getJsonObject(0).getLong("metric_value_id")).isEqualTo(1L);
                    assertThat(result.getJsonObject(1).getLong("metric_value_id")).isEqualTo(2L);
                    assertThat(result.getJsonObject(2).getLong("metric_value_id")).isEqualTo(3L);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method did throw exception"))
            );
    }

    @Test
    void getAllResourceNotFound(VertxTestContext testContext) {
        long resourceId = 1L;

        when(rc.pathParam("id")).thenReturn(String.valueOf(resourceId));
        when(resourceService.existsOneById(resourceId)).thenReturn(Single.just(false));

        metricValueHandler.getAll(rc)
            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void getAllResourceNoMetricValues(VertxTestContext testContext) {
        long resourceId = 1L;
        JsonArray metricValues = new JsonArray();

        when(rc.pathParam("id")).thenReturn(String.valueOf(resourceId));
        when(resourceService.existsOneById(resourceId)).thenReturn(Single.just(true));
        when(metricValueService.findAllByResource(resourceId, true)).thenReturn(Single.just(metricValues));

        metricValueHandler.getAll(rc)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.size()).isEqualTo(0);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method did throw exception"))
            );
    }

    @Test
    void getAllResourceMetricValuesNotFound(VertxTestContext testContext) {
        long resourceId = 1L;
        Single<JsonArray> handler = new SingleHelper<JsonArray>().getEmptySingle();

        when(rc.pathParam("id")).thenReturn(String.valueOf(resourceId));
        when(resourceService.existsOneById(resourceId)).thenReturn(Single.just(true));
        when(metricValueService.findAllByResource(resourceId, true)).thenReturn(handler);

        metricValueHandler.getAll(rc)
            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void postAllValid(VertxTestContext testContext) {
        long resourceId = 1L;
        JsonObject value1 = new JsonObject("{\"metricId\": 1, \"value\": 4}");
        JsonObject value2 = new JsonObject("{\"metricId\": 2, \"value\": \"four\"}");
        JsonObject value3 = new JsonObject("{\"metricId\": 3, \"value\": false}");
        JsonArray requestBody = new JsonArray(List.of(value1, value2, value3));
        Metric metric1 = TestObjectProvider.createMetric(1L, "latency", 1L, "number", false);
        Metric metric2 = TestObjectProvider.createMetric(2L, "availability", 2L, "string", false);
        Metric metric3 = TestObjectProvider.createMetric(3L, "online", 3L, "boolean", false);

        RoutingContextMockHelper.mockBody(rc, requestBody);
        when(rc.pathParam("id")).thenReturn(String.valueOf(resourceId));
        when(resourceService.existsOneById(resourceId)).thenReturn(Single.just(true));
        when(metricService.findOne(1L)).thenReturn(Single.just(JsonObject.mapFrom(metric1)));
        when(metricService.findOne(2L)).thenReturn(Single.just(JsonObject.mapFrom(metric2)));
        when(metricService.findOne(3L)).thenReturn(Single.just(JsonObject.mapFrom(metric3)));
        when(metricService.existsOneById(anyLong())).thenReturn(Single.just(true));
        when(metricValueService.existsOneByResourceAndMetric(eq(resourceId), anyLong()))
            .thenReturn(Single.just(false));

        when(metricValueService.saveAll(any(JsonArray.class))).thenReturn(Completable.complete());

        metricValueHandler.postAll(rc)
            .blockingSubscribe(() -> {},
                throwable -> testContext.verify(() -> fail("method did throw exception"))
            );

        verify(metricValueService).saveAll(any(JsonArray.class));
        testContext.completeNow();
    }

    @Test
    void postAllResourceNotFound(VertxTestContext testContext) {
        long resourceId = 1L;
        JsonObject value1 = new JsonObject("{\"metricId\": 1, \"value\": 4}");
        JsonArray requestBody = new JsonArray(List.of(value1));

        RoutingContextMockHelper.mockBody(rc, requestBody);
        when(rc.pathParam("id")).thenReturn(String.valueOf(resourceId));
        when(resourceService.existsOneById(resourceId)).thenReturn(Single.just(false));

        metricValueHandler.postAll(rc)
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void postAllMetricNotFound(VertxTestContext testContext) {
        long resourceId = 1L;
        JsonObject value = new JsonObject("{\"metricId\": 1, \"value\": 4}");
        JsonArray requestBody = new JsonArray(List.of(value));
        Metric metric1 = TestObjectProvider.createMetric(1L, "latency", 1L, "number", false);

        RoutingContextMockHelper.mockBody(rc, requestBody);
        when(rc.pathParam("id")).thenReturn(String.valueOf(resourceId));
        when(resourceService.existsOneById(resourceId)).thenReturn(Single.just(true));
        when(metricService.findOne(1L)).thenReturn(Single.just(JsonObject.mapFrom(metric1)));
        when(metricService.existsOneById(1L)).thenReturn(Single.just(false));
        when(metricValueService.existsOneByResourceAndMetric(eq(resourceId), anyLong()))
            .thenReturn(Single.just(false));

        metricValueHandler.postAll(rc)
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void postAllBadInput(VertxTestContext testContext) {
        long resourceId = 1L;
        JsonObject value = new JsonObject("{\"metricId\": 1, \"value\": 4}");
        JsonArray requestBody = new JsonArray(List.of(value));
        Metric metric1 = TestObjectProvider.createMetric(1L, "latency", 1L, "string", false);

        RoutingContextMockHelper.mockBody(rc, requestBody);
        when(rc.pathParam("id")).thenReturn(String.valueOf(resourceId));
        when(resourceService.existsOneById(resourceId)).thenReturn(Single.just(true));
        when(metricService.findOne(1L)).thenReturn(Single.just(JsonObject.mapFrom(metric1)));
        when(metricService.existsOneById(1L)).thenReturn(Single.just(true));
        when(metricValueService.existsOneByResourceAndMetric(eq(resourceId), anyLong()))
            .thenReturn(Single.just(false));

        metricValueHandler.postAll(rc)
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(BadInputException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void postAllAlreadyExists(VertxTestContext testContext) {
        long resourceId = 1L;
        JsonObject value = new JsonObject("{\"metricId\": 1, \"value\": 4}");
        JsonArray requestBody = new JsonArray(List.of(value));
        Metric metric1 = TestObjectProvider.createMetric(1L, "latency", 1L, "number", false);

        RoutingContextMockHelper.mockBody(rc, requestBody);
        when(rc.pathParam("id")).thenReturn(String.valueOf(resourceId));
        when(resourceService.existsOneById(resourceId)).thenReturn(Single.just(true));
        when(metricService.findOne(1L)).thenReturn(Single.just(JsonObject.mapFrom(metric1)));
        when(metricService.existsOneById(1L)).thenReturn(Single.just(true));
        when(metricValueService.existsOneByResourceAndMetric(eq(resourceId), anyLong()))
            .thenReturn(Single.just(true));

        metricValueHandler.postAll(rc)
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(AlreadyExistsException.class);
                    testContext.completeNow();
                })
            );
    }

    @ParameterizedTest
    @CsvSource({
        "number, 4",
        "string, \"four\"",
        "boolean, true"
    })
    void updateOneValid(String metricTypeName, String value, VertxTestContext testContext) {
        long resourceId = 1;
        long metricId = 1;
        String valueString = null;
        Double valueNumber = null;
        Boolean valueBoolean = null;
        Metric metric = TestObjectProvider.createMetric(metricId, "availability", 1L,
            metricTypeName, false);
        JsonObject requestBody = new JsonObject("{\"value\": " + value + "}");

        RoutingContextMockHelper.mockBody(rc, requestBody);
        when(rc.pathParam("resourceId")).thenReturn(String.valueOf(resourceId));
        when(rc.pathParam("metricId")).thenReturn(String.valueOf(metricId));
        when(resourceService.existsOneById(resourceId)).thenReturn(Single.just(true));
        when(metricService.existsOneById(metricId)).thenReturn(Single.just(true));
        when(metricValueService.existsOneByResourceAndMetric(resourceId, metricId)).thenReturn(Single.just(true));
        when(metricService.findOne(metricId)).thenReturn(Single.just(JsonObject.mapFrom(metric)));
        switch (metricTypeName) {
            case "number":
                valueNumber = 4.0;
                break;
            case "string":
                valueString = "four";
                break;
            case "boolean":
                valueBoolean = true;
        }
        when(metricValueService.updateByResourceAndMetric(resourceId, metricId, valueString, valueNumber,
            valueBoolean))
            .thenReturn(Completable.complete());

        metricValueHandler.updateOne(rc)
            .blockingSubscribe(() -> {},
                throwable -> testContext.verify(() -> fail("method did throw exception"))
            );

        verify(metricValueService).updateByResourceAndMetric(resourceId, metricId, valueString,
            valueNumber, valueBoolean);
        testContext.completeNow();
    }

    @ParameterizedTest
    @CsvSource({
        "false, true, true",
        "true, false, true",
        "true, true, false"
    })
    void updateOneCheckUpdateDeleteMetricValueExistsFalse(boolean resourceExists, boolean metricExists, boolean metricValueExists, VertxTestContext testContext) {
        long resourceId = 1;
        long metricId = 1;
        Metric metric = TestObjectProvider.createMetric(metricId, "availability", 1L, "number", false);
        JsonObject requestBody = new JsonObject("{\"value\": 4}");

        RoutingContextMockHelper.mockBody(rc, requestBody);
        when(rc.pathParam("resourceId")).thenReturn(String.valueOf(resourceId));
        when(rc.pathParam("metricId")).thenReturn(String.valueOf(metricId));
        when(resourceService.existsOneById(resourceId)).thenReturn(Single.just(resourceExists));
        when(metricService.existsOneById(metricId)).thenReturn(Single.just(metricExists));
        when(metricValueService.existsOneByResourceAndMetric(resourceId, metricId)).thenReturn(Single.just(metricValueExists));
        when(metricService.findOne(metricId)).thenReturn(Single.just(JsonObject.mapFrom(metric)));

        metricValueHandler.updateOne(rc)
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }

    @ParameterizedTest
    @CsvSource({
        "number, 4, true",
        "number, \"four\", false",
        "number, false, false",
        "string, 4, false",
        "string, false, false",
        "boolean, 4, false",
        "boolean, \"four\", false"
    })
    void updateOneBadInput(String metricTypeName, String value, boolean isMonitored, VertxTestContext testContext) {
        long resourceId = 1;
        long metricId = 1;
        Metric metric = TestObjectProvider.createMetric(metricId, "availability", 1L, metricTypeName, isMonitored);
        JsonObject requestBody = new JsonObject("{\"value\": " + value + "}");

        RoutingContextMockHelper.mockBody(rc, requestBody);
        when(rc.pathParam("resourceId")).thenReturn(String.valueOf(resourceId));
        when(rc.pathParam("metricId")).thenReturn(String.valueOf(metricId));
        when(resourceService.existsOneById(resourceId)).thenReturn(Single.just(true));
        when(metricService.existsOneById(metricId)).thenReturn(Single.just(true));
        when(metricValueService.existsOneByResourceAndMetric(resourceId, metricId)).thenReturn(Single.just(true));
        when(metricService.findOne(metricId)).thenReturn(Single.just(JsonObject.mapFrom(metric)));

        metricValueHandler.updateOne(rc)
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(BadInputException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void deleteOneValid(VertxTestContext testContext) {
        long resourceId = 1L;
        long metricId = 1L;

        when(rc.pathParam("resourceId")).thenReturn(String.valueOf(resourceId));
        when(rc.pathParam("metricId")).thenReturn(String.valueOf(resourceId));
        when(metricService.existsOneById(metricId)).thenReturn(Single.just(true));
        when(resourceService.existsOneById(resourceId)).thenReturn(Single.just(true));
        when(metricValueService.existsOneByResourceAndMetric(resourceId, metricId)).thenReturn(Single.just(true));
        when(metricValueService.deleteByResourceAndMetric(resourceId, metricId)).thenReturn(Completable.complete());

        metricValueHandler.deleteOne(rc)
            .blockingSubscribe(() -> {},
                throwable -> testContext.verify(() -> fail("method did throw exception"))
            );

        verify(metricValueService).deleteByResourceAndMetric(resourceId, metricId);
        testContext.completeNow();
    }

    @Test
    void deleteOneMetricNotFound(VertxTestContext testContext) {
        long resourceId = 1L;
        long metricId = 1L;

        when(rc.pathParam("resourceId")).thenReturn(String.valueOf(resourceId));
        when(rc.pathParam("metricId")).thenReturn(String.valueOf(metricId));
        when(metricService.existsOneById(metricId)).thenReturn(Single.just(false));
        when(resourceService.existsOneById(resourceId)).thenReturn(Single.just(true));
        when(metricValueService.existsOneByResourceAndMetric(resourceId, metricId)).thenReturn(Single.just(true));

        metricValueHandler.deleteOne(rc)
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void deleteOneResourceNotFound(VertxTestContext testContext) {
        long resourceId = 1L;
        long metricId = 1L;

        when(rc.pathParam("resourceId")).thenReturn(String.valueOf(resourceId));
        when(rc.pathParam("metricId")).thenReturn(String.valueOf(metricId));
        when(metricService.existsOneById(metricId)).thenReturn(Single.just(true));
        when(resourceService.existsOneById(resourceId)).thenReturn(Single.just(false));
        when(metricValueService.existsOneByResourceAndMetric(resourceId, metricId)).thenReturn(Single.just(true));

        metricValueHandler.deleteOne(rc)
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void deleteOneMetricValueNotFound(VertxTestContext testContext) {
        long resourceId = 1L;
        long metricId = 1L;

        when(rc.pathParam("resourceId")).thenReturn(String.valueOf(resourceId));
        when(rc.pathParam("metricId")).thenReturn(String.valueOf(metricId));
        when(metricService.existsOneById(metricId)).thenReturn(Single.just(true));
        when(resourceService.existsOneById(resourceId)).thenReturn(Single.just(true));
        when(metricValueService.existsOneByResourceAndMetric(resourceId, metricId)).thenReturn(Single.just(false));

        metricValueHandler.deleteOne(rc)
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void checkAddMetricsResourceExistsValid(VertxTestContext testContext) {
        JsonObject value1 = new JsonObject("{\"metricId\": 1, \"value\": 4}");
        JsonObject value2 = new JsonObject("{\"metricId\": 2, \"value\": \"four\"}");
        JsonObject value3 = new JsonObject("{\"metricId\": 3, \"value\": false}");
        JsonArray requestBody = new JsonArray(List.of(value1, value2, value3));
        long resourceId = 1L;
        Metric metric1 = TestObjectProvider.createMetric(1L, "latency", 1L, "number", false);
        Metric metric2 = TestObjectProvider.createMetric(2L, "availability", 2L, "string", false);
        Metric metric3 = TestObjectProvider.createMetric(3L, "online", 3L, "boolean", false);

        when(metricService.findOne(1L)).thenReturn(Single.just(JsonObject.mapFrom(metric1)));
        when(metricService.findOne(2L)).thenReturn(Single.just(JsonObject.mapFrom(metric2)));
        when(metricService.findOne(3L)).thenReturn(Single.just(JsonObject.mapFrom(metric3)));
        when(metricService.existsOneById(anyLong())).thenReturn(Single.just(true));
        when(metricValueService.existsOneByResourceAndMetric(eq(resourceId), anyLong()))
            .thenReturn(Single.just(false));

        metricValueHandler.checkAddMetricsResourceExists(requestBody, resourceId)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.size()).isEqualTo(3);
                    assertThat(result.get(0).getValueNumber()).isEqualTo(new BigDecimal("4.0"));
                    assertThat(result.get(1).getValueString()).isEqualTo("four");
                    assertThat(result.get(2).getValueBool()).isEqualTo(false);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method did throw exception"))
            );
    }

    @Test
    void checkAddMetricsResourceExistsMetricNotExists(VertxTestContext testContext) {
        JsonObject value1 = new JsonObject("{\"metricId\": 1, \"value\": 4}");
        JsonArray requestBody = new JsonArray(List.of(value1));
        long resourceId = 1L;
        Metric metric1 = TestObjectProvider.createMetric(1L, "latency", 1L, "number", false);

        when(metricService.findOne(1L)).thenReturn(Single.just(JsonObject.mapFrom(metric1)));
        when(metricService.existsOneById(1L)).thenReturn(Single.just(false));
        when(metricValueService.existsOneByResourceAndMetric(eq(resourceId), anyLong()))
            .thenReturn(Single.just(false));

        metricValueHandler.checkAddMetricsResourceExists(requestBody, resourceId)
            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void checkAddMetricsResourceExistsDuplicated(VertxTestContext testContext) {
        JsonObject value1 = new JsonObject("{\"metricId\": 1, \"value\": 4}");
        JsonArray requestBody = new JsonArray(List.of(value1));
        long resourceId = 1L;
        Metric metric1 = TestObjectProvider.createMetric(1L, "latency", 1L, "number", false);

        when(metricService.findOne(1L)).thenReturn(Single.just(JsonObject.mapFrom(metric1)));
        when(metricService.existsOneById(1L)).thenReturn(Single.just(true));
        when(metricValueService.existsOneByResourceAndMetric(eq(resourceId), anyLong()))
            .thenReturn(Single.just(true));

        metricValueHandler.checkAddMetricsResourceExists(requestBody, resourceId)
            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(AlreadyExistsException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void checkAddMetricsResourceExistsDataTypeMismatch(VertxTestContext testContext) {
        JsonObject value1 = new JsonObject("{\"metricId\": 1, \"value\": 4}");
        JsonArray requestBody = new JsonArray(List.of(value1));
        long resourceId = 1L;
        Metric metric1 = TestObjectProvider.createMetric(1L, "latency", 1L, "string", false);

        when(metricService.findOne(1L)).thenReturn(Single.just(JsonObject.mapFrom(metric1)));
        when(metricService.existsOneById(1L)).thenReturn(Single.just(true));
        when(metricValueService.existsOneByResourceAndMetric(eq(resourceId), anyLong()))
            .thenReturn(Single.just(true));

        metricValueHandler.checkAddMetricsResourceExists(requestBody, resourceId)
            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(BadInputException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void checkUpdateDeleteMetricValueExistsAllExist(VertxTestContext testContext) {
        long resourceId = 1L;
        long metricId = 1L;

        when(resourceService.existsOneById(resourceId)).thenReturn(Single.just(true));
        when(metricService.existsOneById(metricId)).thenReturn(Single.just(true));
        when(metricValueService.existsOneByResourceAndMetric(resourceId, metricId)).thenReturn(Single.just(true));

        metricValueHandler.checkUpdateDeleteMetricValueExists(resourceId, metricId)
            .blockingSubscribe(() -> {},
                throwable -> testContext.verify(() -> fail("method did throw exception"))
            );

        verify(resourceService).existsOneById(resourceId);
        verify(metricService).existsOneById(metricId);
        verify(metricValueService).existsOneByResourceAndMetric(resourceId, metricId);
        testContext.completeNow();
    }

    @ParameterizedTest
    @CsvSource({
        "false, true, true",
        "true, false, true",
        "true, true, false"
    })
    void checkUpdateDeleteMetricValueExistsResourceNotExists(boolean resourceExists, boolean metricExists,
                                                             boolean metricValueExists, VertxTestContext testContext) {
        long resourceId = 1L;
        long metricId = 1L;

        when(resourceService.existsOneById(resourceId)).thenReturn(Single.just(resourceExists));
        when(metricService.existsOneById(metricId)).thenReturn(Single.just(metricExists));
        when(metricValueService.existsOneByResourceAndMetric(resourceId, metricId))
            .thenReturn(Single.just(metricValueExists));

        metricValueHandler.checkUpdateDeleteMetricValueExists(resourceId, metricId)
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }

    @ParameterizedTest
    @CsvSource({
        "number, 4",
        "string, \"four\"",
        "boolean, true"
    })
    void submitUpdateByValueValid(String metricTypeName, String value, VertxTestContext testContext) {
        long resourceId = 1;
        long metricId = 1;
        Map<String, Long> ids = Map.of("resourceId", resourceId, "metricId", metricId);
        String valueString = null;
        Double valueNumber = null;
        Boolean valueBoolean = null;
        JsonObject requestBody = new JsonObject("{\"value\": " + value + "}");

        switch (metricTypeName) {
            case "number":
                valueNumber = 4.0;
                break;
            case "string":
                valueString = "four";
                break;
            case "boolean":
                valueBoolean = true;
        }
        when(metricValueService.updateByResourceAndMetric(resourceId, metricId, valueString, valueNumber,
            valueBoolean))
            .thenReturn(Completable.complete());

        metricValueHandler.submitUpdateByValue(requestBody, ids)
            .blockingSubscribe(() -> {},
                throwable -> testContext.verify(() -> fail("method did throw exception"))
            );

        verify(metricValueService).updateByResourceAndMetric(resourceId, metricId, valueString, valueNumber,
            valueBoolean);
        testContext.completeNow();
    }

    @Test
    void submitUpdateByValueBadInput(VertxTestContext testContext) {
        long resourceId = 1;
        long metricId = 1;
        Map<String, Long> ids = Map.of("resourceId", resourceId, "metricId", metricId);
        JsonObject requestBody = new JsonObject("{\"value\": [\"invalid\"]}");

        metricValueHandler.submitUpdateByValue(requestBody, ids)
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(BadInputException.class);
                    testContext.completeNow();
                })
            );
    }
}
