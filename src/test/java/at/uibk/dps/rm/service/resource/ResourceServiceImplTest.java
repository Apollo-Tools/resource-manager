package at.uibk.dps.rm.service.resource;


import at.uibk.dps.rm.entity.model.MetricValue;
import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.entity.model.ResourceType;
import at.uibk.dps.rm.repository.ResourceRepository;
import at.uibk.dps.rm.service.database.resource.ResourceServiceImpl;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletionStage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class ResourceServiceImplTest {

    private ResourceServiceImpl resourceService;

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
        Set<MetricValue> metricValueSet = new HashSet<>();
        entity.setMetricValues(metricValueSet);
        CompletionStage<Resource> completionStage = CompletionStages.completedFuture(entity);
        doReturn(completionStage).when(resourceRepository).findByIdAndFetch(resourceId);

        resourceService.findOne(resourceId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.getLong("resource_id")).isEqualTo(1L);
                assertThat(result.getJsonArray("metric_values")).isNull();
                verify(resourceRepository, times(1)).findByIdAndFetch(resourceId);
                verify(resourceRepository, times(0)).findById(resourceId);
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
                verify(resourceRepository, times(1)).findByIdAndFetch(resourceId);
                verify(resourceRepository, times(0)).findById(resourceId);
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
                verify(resourceRepository, times(1)).findByResourceType(typeId);
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
                verify(resourceRepository, times(1)).findByResourceType(typeId);
                testContext.completeNow();
        })));
    }


}
