package at.uibk.dps.rm.service.database.metric;

import at.uibk.dps.rm.entity.model.Metric;
import at.uibk.dps.rm.repository.MetricRepository;
import at.uibk.dps.rm.util.JsonMapperConfig;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.hibernate.reactive.util.impl.CompletionStages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CompletionStage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class MetricServiceImplTest {

    private MetricService metricService;

    @Mock
    MetricRepository metricRepository;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        metricService = new MetricServiceImpl(metricRepository);
    }

    @Test
    void findOneMyMetricExists(VertxTestContext testContext) {
        String metric = "testmetric";
        Metric entity = new Metric();
        entity.setMetricId(1L);
        entity.setMetric(metric);
        CompletionStage<Metric> completionStage = CompletionStages.completedFuture(entity);
        doReturn(completionStage).when(metricRepository).findByMetric(metric);

        metricService.findOneByMetric(metric)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.getLong("metric_id")).isEqualTo(1L);
                assertThat(result.getString("metric")).isEqualTo("testmetric");
                verify(metricRepository).findByMetric(metric);
                testContext.completeNow();
        })));
    }

    @Test
    void findOneMyMetricNotExists(VertxTestContext testContext) {
        String metric = "testmetric";
        CompletionStage<Metric> completionStage = CompletionStages.completedFuture(null);
        doReturn(completionStage).when(metricRepository).findByMetric(metric);

        metricService.findOneByMetric(metric)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isNull();
                verify(metricRepository).findByMetric(metric);
                testContext.completeNow();
        })));
    }

    @Test
    void checkMetricExistsByMetricTrue(VertxTestContext testContext) {
        String metric = "testmetric";
        Metric entity = new Metric();
        CompletionStage<Metric> completionStage = CompletionStages.completedFuture(entity);
        doReturn(completionStage).when(metricRepository).findByMetric(metric);

        metricService.existsOneByMetric(metric)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isEqualTo(true);
                verify(metricRepository).findByMetric(metric);
                testContext.completeNow();
        })));
    }

    @Test
    void checkMetricExistsByMetricFalse(VertxTestContext testContext) {
        String metric = "testmetric";
        CompletionStage<Metric> completionStage = CompletionStages.completedFuture(null);
        doReturn(completionStage).when(metricRepository).findByMetric(metric);

        metricService.existsOneByMetric(metric)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isEqualTo(false);
                verify(metricRepository).findByMetric(metric);
                testContext.completeNow();
        })));
    }
}
