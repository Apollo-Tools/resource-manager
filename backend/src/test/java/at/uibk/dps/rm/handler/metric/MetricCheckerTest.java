package at.uibk.dps.rm.handler.metric;

import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.service.rxjava3.database.metric.MetricService;
import at.uibk.dps.rm.testutil.objectprovider.TestMetricProvider;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
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
import static org.mockito.Mockito.when;

/**
 * Implements tests for the {@link MetricChecker} class.
 *
 * @author matthi-g
 */
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
    void checkFindAllByPlatformId(VertxTestContext testContext) {
        long platformId = 1L;
        boolean required = false;
        JsonObject m1 = JsonObject.mapFrom(TestMetricProvider.createMetric(1L, "cpu"));
        JsonObject m2 = JsonObject.mapFrom(TestMetricProvider.createMetric(2L, "memory"));
        JsonArray metrics = new JsonArray(List.of(m1, m2));

        when(metricService.findAllByPlatformId(platformId, required)).thenReturn(Single.just(metrics));

        metricChecker.checkFindAllByPlatform(platformId, required)
            .subscribe(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(2);
                assertThat(result.getJsonObject(0).getLong("metric_id")).isEqualTo(1L);
                assertThat(result.getJsonObject(1).getLong("metric_id")).isEqualTo(2L);
                testContext.completeNow();
            }),
            throwable -> testContext.verify(() -> fail("method has thrown exception"))
        );
    }


    @Test
    void checkFindAllByPlatformIdNotFound(VertxTestContext testContext) {
        long platformId = 1L;
        boolean required = false;

        when(metricService.findAllByPlatformId(platformId, required))
            .thenReturn(Single.error(NotFoundException::new));

        metricChecker.checkFindAllByPlatform(platformId, required)
            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }
}
