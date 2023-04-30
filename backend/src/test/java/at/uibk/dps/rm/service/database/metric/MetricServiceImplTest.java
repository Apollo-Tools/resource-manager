package at.uibk.dps.rm.service.database.metric;

import at.uibk.dps.rm.entity.model.Metric;
import at.uibk.dps.rm.repository.metric.MetricRepository;
import at.uibk.dps.rm.testutil.objectprovider.TestMetricProvider;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.hibernate.reactive.util.impl.CompletionStages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

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
    void findAllByResourceTypeId(VertxTestContext testContext) {
        long resourceTypeId = 1L;
        boolean required = true;
        Metric m1 = TestMetricProvider.createMetric(1L, "m1");
        Metric m2 = TestMetricProvider.createMetric(2L, "m2");
        Metric m3 = TestMetricProvider.createMetric(3L, "m3");
        CompletionStage<List<Metric>> completionStage = CompletionStages.completedFuture(List.of(m1, m2, m3));

        when(metricRepository.findAllByResourceTypeId(resourceTypeId, required)).thenReturn(completionStage);

        metricService.findAllByResourceTypeId(resourceTypeId, required)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(3);
                assertThat(result.getJsonObject(0).getLong("metric_id")).isEqualTo(1L);
                assertThat(result.getJsonObject(1).getLong("metric_id")).isEqualTo(2L);
                assertThat(result.getJsonObject(2).getLong("metric_id")).isEqualTo(3L);
                testContext.completeNow();
            })));
    }

    @Test
    void findAllByResourceTypeIdEmpty(VertxTestContext testContext) {
        long resourceTypeId = 1L;
        boolean required = true;
        CompletionStage<List<Metric>> completionStage = CompletionStages.completedFuture(new ArrayList<>());

        when(metricRepository.findAllByResourceTypeId(resourceTypeId, required)).thenReturn(completionStage);

        metricService.findAllByResourceTypeId(resourceTypeId, required)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(0);
                testContext.completeNow();
            })));
    }


    @Test
    void findOneByMetricExists(VertxTestContext testContext) {
        String metric = "testmetric";
        Metric entity = new Metric();
        entity.setMetricId(1L);
        entity.setMetric(metric);
        CompletionStage<Metric> completionStage = CompletionStages.completedFuture(entity);
        when(metricRepository.findByMetric(metric)).thenReturn(completionStage);

        metricService.findOneByMetric(metric)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.getLong("metric_id")).isEqualTo(1L);
                assertThat(result.getString("metric")).isEqualTo("testmetric");
                verify(metricRepository).findByMetric(metric);
                testContext.completeNow();
        })));
    }

    @Test
    void findOneByMetricNotExists(VertxTestContext testContext) {
        String metric = "testmetric";
        CompletionStage<Metric> completionStage = CompletionStages.completedFuture(null);
        when(metricRepository.findByMetric(metric)).thenReturn(completionStage);

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
        when(metricRepository.findByMetric(metric)).thenReturn(completionStage);

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
        when(metricRepository.findByMetric(metric)).thenReturn(completionStage);

        metricService.existsOneByMetric(metric)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isEqualTo(false);
                verify(metricRepository).findByMetric(metric);
                testContext.completeNow();
        })));
    }
}
