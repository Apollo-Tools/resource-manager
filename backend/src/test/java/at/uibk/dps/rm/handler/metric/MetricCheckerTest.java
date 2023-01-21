package at.uibk.dps.rm.handler.metric;

import at.uibk.dps.rm.entity.dto.slo.ExpressionType;
import at.uibk.dps.rm.entity.dto.slo.ServiceLevelObjective;
import at.uibk.dps.rm.entity.model.Metric;
import at.uibk.dps.rm.entity.model.MetricType;
import at.uibk.dps.rm.entity.model.MetricValue;
import at.uibk.dps.rm.exception.AlreadyExistsException;
import at.uibk.dps.rm.exception.BadInputException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.service.rxjava3.database.metric.MetricService;
import at.uibk.dps.rm.testutil.SingleHelper;
import at.uibk.dps.rm.testutil.TestObjectProvider;
import at.uibk.dps.rm.util.JsonMapperConfig;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class MetricCheckerTest {

    MetricChecker metricChecker;

    @Mock
    MetricService metricService;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        metricChecker = new MetricChecker(metricService);
    }

    @Test
    void checkForDuplicateEntityNotExists(VertxTestContext testContext) {
        String metricName = "region";
        Metric metric = TestObjectProvider.createMetric(1L, metricName);
        JsonObject entity = JsonObject.mapFrom(metric);

        when(metricService.existsOneByMetric(metricName)).thenReturn(Single.just(false));

        metricChecker.checkForDuplicateEntity(entity)
            .blockingSubscribe(() -> {
                },
                throwable -> testContext.verify(() -> fail("method did throw exception"))
            );

        verify(metricService).existsOneByMetric(metricName);
        testContext.completeNow();
    }

    @Test
    void checkForDuplicateEntityExists(VertxTestContext testContext) {
        String metricName = "region";
        Metric metric = TestObjectProvider.createMetric(1L, metricName);
        JsonObject entity = JsonObject.mapFrom(metric);

        when(metricService.existsOneByMetric(metricName)).thenReturn(Single.just(true));

        metricChecker.checkForDuplicateEntity(entity)
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(AlreadyExistsException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void checkUpdateNoDuplicateWithMetricNotExists(VertxTestContext testContext) {
        String metricName = "region";
        Metric metric = TestObjectProvider.createMetric(1L, metricName);
        JsonObject entity = JsonObject.mapFrom(metric);

        when(metricService.existsOneByMetric(metricName)).thenReturn(Single.just(false));

        metricChecker.checkUpdateNoDuplicate(entity, entity)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.getLong("metric_id")).isEqualTo(1L);
                    assertThat(result.getString("metric")).isEqualTo(metricName);
                    verify(metricService).existsOneByMetric(metricName);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method did throw exception " + throwable.getMessage()))
            );
    }

    @Test
    void checkUpdateNoDuplicateWithMetricExists(VertxTestContext testContext) {
        String metricName = "region";
        Metric metric = TestObjectProvider.createMetric(1L, metricName);
        JsonObject entity = JsonObject.mapFrom(metric);

        when(metricService.existsOneByMetric(metricName)).thenReturn(Single.just(true));

        metricChecker.checkUpdateNoDuplicate(entity, entity)
            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(AlreadyExistsException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void checkUpdateNoDuplicateWithoutMetric(VertxTestContext testContext) {
        String metricName = "region";
        Metric metric = TestObjectProvider.createMetric(1L, metricName);
        JsonObject body = JsonObject.mapFrom(metric);
        body.remove("metric");
        JsonObject entity = JsonObject.mapFrom(metric);

        metricChecker.checkUpdateNoDuplicate(body, entity)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.getLong("metric_id")).isEqualTo(1L);
                    assertThat(result.getString("metric")).isEqualTo(metricName);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method did throw exception " + throwable.getMessage()))
            );
    }

    @Test
    void checkFinOneByMetricExists(VertxTestContext testContext) {
        String metricName = "region";
        Metric metric = TestObjectProvider.createMetric(1L, metricName);
        JsonObject entity = JsonObject.mapFrom(metric);

        when(metricService.findOneByMetric(metricName)).thenReturn(Single.just(entity));

        metricChecker.checkFindOneByMetric(metricName)
            .subscribe(result -> {
                assertThat(result.getLong("metric_id")).isEqualTo(1L);
                assertThat(result.getString("metric")).isEqualTo(metricName);
                verify(metricService).findOneByMetric(metricName);
                testContext.completeNow();
            },
                throwable -> testContext.verify(() -> fail("method did throw exception"))
            );
    }

    @Test
    void checkFinOneByMetricNotExists(VertxTestContext testContext) {
        String metricName = "region";
        Single<JsonObject> handler = new SingleHelper<JsonObject>().getEmptySingle();

        when(metricService.findOneByMetric(metricName)).thenReturn(handler);

        metricChecker.checkFindOneByMetric(metricName)
            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void checkEqualValueTypesNumberTrue(VertxTestContext testContext) {
        String metricName = "region";
        ServiceLevelObjective slo = TestObjectProvider.createServiceLevelObjective(metricName, ExpressionType.GT,
            1.0, 2.0, 3.0);
        MetricType metricType = TestObjectProvider.createMetricType(1L, "number");
        Metric metric = TestObjectProvider.createMetric(1L, metricName, metricType, false);
        JsonObject entity = JsonObject.mapFrom(metric);

        metricChecker.checkEqualValueTypes(slo, entity)
            .subscribe(result -> {
                    assertThat(result).isEqualTo(true);
                    testContext.completeNow();
            },
            throwable -> testContext.verify(() -> fail("method did throw exception"))
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"string", "boolean"})
    void checkEqualValueTypesNumberFalse(String metricTypeName, VertxTestContext testContext) {
        String metricName = "region";
        ServiceLevelObjective slo = TestObjectProvider.createServiceLevelObjective(metricName, ExpressionType.GT,
            1.0, 2.0, 3.0);
        MetricType metricType = TestObjectProvider.createMetricType(1L, metricTypeName);
        Metric metric = TestObjectProvider.createMetric(1L, metricName, metricType, false);
        JsonObject entity = JsonObject.mapFrom(metric);

        metricChecker.checkEqualValueTypes(slo, entity)
            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(BadInputException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void checkEqualValueTypesStringTrue(VertxTestContext testContext) {
        String metricName = "region";
        ServiceLevelObjective slo = TestObjectProvider.createServiceLevelObjective(metricName, ExpressionType.GT,
            "eu-west", "eu-east");
        MetricType metricType = TestObjectProvider.createMetricType(1L, "string");
        Metric metric = TestObjectProvider.createMetric(1L, metricName, metricType, false);
        JsonObject entity = JsonObject.mapFrom(metric);

        metricChecker.checkEqualValueTypes(slo, entity)
            .subscribe(result -> {
                    assertThat(result).isEqualTo(true);
                    testContext.completeNow();
                },
                throwable -> testContext.verify(() -> fail("method did throw exception"))
            );
    }

    @ParameterizedTest
    @ValueSource(strings = {"number", "boolean"})
    void checkEqualValueTypesStringFalse(String metricTypeName, VertxTestContext testContext) {
        String metricName = "region";
        ServiceLevelObjective slo = TestObjectProvider.createServiceLevelObjective(metricName, ExpressionType.GT,
            "eu-west", "eu-east");
        MetricType metricType = TestObjectProvider.createMetricType(1L, metricTypeName);
        Metric metric = TestObjectProvider.createMetric(1L, metricName, metricType, false);
        JsonObject entity = JsonObject.mapFrom(metric);

        metricChecker.checkEqualValueTypes(slo, entity)
            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(BadInputException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void checkEqualValueTypesBooleanTrue(VertxTestContext testContext) {
        String metricName = "region";
        ServiceLevelObjective slo = TestObjectProvider.createServiceLevelObjective(metricName, ExpressionType.GT,
            true, false, false);
        MetricType metricType = TestObjectProvider.createMetricType(1L, "boolean");
        Metric metric = TestObjectProvider.createMetric(1L, metricName, metricType, false);
        JsonObject entity = JsonObject.mapFrom(metric);

        metricChecker.checkEqualValueTypes(slo, entity)
            .subscribe(result -> {
                    assertThat(result).isEqualTo(true);
                    testContext.completeNow();
                },
                throwable -> testContext.verify(() -> fail("method did throw exception"))
            );
    }

    @ParameterizedTest
    @ValueSource(strings = {"number", "string"})
    void checkEqualValueTypesBooleanFalse(String metricTypeName, VertxTestContext testContext) {
        String metricName = "region";
        ServiceLevelObjective slo = TestObjectProvider.createServiceLevelObjective(metricName, ExpressionType.GT,
            true, false, false);
        MetricType metricType = TestObjectProvider.createMetricType(1L, metricTypeName);
        Metric metric = TestObjectProvider.createMetric(1L, metricName, metricType, false);
        JsonObject entity = JsonObject.mapFrom(metric);

        metricChecker.checkEqualValueTypes(slo, entity)
            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(BadInputException.class);
                    testContext.completeNow();
                })
            );
    }

    @ParameterizedTest
    @CsvSource({
        "true, 0.0",
        "false, 4.0"
    })
    void checkAddMetricValueSetCorrectlyNumber(boolean isMonitored, double result, VertxTestContext testContext) {
        long metricId = 1;
        MetricType metricType = TestObjectProvider.createMetricType(1L, "number");
        Metric metric = TestObjectProvider.createMetric(metricId, "availability", metricType, isMonitored);
        JsonObject requestBody = new JsonObject("{\"metricId\": 1, \"value\": 4}");
        MetricValue metricValue = new MetricValue();

        when(metricService.findOne(metricId)).thenReturn(Single.just(JsonObject.mapFrom(metric)));

        metricChecker.checkAddMetricValueSetCorrectly(requestBody, metricId, metricValue)
            .blockingSubscribe(() -> {},
                throwable -> testContext.verify(() -> fail("method did throw exception"))
            );

        assertThat(metricValue.getValueNumber().doubleValue()).isEqualTo(result);
        testContext.completeNow();
    }

    @ParameterizedTest
    @CsvSource({
        "true, ''",
        "false, four"
    })
    void checkAddMetricValueSetCorrectlyString(boolean isMonitored, String result, VertxTestContext testContext) {
        long metricId = 1;
        MetricType metricType = TestObjectProvider.createMetricType(1L, "string");
        Metric metric = TestObjectProvider.createMetric(metricId, "availability", metricType, isMonitored);
        JsonObject requestBody = new JsonObject("{\"metricId\": 1, \"value\": \"four\"}");
        MetricValue metricValue = new MetricValue();

        when(metricService.findOne(metricId)).thenReturn(Single.just(JsonObject.mapFrom(metric)));

        metricChecker.checkAddMetricValueSetCorrectly(requestBody, metricId, metricValue)
            .blockingSubscribe(() -> {},
                throwable -> testContext.verify(() -> fail("method did throw exception"))
            );

        assertThat(metricValue.getValueString()).isEqualTo(result);
        testContext.completeNow();
    }

    @ParameterizedTest
    @CsvSource({
        "true, false",
        "false, true"
    })
    void checkAddMetricValueSetCorrectlyBoolean(boolean isMonitored, boolean result, VertxTestContext testContext) {
        long metricId = 1;
        MetricType metricType = TestObjectProvider.createMetricType(1L, "boolean");
        Metric metric = TestObjectProvider.createMetric(metricId, "availability", metricType, isMonitored);
        JsonObject requestBody = new JsonObject("{\"metricId\": 1, \"value\": true}");
        MetricValue metricValue = new MetricValue();

        when(metricService.findOne(metricId)).thenReturn(Single.just(JsonObject.mapFrom(metric)));

        metricChecker.checkAddMetricValueSetCorrectly(requestBody, metricId, metricValue)
            .blockingSubscribe(() -> {},
                throwable -> testContext.verify(() -> fail("method did throw exception"))
            );

        assertThat(metricValue.getValueBool()).isEqualTo(result);
        testContext.completeNow();
    }

    @ParameterizedTest
    @CsvSource({
        "number, \"four\"",
        "number, false",
        "string, 4",
        "string, false",
        "boolean, 4",
        "boolean, \"four\""
    })
    void checkAddMetricValueSetCorrectlyFalse(String metricTypeValue, String value, VertxTestContext testContext) {
        long metricId = 1;
        MetricType metricType = TestObjectProvider.createMetricType(1L, metricTypeValue);
        Metric metric = TestObjectProvider.createMetric(metricId, "availability", metricType, false);
        JsonObject requestBody = new JsonObject("{\"metricId\": 1, \"value\": " + value + "}");
        MetricValue metricValue = new MetricValue();

        when(metricService.findOne(metricId)).thenReturn(Single.just(JsonObject.mapFrom(metric)));

        metricChecker.checkAddMetricValueSetCorrectly(requestBody, metricId, metricValue)
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(BadInputException.class);
                    testContext.completeNow();
                })
            );
    }

    @ParameterizedTest
    @CsvSource({
        "number, 4",
        "string, \"four\"",
        "boolean, false"
    })
    void checkUpdateMetricValueSetCorrectlyTrue(String metricTypeName, String value, VertxTestContext testContext) {
        long metricId = 1;
        MetricType metricType = TestObjectProvider.createMetricType(1L, metricTypeName);
        Metric metric = TestObjectProvider.createMetric(metricId, "availability", metricType, false);
        JsonObject requestBody = new JsonObject("{\"value\": " + value + "}");

        when(metricService.findOne(metricId)).thenReturn(Single.just(JsonObject.mapFrom(metric)));

        metricChecker.checkUpdateMetricValueSetCorrectly(requestBody, metricId)
            .blockingSubscribe(() -> {},
                throwable -> testContext.verify(() -> fail("method did throw exception"))
            );

        verify(metricService).findOne(metricId);
        testContext.completeNow();
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
    void checkUpdateMetricValueSetCorrectlyFalse(String metricTypeName, String value, Boolean isMonitored,
                                                 VertxTestContext testContext) {
        long metricId = 1;
        MetricType metricType = TestObjectProvider.createMetricType(1L, metricTypeName);
        Metric metric = TestObjectProvider.createMetric(metricId, "availability", metricType, isMonitored);
        JsonObject requestBody = new JsonObject("{\"value\": " + value + "}");

        when(metricService.findOne(metricId)).thenReturn(Single.just(JsonObject.mapFrom(metric)));

        metricChecker.checkUpdateMetricValueSetCorrectly(requestBody, metricId)
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(BadInputException.class);
                    testContext.completeNow();
                })
            );
    }
}
