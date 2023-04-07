package at.uibk.dps.rm.service.database.metric;

import at.uibk.dps.rm.entity.model.Metric;
import at.uibk.dps.rm.entity.model.MetricValue;
import at.uibk.dps.rm.repository.metric.MetricValueRepository;
import at.uibk.dps.rm.testutil.objectprovider.TestMetricProvider;
import at.uibk.dps.rm.util.JsonMapperConfig;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
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
import static org.mockito.Mockito.*;

@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class MetricValueServiceImplTest {

    private MetricValueService metricValueService;

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
        MetricValue mv1 = TestMetricProvider.createMetricValue(1L, 1L, "availability", 0.99);
        MetricValue mv2 = TestMetricProvider.createMetricValue(2L, 2L, "latency", 40);
        List<JsonObject> metricValues = List.of(JsonObject.mapFrom(mv1), JsonObject.mapFrom(mv2));
        metricValues.forEach(entry -> entry.put("resource", new JsonObject("{\"resource_id\": 1}")));

        when(metricValueRepository.createAll(anyList())).thenReturn(completionStage);

        JsonArray data = new JsonArray(metricValues);

        metricValueService.saveAll(data)
                .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                    assertThat(result).isNull();
                    verify(metricValueRepository).createAll(anyList());
                    testContext.completeNow();
                })));
    }

    @Test
    void findOneEntityExists(VertxTestContext testContext) {
        long entityId = 1L;
        MetricValue entity = new MetricValue();
        entity.setMetricValueId(entityId);
        CompletionStage<MetricValue> completionStage = CompletionStages.completedFuture(entity);
        when(metricValueRepository.findByIdAndFetch(entityId)).thenReturn(completionStage);

        metricValueService.findOne(entityId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.getLong("metric_value_id")).isEqualTo(1L);
                assertThat(result.getJsonObject("resource")).isNull();
                verify(metricValueRepository).findByIdAndFetch(entityId);
                testContext.completeNow();
        })));
    }

    @Test
    void findOneEntityNotExists(VertxTestContext testContext) {
        long entityId = 1L;
        CompletionStage<MetricValue> completionStage = CompletionStages.completedFuture(null);
        when(metricValueRepository.findByIdAndFetch(entityId)).thenReturn(completionStage);

        metricValueService.findOne(entityId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isNull();
                verify(metricValueRepository).findByIdAndFetch(entityId);
                testContext.completeNow();
        })));
    }

    @Test
    void findAllByResourceWithValue(VertxTestContext testContext) {
        long resourceId = 1L;
        boolean includeValue = true;
        Metric metric1 = new Metric();
        metric1.setMetricId(1L);
        Metric metric2 = new Metric();
        metric2.setMetricId(2L);
        MetricValue entity1 = new MetricValue();
        entity1.setMetricValueId(1L);
        entity1.setMetric(metric1);
        entity1.setValueNumber(10.0);
        MetricValue entity2 = new MetricValue();
        entity2.setMetricValueId(2L);
        entity2.setMetric(metric2);
        entity2.setValueString("ubuntu");
        List<MetricValue> metricValueList = new ArrayList<>();
        metricValueList.add(entity1);
        metricValueList.add(entity2);
        CompletionStage<List<MetricValue>> completionStage = CompletionStages.completedFuture(metricValueList);
        when(metricValueRepository.findByResourceAndFetch(resourceId)).thenReturn(completionStage);

        metricValueService.findAllByResource(resourceId, includeValue)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(2);
                assertThat(result.getJsonObject(0).getLong("metric_value_id")).isEqualTo(1L);
                assertThat(result.getJsonObject(0).getDouble("value_number")).isEqualTo(10.0);
                assertThat(result.getJsonObject(1).getLong("metric_value_id")).isEqualTo(2L);
                assertThat(result.getJsonObject(1).getString("value_string")).isEqualTo("ubuntu");
                verify(metricValueRepository).findByResourceAndFetch(resourceId);
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
        when(metricValueRepository.findByResourceAndFetch(resourceId)).thenReturn(completionStage);

        metricValueService.findAllByResource(resourceId, includeValue)
                .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                    assertThat(result.size()).isEqualTo(2);
                    assertThat(result.getJsonObject(0).getLong("metric_id")).isEqualTo(1L);
                    assertThat(result.getJsonObject(1).getLong("metric_id")).isEqualTo(2L);
                    verify(metricValueRepository).findByResourceAndFetch(resourceId);
                    testContext.completeNow();
                })));
    }

    @Test
    void findAllByResourceEmptyResult(VertxTestContext testContext) {
        long resourceId = 1L;
        boolean includeValue = false;
        List<MetricValue> metricValueList = new ArrayList<>();
        CompletionStage<List<MetricValue>> completionStage = CompletionStages.completedFuture(metricValueList);
        when(metricValueRepository.findByResourceAndFetch(resourceId)).thenReturn(completionStage);

        metricValueService.findAllByResource(resourceId, includeValue)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(0);
                verify(metricValueRepository).findByResourceAndFetch(resourceId);
                testContext.completeNow();
        })));
    }


    @Test
    void findAllByResourceNullMetricValue(VertxTestContext testContext) {
        long resourceId = 1L;
        boolean includeValue = false;
        List<MetricValue> metricValueList = new ArrayList<>();
        metricValueList.add(null);
        CompletionStage<List<MetricValue>> completionStage = CompletionStages.completedFuture(metricValueList);
        when(metricValueRepository.findByResourceAndFetch(resourceId)).thenReturn(completionStage);

        metricValueService.findAllByResource(resourceId, includeValue)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(0);
                testContext.completeNow();
        })));
    }

    @Test
    void findOneByResourceAndMetricExists(VertxTestContext testContext) {
        long resourceId = 1L;
        long metricId = 2L;
        MetricValue entity = new MetricValue();
        entity.setMetricValueId(3L);
        CompletionStage<MetricValue> completionStage = CompletionStages.completedFuture(entity);
        when(metricValueRepository.findByResourceAndMetric(resourceId, metricId)).thenReturn(completionStage);

        metricValueService.findOneByResourceAndMetric(resourceId, metricId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.getLong("metric_value_id")).isEqualTo(3L);
                verify(metricValueRepository).findByResourceAndMetric(resourceId, metricId);
                testContext.completeNow();
        })));
    }

    @Test
    void findOneByResourceAndMetricNotExists(VertxTestContext testContext) {
        long resourceId = 1L;
        long metricId = 2L;
        CompletionStage<MetricValue> completionStage = CompletionStages.completedFuture(null);
        when(metricValueRepository.findByResourceAndMetric(resourceId, metricId)).thenReturn(completionStage);

        metricValueService.findOneByResourceAndMetric(resourceId, metricId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isNull();
                verify(metricValueRepository).findByResourceAndMetric(resourceId, metricId);
                testContext.completeNow();
        })));
    }

    @Test
    void checkOneByResourceAndMetricExists(VertxTestContext testContext) {
        long resourceId = 1L;
        long metricId = 2L;
        MetricValue entity = new MetricValue();
        CompletionStage<MetricValue> completionStage = CompletionStages.completedFuture(entity);
        when(metricValueRepository.findByResourceAndMetric(resourceId, metricId)).thenReturn(completionStage);

        metricValueService.existsOneByResourceAndMetric(resourceId, metricId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isEqualTo(true);
                verify(metricValueRepository).findByResourceAndMetric(resourceId, metricId);
                testContext.completeNow();
        })));
    }

    @Test
    void checkOneByResourceAndMetricNotExists(VertxTestContext testContext) {
        long resourceId = 1L;
        long metricId = 2L;
        CompletionStage<MetricValue> completionStage = CompletionStages.completedFuture(null);
        when(metricValueRepository.findByResourceAndMetric(resourceId, metricId)).thenReturn(completionStage);

        metricValueService.existsOneByResourceAndMetric(resourceId, metricId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isEqualTo(false);
                verify(metricValueRepository).findByResourceAndMetric(resourceId, metricId);
                testContext.completeNow();
        })));
    }

    @Test
    void updateByResourceAndMetric(VertxTestContext testContext) {
        long resourceId = 1L;
        long metricId = 2L;
        double valueNumber = 13.37;
        CompletionStage<Void> completionStage = CompletionStages.completedFuture(null);
        when(metricValueRepository.updateByResourceAndMetric(resourceId, metricId, null, valueNumber, null))
            .thenReturn(completionStage);

        metricValueService.updateByResourceAndMetric(resourceId, metricId, null, valueNumber, null)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isNull();
                verify(metricValueRepository)
                        .updateByResourceAndMetric(resourceId, metricId, null, valueNumber, null);
                testContext.completeNow();
        })));
    }

    @Test
    void deleteByResourceAndMetric(VertxTestContext testContext) {
        long resourceId = 1L;
        long metricId = 2L;
        CompletionStage<Integer> completionStage = CompletionStages.completedFuture(0);
        when(metricValueRepository.deleteByResourceAndMetric(resourceId, metricId)).thenReturn(completionStage);

        metricValueService.deleteByResourceAndMetric(resourceId, metricId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isNull();
                verify(metricValueRepository).deleteByResourceAndMetric(resourceId, metricId);
                testContext.completeNow();
        })));
    }
}
