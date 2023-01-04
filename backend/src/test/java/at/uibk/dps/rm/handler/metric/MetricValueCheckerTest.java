package at.uibk.dps.rm.handler.metric;

import at.uibk.dps.rm.entity.model.MetricValue;
import at.uibk.dps.rm.exception.AlreadyExistsException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.service.rxjava3.database.metric.MetricValueService;
import at.uibk.dps.rm.testutil.SingleHelper;
import at.uibk.dps.rm.testutil.TestObjectProvider;
import at.uibk.dps.rm.util.JsonMapperConfig;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class MetricValueCheckerTest {

    MetricValueChecker metricValueChecker;

    @Mock
    MetricValueService metricValueService;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        metricValueChecker = new MetricValueChecker(metricValueService);
    }

    @Test
    void checkFindAllByResource(VertxTestContext testContext) {
        long resourceId = 1L;
        MetricValue mv1 = TestObjectProvider.createMetricValue(1L, 1L, "latency", 25.0);
        MetricValue mv2 = TestObjectProvider.createMetricValue(2L, 2L, "availability", 0.995);
        MetricValue mv3 = TestObjectProvider.createMetricValue(3L, 3L, "bandwidth", 1000);
        JsonArray metricValues = new JsonArray(List.of(JsonObject.mapFrom(mv1),
            JsonObject.mapFrom(mv2), JsonObject.mapFrom(mv3)));

        when(metricValueService.findAllByResource(resourceId, true)).thenReturn(Single.just(metricValues));

        metricValueChecker.checkFindAllByResource(resourceId, true)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.size()).isEqualTo(3);
                    assertThat(result.getJsonObject(0).getLong("metric_value_id")).isEqualTo(1L);
                    assertThat(result.getJsonObject(1).getLong("metric_value_id")).isEqualTo(2L);
                    assertThat(result.getJsonObject(2).getLong("metric_value_id")).isEqualTo(3L);
                    verify(metricValueService).findAllByResource(resourceId, true);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method did throw exception " + throwable.getMessage()))
            );
    }

    @Test
    void checkFindAllByResourceEmpty(VertxTestContext testContext) {
        long resourceId = 1L;
        JsonArray metricValues = new JsonArray();

        when(metricValueService.findAllByResource(resourceId, true)).thenReturn(Single.just(metricValues));

        metricValueChecker.checkFindAllByResource(resourceId, true)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.size()).isEqualTo(0);
                    verify(metricValueService).findAllByResource(resourceId, true);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method did throw exception " + throwable.getMessage()))
            );
    }

    @Test
    void checkFindAllByResourceNotFound(VertxTestContext testContext) {
        long resourceId = 1L;
        Single<JsonArray> handler = new SingleHelper<JsonArray>().getEmptySingle();

        when(metricValueService.findAllByResource(resourceId, true)).thenReturn(handler);

        metricValueChecker.checkFindAllByResource(resourceId, true)
            .subscribe(result -> testContext.verify(() -> fail("method did throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void checkForDuplicateEntityByResourceAndMetricNotExists(VertxTestContext testContext) {
        long resourceId = 1L;
        long metricId = 1L;

        when(metricValueService.existsOneByResourceAndMetric(resourceId, metricId)).thenReturn(Single.just(false));

        metricValueChecker.checkForDuplicateByResourceAndMetric(resourceId, metricId)
            .blockingSubscribe(() -> {
                },
                throwable -> testContext.verify(() -> fail("method did throw exception"))
            );

        verify(metricValueService).existsOneByResourceAndMetric(resourceId, metricId);
        testContext.completeNow();
    }

    @Test
    void checkForDuplicateEntityExists(VertxTestContext testContext) {
        long resourceId = 1L;
        long metricId = 1L;

        when(metricValueService.existsOneByResourceAndMetric(resourceId, metricId)).thenReturn(Single.just(true));

        metricValueChecker.checkForDuplicateByResourceAndMetric(resourceId, metricId)
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(AlreadyExistsException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void checkMetricValueExistsByResourceAndMetricExists(VertxTestContext testContext) {
        long resourceId = 1L;
        long metricId = 1L;

        when(metricValueService.existsOneByResourceAndMetric(resourceId, metricId)).thenReturn(Single.just(true));

        metricValueChecker.checkMetricValueExistsByResourceAndMetric(resourceId, metricId)
            .blockingSubscribe(() -> {
                },
                throwable -> testContext.verify(() -> fail("method did throw exception"))
            );

        verify(metricValueService).existsOneByResourceAndMetric(resourceId, metricId);
        testContext.completeNow();
    }

    @Test
    void checkMetricValueExistsByResourceAndMetricNotExists(VertxTestContext testContext) {
        long resourceId = 1L;
        long metricId = 1L;

        when(metricValueService.existsOneByResourceAndMetric(resourceId, metricId)).thenReturn(Single.just(false));

        metricValueChecker.checkMetricValueExistsByResourceAndMetric(resourceId, metricId)
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }
}