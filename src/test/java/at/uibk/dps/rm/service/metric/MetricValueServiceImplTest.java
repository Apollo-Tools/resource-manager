package at.uibk.dps.rm.service.metric;

import at.uibk.dps.rm.entity.model.Metric;
import at.uibk.dps.rm.entity.model.MetricValue;
import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.entity.model.ResourceType;
import at.uibk.dps.rm.repository.MetricValueRepository;
import at.uibk.dps.rm.service.database.metric.MetricValueServiceImpl;
import at.uibk.dps.rm.util.JsonMapperConfig;
import io.vertx.core.json.JsonArray;
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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class MetricValueServiceImplTest {

    private MetricValueServiceImpl metricValueService;

    @Mock
    MetricValueRepository metricValueRepository;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        metricValueService = new MetricValueServiceImpl(metricValueRepository);
    }

    @Test
    void testSaveAll(VertxTestContext testContext) {
        CompletionStage<Void> completionStage = CompletionStages.voidFuture();
        doReturn(completionStage).when(metricValueRepository).createAll(anyList());

        JsonArray data = new JsonArray("[{\"count\": 10, \"value_bool\": false}, " +
                "{\"count\": 3, \"value_string\": \"ubuntu \"}]");

        metricValueService.saveAll(data)
                .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                    assertThat(result).isNull();
                    verify(metricValueRepository, times(1)).createAll(anyList());
                    testContext.completeNow();
                })));
    }

    @Test
    void findOneEntityExists(VertxTestContext testContext) {
        long entityId = 1L;
        MetricValue entity = new MetricValue();
        entity.setMetricValueId(entityId);
        entity.setResource(new Resource());
        CompletionStage<MetricValue> completionStage = CompletionStages.completedFuture(entity);
        doReturn(completionStage).when(metricValueRepository).findByIdAndFetch(entityId);

        metricValueService.findOne(entityId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.getLong("metric_value_id")).isEqualTo(1L);
                assertThat(result.getJsonObject("resource")).isNull();
                verify(metricValueRepository, times(1)).findByIdAndFetch(entityId);
                testContext.completeNow();
        })));
    }

    @Test
    void findOneEntityNotExists(VertxTestContext testContext) {
        long entityId = 1L;
        CompletionStage<MetricValue> completionStage = CompletionStages.completedFuture(null);
        doReturn(completionStage).when(metricValueRepository).findByIdAndFetch(entityId);

        metricValueService.findOne(entityId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isNull();
                verify(metricValueRepository, times(1)).findByIdAndFetch(entityId);
                testContext.completeNow();
        })));
    }

    @Test
    void findAllByResourceWithValue(VertxTestContext testContext) {
        long resourceId = 1L;
        boolean includeValue = true;
        Resource resource = new Resource();
        Metric metric1 = new Metric();
        metric1.setMetricId(1L);
        Metric metric2 = new Metric();
        metric2.setMetricId(2L);
        MetricValue entity1 = new MetricValue();
        entity1.setMetricValueId(1L);
        entity1.setMetric(metric1);
        entity1.setValueNumber(10.0);
        entity1.setResource(resource);
        MetricValue entity2 = new MetricValue();
        entity2.setMetricValueId(2L);
        entity2.setMetric(metric2);
        entity2.setValueString("ubuntu");
        entity2.setResource(resource);
        List<MetricValue> metricValueList = new ArrayList<>();
        metricValueList.add(entity1);
        metricValueList.add(entity2);
        CompletionStage<List<MetricValue>> completionStage = CompletionStages.completedFuture(metricValueList);
        doReturn(completionStage).when(metricValueRepository).findByResourceAndFetch(resourceId);

        metricValueService.findAllByResource(resourceId, includeValue)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(2);
                assertThat(result.getJsonObject(0).getLong("metric_value_id")).isEqualTo(1L);
                assertThat(result.getJsonObject(0).getDouble("value_number")).isEqualTo(10.0);
                assertThat(result.getJsonObject(0).getJsonObject("resource")).isNull();
                assertThat(result.getJsonObject(1).getLong("metric_value_id")).isEqualTo(2L);
                assertThat(result.getJsonObject(1).getString("value_string")).isEqualTo("ubuntu");
                assertThat(result.getJsonObject(1).getJsonObject("resource")).isNull();
                verify(metricValueRepository, times(1)).findByResourceAndFetch(resourceId);
                testContext.completeNow();
        })));
    }

    @Test
    void findAllByResourceWithoutValue(VertxTestContext testContext) {
        long resourceId = 1L;
        boolean includeValue = false;
        Metric metric1 = new Metric();
        metric1.setMetricId(1L);
        Metric metric2 = new Metric();
        metric2.setMetricId(2L);
        MetricValue entity1 = new MetricValue();
        entity1.setMetric(metric1);
        MetricValue entity2 = new MetricValue();
        entity2.setMetric(metric2);
        List<MetricValue> metricValueList = new ArrayList<>();
        metricValueList.add(entity1);
        metricValueList.add(entity2);
        CompletionStage<List<MetricValue>> completionStage = CompletionStages.completedFuture(metricValueList);
        doReturn(completionStage).when(metricValueRepository).findByResourceAndFetch(resourceId);

        metricValueService.findAllByResource(resourceId, includeValue)
                .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                    assertThat(result.size()).isEqualTo(2);
                    assertThat(result.getJsonObject(0).getLong("metric_id")).isEqualTo(1L);
                    assertThat(result.getJsonObject(1).getLong("metric_id")).isEqualTo(2L);
                    verify(metricValueRepository, times(1)).findByResourceAndFetch(resourceId);
                    testContext.completeNow();
                })));
    }

    @Test
    void findAllByResourceEmptyResult(VertxTestContext testContext) {
        long resourceId = 1L;
        boolean includeValue = false;
        List<MetricValue> metricValueList = new ArrayList<>();
        CompletionStage<List<MetricValue>> completionStage = CompletionStages.completedFuture(metricValueList);
        doReturn(completionStage).when(metricValueRepository).findByResourceAndFetch(resourceId);

        metricValueService.findAllByResource(resourceId, includeValue)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(0);
                verify(metricValueRepository, times(1)).findByResourceAndFetch(resourceId);
                testContext.completeNow();
        })));
    }

    @Test
    void findOneByResourceAndMetricExists(VertxTestContext testContext) {
        long resourceId = 1L;
        long metricId = 2L;
        MetricValue entity = new MetricValue();
        entity.setMetricValueId(3L);
        entity.setResource(new Resource());
        CompletionStage<MetricValue> completionStage = CompletionStages.completedFuture(entity);
        doReturn(completionStage).when(metricValueRepository).findByResourceAndMetric(resourceId, metricId);

        metricValueService.findOneByResourceAndMetric(resourceId, metricId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.getLong("metric_value_id")).isEqualTo(3L);
                assertThat(result.getJsonObject("resource")).isNull();
                verify(metricValueRepository, times(1)).findByResourceAndMetric(resourceId, metricId);
                testContext.completeNow();
        })));
    }

    @Test
    void findOneByResourceAndMetricNotExists(VertxTestContext testContext) {
        long resourceId = 1L;
        long metricId = 2L;
        CompletionStage<MetricValue> completionStage = CompletionStages.completedFuture(null);
        doReturn(completionStage).when(metricValueRepository).findByResourceAndMetric(resourceId, metricId);

        metricValueService.findOneByResourceAndMetric(resourceId, metricId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isNull();
                verify(metricValueRepository, times(1)).findByResourceAndMetric(resourceId, metricId);
                testContext.completeNow();
        })));
    }

    @Test
    void checkOneByResourceAndMetricExists(VertxTestContext testContext) {
        long resourceId = 1L;
        long metricId = 2L;
        MetricValue entity = new MetricValue();
        CompletionStage<MetricValue> completionStage = CompletionStages.completedFuture(entity);
        doReturn(completionStage).when(metricValueRepository).findByResourceAndMetric(resourceId, metricId);

        metricValueService.existsOneByResourceAndMetric(resourceId, metricId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isEqualTo(true);
                verify(metricValueRepository, times(1)).findByResourceAndMetric(resourceId, metricId);
                testContext.completeNow();
        })));
    }

    @Test
    void checkOneByResourceAndMetricNotExists(VertxTestContext testContext) {
        long resourceId = 1L;
        long metricId = 2L;
        CompletionStage<MetricValue> completionStage = CompletionStages.completedFuture(null);
        doReturn(completionStage).when(metricValueRepository).findByResourceAndMetric(resourceId, metricId);

        metricValueService.existsOneByResourceAndMetric(resourceId, metricId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isEqualTo(false);
                verify(metricValueRepository, times(1)).findByResourceAndMetric(resourceId, metricId);
                testContext.completeNow();
        })));
    }

    @Test
    void updateByResourceAndMetric(VertxTestContext testContext) {
        long resourceId = 1L;
        long metricId = 2L;
        double valueNumber = 13.37;
        CompletionStage<MetricValue> completionStage = CompletionStages.completedFuture(null);
        doReturn(completionStage)
                .when(metricValueRepository)
                .updateByResourceAndMetric(resourceId, metricId, null, valueNumber, null);

        metricValueService.updateByResourceAndMetric(resourceId, metricId, null, valueNumber, null)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isNull();
                verify(metricValueRepository, times(1))
                        .updateByResourceAndMetric(resourceId, metricId, null, valueNumber, null);
                testContext.completeNow();
        })));
    }

    @Test
    void deleteByResourceAndMetric(VertxTestContext testContext) {
        long resourceId = 1L;
        long metricId = 2L;
        CompletionStage<ResourceType> completionStage = CompletionStages.completedFuture(null);
        doReturn(completionStage).when(metricValueRepository).deleteByResourceAndMetric(resourceId, metricId);

        metricValueService.deleteByResourceAndMetric(resourceId, metricId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isNull();
                verify(metricValueRepository, times(1)).deleteByResourceAndMetric(resourceId, metricId);
                testContext.completeNow();
        })));
    }
}
