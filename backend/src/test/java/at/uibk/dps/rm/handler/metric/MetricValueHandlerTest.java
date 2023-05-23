package at.uibk.dps.rm.handler.metric;

import at.uibk.dps.rm.entity.model.MetricValue;
import at.uibk.dps.rm.exception.AlreadyExistsException;
import at.uibk.dps.rm.exception.BadInputException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.handler.resource.ResourceChecker;
import at.uibk.dps.rm.testutil.RoutingContextMockHelper;
import at.uibk.dps.rm.testutil.objectprovider.TestMetricProvider;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.ext.web.RoutingContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * Implements tests for the {@link MetricValueHandler} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class MetricValueHandlerTest {

    private MetricValueHandler metricValueHandler;

    @Mock
    private MetricValueChecker metricValueChecker;

    @Mock
    private MetricChecker metricChecker;

    @Mock
    private ResourceChecker resourceChecker;

    @Mock
    private RoutingContext rc;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        metricValueHandler = new MetricValueHandler(metricValueChecker, metricChecker, resourceChecker);
    }

    @ParameterizedTest
    @ValueSource(strings = {"valid", "empty", "resourceNotExists"})
    void getAll(String testCase, VertxTestContext testContext) {
        long resourceId = 1L;
        MetricValue mv1 = TestMetricProvider.createMetricValue(1L, 1L, "latency", 25.0);
        MetricValue mv2 = TestMetricProvider.createMetricValue(2L, 2L, "availability", 0.995);
        MetricValue mv3 = TestMetricProvider.createMetricValue(3L, 3L, "bandwidth", 1000);
        JsonArray metricValues = new JsonArray(List.of(JsonObject.mapFrom(mv1),
            JsonObject.mapFrom(mv2), JsonObject.mapFrom(mv3)));
        if (testCase.equals("empty")) {
            metricValues = new JsonArray();
        }

        when(rc.pathParam("id")).thenReturn(String.valueOf(resourceId));
        when(resourceChecker.checkExistsOne(resourceId)).thenReturn(testCase.equals("resourceNotExists") ?
                Completable.error(NotFoundException::new) : Completable.complete());
        if (!testCase.equals("resourceNotExists")) {
            when(metricValueChecker.checkFindAllByResource(resourceId, true))
                .thenReturn(Single.just(metricValues));
        }

        metricValueHandler.getAll(rc)
            .subscribe(result -> testContext.verify(() -> {
                if (testCase.equals("valid")) {
                    assertThat(result.size()).isEqualTo(3);
                    assertThat(result.getJsonObject(0).getLong("metric_value_id")).isEqualTo(1L);
                    assertThat(result.getJsonObject(1).getLong("metric_value_id")).isEqualTo(2L);
                    assertThat(result.getJsonObject(2).getLong("metric_value_id")).isEqualTo(3L);
                } else if (testCase.equals("empty")) {
                    assertThat(result.size()).isEqualTo(0);
                } else {
                    fail("method did not throw exception");
                }
                testContext.completeNow();
            }), throwable -> testContext.verify(() -> {
                if (testCase.equals("resourceNotExists")) {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                } else {
                    fail("method has thrown exception");
                }
                testContext.completeNow();
            }));
    }

    @ParameterizedTest
    @ValueSource(strings = {"valid", "badInput", "alreadyExists", "resourceNotExists"})
    void postAll(String testCase, VertxTestContext testContext) {
        long resourceId = 1L;
        JsonObject value1 = new JsonObject("{\"metricId\": 1, \"value\": 4}");
        JsonObject value2 = new JsonObject("{\"metricId\": 2, \"value\": \"four\"}");
        JsonObject value3 = new JsonObject("{\"metricId\": 3, \"value\": false}");
        JsonArray requestBody = new JsonArray(List.of(value1, value2, value3));

        RoutingContextMockHelper.mockBody(rc, requestBody);
        when(rc.pathParam("id")).thenReturn(String.valueOf(resourceId));
        when(resourceChecker.checkExistsOne(resourceId)).thenReturn(testCase.equals("resourceNotExists") ?
                Completable.error(NotFoundException::new) : Completable.complete());
        if (!testCase.equals("resourceNotExists")) {
            when(metricChecker.checkAddMetricValueSetCorrectly(eq(value1), eq(1L), any())).thenReturn(Completable.complete());
            when(metricChecker.checkAddMetricValueSetCorrectly(eq(value2), eq(2L), any()))
                .thenReturn(testCase.equals("badInput") ? Completable.error(BadInputException::new) :
                    Completable.complete());
            when(metricChecker.checkAddMetricValueSetCorrectly(eq(value3), eq(3L), any())).thenReturn(Completable.complete());
            when(metricValueChecker.checkForDuplicateByResourceAndMetric(eq(resourceId), anyLong()))
                .thenReturn(testCase.equals("alreadyExists") ? Completable.error(AlreadyExistsException::new) :
                    Completable.complete());
        }
        if (testCase.equals("valid")) {
            when(metricValueChecker.submitCreateAll(any(JsonArray.class))).thenReturn(Completable.complete());
        }

        metricValueHandler.postAll(rc)
            .blockingSubscribe(() -> testContext.verify(() -> {
                    if (!testCase.equals("valid")) {
                        fail("method did not throw exception");
                    }
                }), throwable -> testContext.verify(() -> {
                    switch (testCase) {
                        case "valid":
                            fail("method has thrown exception");
                            break;
                        case "badInput":
                            assertThat(throwable).isInstanceOf(BadInputException.class);
                            break;
                        case "alreadyExists":
                            assertThat(throwable).isInstanceOf(AlreadyExistsException.class);
                            break;
                        case "resourceNotExists":
                            assertThat(throwable).isInstanceOf(NotFoundException.class);
                            break;
                }
                })
            );
        testContext.completeNow();
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
        JsonObject requestBody = new JsonObject("{\"value\": " + value + "}");

        RoutingContextMockHelper.mockBody(rc, requestBody);
        when(rc.pathParam("resourceId")).thenReturn(String.valueOf(resourceId));
        when(rc.pathParam("metricId")).thenReturn(String.valueOf(metricId));
        when(resourceChecker.checkExistsOne(resourceId)).thenReturn(Completable.complete());
        when(metricChecker.checkExistsOne(metricId)).thenReturn(Completable.complete());
        when(metricValueChecker.checkMetricValueExistsByResourceAndMetric(resourceId, metricId))
            .thenReturn(Completable.complete());
        when(metricChecker.checkUpdateMetricValueSetCorrectly(requestBody, metricId)).thenReturn(Completable.complete());
        switch (metricTypeName) {
            case "number":
                when(metricValueChecker.submitUpdateMetricValue(resourceId, metricId, Double.valueOf(value)))
                    .thenReturn(Completable.complete());
                break;
            case "string":
                when(metricValueChecker.submitUpdateMetricValue(resourceId, metricId, value.replace("\"", "")))
                    .thenReturn(Completable.complete());
                break;
            case "boolean":
                when(metricValueChecker.submitUpdateMetricValue(resourceId, metricId, Boolean.valueOf(value)))
                    .thenReturn(Completable.complete());
        }

        metricValueHandler.updateOne(rc)
            .blockingSubscribe(() -> {},
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
        testContext.completeNow();
    }

    @ParameterizedTest
    @ValueSource(strings = {"resourceNotFound", "metricNotFound", "mvNotFound", "badInput", ""})
    void updateOneInvalid(String testCase, VertxTestContext testContext) {
        long resourceId = 1;
        long metricId = 1;
        JsonObject requestBody = new JsonObject("{\"value\": 4}");

        RoutingContextMockHelper.mockBody(rc, requestBody);
        when(rc.pathParam("resourceId")).thenReturn(String.valueOf(resourceId));
        when(rc.pathParam("metricId")).thenReturn(String.valueOf(metricId));
        when(resourceChecker.checkExistsOne(resourceId)).thenReturn(testCase.startsWith("resource") ?
            Completable.error(NotFoundException::new) : Completable.complete());
        when(metricChecker.checkExistsOne(metricId)).thenReturn(testCase.startsWith("metric") ?
            Completable.error(NotFoundException::new) : Completable.complete());
        when(metricValueChecker.checkMetricValueExistsByResourceAndMetric(resourceId, metricId))
            .thenReturn(testCase.startsWith("mv") ?
                Completable.error(NotFoundException::new) : Completable.complete());
        when(metricChecker.checkUpdateMetricValueSetCorrectly(requestBody, metricId))
            .thenReturn(testCase.startsWith("badInput") ? Completable.error(BadInputException::new) :
                Completable.complete());

        metricValueHandler.updateOne(rc)
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    if (testCase.equals("badInput")) {
                        assertThat(throwable).isInstanceOf(BadInputException.class);
                    } else {
                        assertThat(throwable).isInstanceOf(NotFoundException.class);
                    }
                    testContext.completeNow();
                })
            );
    }

    @ParameterizedTest
    @ValueSource(strings = {"valid", "metricNotFound", "resourceNotFound", "mvNotFound"})
    void deleteOne(String testCase, VertxTestContext testContext) {
        long resourceId = 1L;
        long metricId = 1L;

        when(rc.pathParam("resourceId")).thenReturn(String.valueOf(resourceId));
        when(rc.pathParam("metricId")).thenReturn(String.valueOf(resourceId));
        when(metricChecker.checkExistsOne(metricId)).thenReturn(testCase.startsWith("metric") ?
            Completable.error(NotFoundException::new) : Completable.complete());
        when(resourceChecker.checkExistsOne(resourceId)).thenReturn(testCase.startsWith("resource") ?
            Completable.error(NotFoundException::new) : Completable.complete());
        when(metricValueChecker.checkMetricValueExistsByResourceAndMetric(resourceId, metricId))
            .thenReturn(testCase.startsWith("mv") ?
                Completable.error(NotFoundException::new) : Completable.complete());
        if (testCase.equals("valid")) {
            when(metricValueChecker.submitDeleteMetricValue(resourceId, metricId)).thenReturn(Completable.complete());
        }

        metricValueHandler.deleteOne(rc)
            .blockingSubscribe(() -> testContext.verify(() -> {
                    if (!testCase.equals("valid")) {
                        fail("method did not throw exception");
                    }
                }), throwable -> testContext.verify(() -> {
                    if (testCase.equals("valid")) {
                        fail("method has thrown exception");
                    } else {
                        assertThat(throwable).isInstanceOf(NotFoundException.class);
                    }
                    testContext.completeNow();
                })
            );
        testContext.completeNow();
    }
}
