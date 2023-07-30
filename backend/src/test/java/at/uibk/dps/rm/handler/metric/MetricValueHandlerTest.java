package at.uibk.dps.rm.handler.metric;

import at.uibk.dps.rm.entity.model.MetricValue;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
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
    private RoutingContext rc;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        metricValueHandler = new MetricValueHandler(metricValueChecker);
    }

    @Test
    void getAll(VertxTestContext testContext) {
        long resourceId = 1L;
        MetricValue mv1 = TestMetricProvider.createMetricValue(1L, 1L, "latency", 25.0);
        MetricValue mv2 = TestMetricProvider.createMetricValue(2L, 2L, "availability", 0.995);
        MetricValue mv3 = TestMetricProvider.createMetricValue(3L, 3L, "bandwidth", 1000);
        JsonArray metricValues = new JsonArray(List.of(JsonObject.mapFrom(mv1),
            JsonObject.mapFrom(mv2), JsonObject.mapFrom(mv3)));

        when(rc.pathParam("id")).thenReturn(String.valueOf(resourceId));
        when(metricValueChecker.checkFindAllByResource(resourceId, true))
            .thenReturn(Single.just(metricValues));

        metricValueHandler.getAll(rc)
            .subscribe(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(3);
                assertThat(result.getJsonObject(0).getLong("metric_value_id")).isEqualTo(1L);
                assertThat(result.getJsonObject(1).getLong("metric_value_id")).isEqualTo(2L);
                assertThat(result.getJsonObject(2).getLong("metric_value_id")).isEqualTo(3L);
                testContext.completeNow();
            }), throwable -> testContext.verify(() -> fail("method has thrown exception")));
    }

    @Test
    void postAll(VertxTestContext testContext) {
        long resourceId = 1L;
        JsonObject value1 = new JsonObject("{\"metricId\": 1, \"value\": 4}");
        JsonObject value2 = new JsonObject("{\"metricId\": 2, \"value\": \"four\"}");
        JsonObject value3 = new JsonObject("{\"metricId\": 3, \"value\": false}");
        JsonArray requestBody = new JsonArray(List.of(value1, value2, value3));

        RoutingContextMockHelper.mockBody(rc, requestBody);
        when(rc.pathParam("id")).thenReturn(String.valueOf(resourceId));
        when(metricValueChecker.submitCreateAll(resourceId, requestBody)).thenReturn(Completable.complete());

        metricValueHandler.postAll(rc)
            .blockingSubscribe(() -> testContext.verify(() -> {}),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
        testContext.completeNow();
    }

    @Test
    void updateOne(VertxTestContext testContext) {
        long resourceId = 1L;
        long metricId = 2L;
        JsonObject requestBody = new JsonObject("{\"value\": 8}");

        RoutingContextMockHelper.mockBody(rc, requestBody);
        when(rc.pathParam("resourceId")).thenReturn(String.valueOf(resourceId));
        when(rc.pathParam("metricId")).thenReturn(String.valueOf(metricId));
        when(metricValueChecker.updateOneByValue(1L, 2L, requestBody))
            .thenReturn(Completable.complete());

        metricValueHandler.updateOne(rc)
            .blockingSubscribe(() -> {},
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
        testContext.completeNow();
    }

    @Test
    void deleteOne(VertxTestContext testContext) {
        long resourceId = 1L;
        long metricId = 1L;

        when(rc.pathParam("resourceId")).thenReturn(String.valueOf(resourceId));
        when(rc.pathParam("metricId")).thenReturn(String.valueOf(resourceId));
        when(metricValueChecker.submitDeleteMetricValue(resourceId, metricId)).thenReturn(Completable.complete());

        metricValueHandler.deleteOne(rc)
            .blockingSubscribe(() -> testContext.verify(() -> {}),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
        testContext.completeNow();
    }
}
