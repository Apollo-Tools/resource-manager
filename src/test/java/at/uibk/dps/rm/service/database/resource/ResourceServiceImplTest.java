package at.uibk.dps.rm.service.database.resource;

import at.uibk.dps.rm.entity.model.MetricValue;
import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.repository.ResourceRepository;
import at.uibk.dps.rm.util.JsonMapperConfig;
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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class ResourceServiceImplTest {

    private ResourceService resourceService;

    @Mock
    ResourceRepository resourceRepository;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        resourceService = new ResourceServiceImpl(resourceRepository);
    }

    @Test
    void findEntityExists(VertxTestContext testContext) {
        long resourceId = 1L;
        Resource entity = new Resource();
        entity.setResourceId(resourceId);
        List<MetricValue> metricValueSet = new ArrayList<>();
        entity.setMetricValues(metricValueSet);
        CompletionStage<Resource> completionStage = CompletionStages.completedFuture(entity);
        doReturn(completionStage).when(resourceRepository).findByIdAndFetch(resourceId);

        resourceService.findOne(resourceId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.getLong("resource_id")).isEqualTo(1L);
                assertThat(result.getJsonArray("metric_values")).isNull();
                verify(resourceRepository).findByIdAndFetch(resourceId);
                testContext.completeNow();
        })));
    }

    @Test
    void findEntityNotExists(VertxTestContext testContext) {
        long resourceId = 1L;
        CompletionStage<Resource> completionStage = CompletionStages.completedFuture(null);
        doReturn(completionStage).when(resourceRepository).findByIdAndFetch(resourceId);

        resourceService.findOne(resourceId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isNull();
                verify(resourceRepository).findByIdAndFetch(resourceId);
                testContext.completeNow();
        })));
    }

    @Test
    void checkEntityByResourceTypeExists(VertxTestContext testContext) {
        long typeId = 1L;
        Resource entity = new Resource();
        List<Resource> resultList = new ArrayList<>();
        resultList.add(entity);
        CompletionStage<List<Resource>> completionStage = CompletionStages.completedFuture(resultList);
        doReturn(completionStage).when(resourceRepository).findByResourceType(typeId);

        resourceService.existsOneByResourceType(typeId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isEqualTo(true);
                verify(resourceRepository).findByResourceType(typeId);
                testContext.completeNow();
        })));
    }

    @Test
    void checkEntityNotExists(VertxTestContext testContext) {
        long typeId = 1L;
        List<Resource> resultList = new ArrayList<>();
        CompletionStage<List<Resource>> completionStage = CompletionStages.completedFuture(resultList);
        doReturn(completionStage).when(resourceRepository).findByResourceType(typeId);

        resourceService.existsOneByResourceType(typeId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isEqualTo(false);
                verify(resourceRepository).findByResourceType(typeId);
                testContext.completeNow();
        })));
    }

    @Test
    void checkEntityExistsAndNotReserved(VertxTestContext testContext) {
        long resourceId = 1L;
        CompletionStage<Long> completionStage = CompletionStages.completedFuture(1L);
        doReturn(completionStage).when(resourceRepository).findByIdAndNotReserved(resourceId);

        resourceService.existsOneAndNotReserved(resourceId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isEqualTo(true);
                verify(resourceRepository).findByIdAndNotReserved(resourceId);
                testContext.completeNow();
        })));
    }

    @Test
    void checkEntityNotExistsOrReserved(VertxTestContext testContext) {
        long resourceId = 1L;
        CompletionStage<Long> completionStage = CompletionStages.completedFuture(null);
        doReturn(completionStage).when(resourceRepository).findByIdAndNotReserved(resourceId);

        resourceService.existsOneAndNotReserved(resourceId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isEqualTo(false);
                verify(resourceRepository).findByIdAndNotReserved(1L);
                testContext.completeNow();
        })));
    }

    @Test
    void findAll(VertxTestContext testContext) {
        Resource entity1 = new Resource();
        entity1.setResourceId(1L);
        List<MetricValue> metricValues1 = new ArrayList<>();
        entity1.setMetricValues(metricValues1);
        Resource entity2 = new Resource();
        entity2.setResourceId(2L);
        List<MetricValue> metricValues2 = new ArrayList<>();
        entity2.setMetricValues(metricValues2);
        List<Resource> resultList = new ArrayList<>();
        resultList.add(entity1);
        resultList.add(entity2);
        CompletionStage<List<Resource>> completionStage = CompletionStages.completedFuture(resultList);
        doReturn(completionStage).when(resourceRepository).findAllAndFetch(false);

        resourceService.findAll()
                .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                    assertThat(result.size()).isEqualTo(2);
                    assertThat(result.getJsonObject(0).getLong("resource_id")).isEqualTo(1L);
                    assertThat(result.getJsonObject(1).getLong("resource_id")).isEqualTo(2L);
                    verify(resourceRepository).findAllAndFetch(false);
                    testContext.completeNow();
                })));
    }

    @Test
    void findAllByMultipleMetrics(VertxTestContext testContext) {
        List<String> metrics = new ArrayList<>();
        metrics.add("availability");
        Resource entity1 = new Resource();
        entity1.setResourceId(1L);
        List<MetricValue> metricValues1 = new ArrayList<>();
        entity1.setMetricValues(metricValues1);
        List<Resource> resultList = new ArrayList<>();
        resultList.add(entity1);
        CompletionStage<List<Resource>> completionStage = CompletionStages.completedFuture(resultList);
        doReturn(completionStage).when(resourceRepository).findByMultipleMetricsAndFetch(metrics);

        resourceService.findAllByMultipleMetrics(metrics)
                .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                    assertThat(result.size()).isEqualTo(1);
                    assertThat(result.getJsonObject(0).getLong("resource_id")).isEqualTo(1L);
                    verify(resourceRepository).findByMultipleMetricsAndFetch(metrics);
                    testContext.completeNow();
                })));
    }
}
