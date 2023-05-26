package at.uibk.dps.rm.service.database.resource;

import at.uibk.dps.rm.entity.dto.resource.ResourceTypeEnum;
import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.repository.resource.ResourceRepository;
import at.uibk.dps.rm.testutil.objectprovider.TestResourceProvider;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.hibernate.reactive.util.impl.CompletionStages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletionStage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Implements tests for the {@link ResourceServiceImpl} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class ResourceServiceImplTest {

    private ResourceService resourceService;

    @Mock
    private ResourceRepository resourceRepository;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        resourceService = new ResourceServiceImpl(resourceRepository);
    }

    @Test
    void findEntityExists(VertxTestContext testContext) {
        long resourceId = 1L;
        Resource entity = TestResourceProvider.createResource(resourceId);
        CompletionStage<Resource> completionStage = CompletionStages.completedFuture(entity);

        when(resourceRepository.findByIdAndFetch(resourceId)).thenReturn(completionStage);

        resourceService.findOne(resourceId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.getLong("resource_id")).isEqualTo(1L);
                assertThat(result.getJsonArray("metric_values")).isNull();
                testContext.completeNow();
        })));
    }

    @Test
    void findEntityNotExists(VertxTestContext testContext) {
        long resourceId = 1L;
        CompletionStage<Resource> completionStage = CompletionStages.completedFuture(null);

        when(resourceRepository.findByIdAndFetch(resourceId)).thenReturn(completionStage);

        resourceService.findOne(resourceId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isNull();
                testContext.completeNow();
        })));
    }

    @Test
    void checkEntityByResourceTypeExists(VertxTestContext testContext) {
        long typeId = 1L;
        Resource entity = new Resource();
        CompletionStage<List<Resource>> completionStage = CompletionStages.completedFuture(List.of(entity));

        when(resourceRepository.findByResourceType(typeId)).thenReturn(completionStage);

        resourceService.existsOneByResourceType(typeId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isEqualTo(true);
                testContext.completeNow();
        })));
    }

    @Test
    void checkEntityNotExists(VertxTestContext testContext) {
        long typeId = 1L;
        CompletionStage<List<Resource>> completionStage = CompletionStages.completedFuture(List.of());

        when(resourceRepository.findByResourceType(typeId)).thenReturn(completionStage);

        resourceService.existsOneByResourceType(typeId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isEqualTo(false);
                testContext.completeNow();
        })));
    }

    @Test
    void findAll(VertxTestContext testContext) {
        Resource r1 = TestResourceProvider.createResource(1L);
        Resource r2 = TestResourceProvider.createResource(2L);
        CompletionStage<List<Resource>> completionStage = CompletionStages.completedFuture(List.of(r1, r2));

        when(resourceRepository.findAllAndFetch()).thenReturn(completionStage);

        resourceService.findAll()
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(2);
                assertThat(result.getJsonObject(0).getLong("resource_id")).isEqualTo(1L);
                assertThat(result.getJsonObject(1).getLong("resource_id")).isEqualTo(2L);
                testContext.completeNow();
            })));
    }

    @Test
    void findAllBySLOs(VertxTestContext testContext) {
        List<String> metrics = List.of("availability");
        Resource entity1 = TestResourceProvider.createResource(1L);
        CompletionStage<List<Resource>> completionStage = CompletionStages.completedFuture(List.of(entity1));
        List<Long> regions = new ArrayList<>();
        List<Long> resourceProviders = new ArrayList<>();
        List<Long> resourceTypes = new ArrayList<>();


        when(resourceRepository.findAllBySLOs(metrics, regions, resourceProviders, resourceTypes))
            .thenReturn(completionStage);

        resourceService.findAllBySLOs(metrics, regions, resourceProviders, resourceTypes)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(1);
                assertThat(result.getJsonObject(0).getLong("resource_id")).isEqualTo(1L);
                testContext.completeNow();
            })));
    }

    @Test
    void findAllByFunctionId(VertxTestContext testContext) {
        long functionId = 1L;
        Resource r1 = TestResourceProvider.createResource(1L);
        Resource r2 = TestResourceProvider.createResource(2L);
        CompletionStage<List<Resource>> completionStage = CompletionStages.completedFuture(List.of(r1, r2));

        when(resourceRepository.findAllByFunctionIdAndFetch(functionId)).thenReturn(completionStage);

        resourceService.findAllByFunctionId(functionId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(2);
                assertThat(result.getJsonObject(0).getLong("resource_id")).isEqualTo(1L);
                assertThat(result.getJsonObject(1).getLong("resource_id")).isEqualTo(2L);
                testContext.completeNow();
            })));
    }

    @Test
    void findAllByEnsembleId(VertxTestContext testContext) {
        long ensembleId = 1L;
        Resource r1 = TestResourceProvider.createResource(1L);
        Resource r2 = TestResourceProvider.createResource(2L);
        CompletionStage<List<Resource>> completionStage = CompletionStages.completedFuture(List.of(r1, r2));

        when(resourceRepository.findAllByEnsembleId(ensembleId)).thenReturn(completionStage);

        resourceService.findAllByEnsembleId(ensembleId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(2);
                assertThat(result.getJsonObject(0).getLong("resource_id")).isEqualTo(1L);
                assertThat(result.getJsonObject(1).getLong("resource_id")).isEqualTo(2L);
                testContext.completeNow();
            })));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void existsAllByIdsAndResourceTypes(boolean allExist, VertxTestContext testContext) {
        Set<Long> resourceIds = Set.of(1L, 2L);
        List<String> resourceTypes = List.of(ResourceTypeEnum.EDGE.getValue(), ResourceTypeEnum.VM.getValue());
        Resource r1 = TestResourceProvider.createResource(1L);
        Resource r2 = TestResourceProvider.createResource(2L);
        List<Resource> resources = List.of(r1, r2);
        if (!allExist) {
            resources = List.of(r2);
        }
        CompletionStage<List<Resource>> completionStage = CompletionStages.completedFuture(resources);

        when(resourceRepository.findAllByResourceIdsAndResourceTypes(resourceIds, resourceTypes))
            .thenReturn(completionStage);

        resourceService.existsAllByIdsAndResourceTypes(resourceIds, resourceTypes)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isEqualTo(allExist);
                testContext.completeNow();
            })));
    }

    @Test
    void findAllByResourceIds(VertxTestContext testContext) {
        List<Long> resourceIds = List.of(1L, 2L);
        Resource r1 = TestResourceProvider.createResource(1L);
        Resource r2 = TestResourceProvider.createResource(2L);
        CompletionStage<List<Resource>> completionStage = CompletionStages.completedFuture(List.of(r1, r2));

        when(resourceRepository.findAllByResourceIdsAndFetch(resourceIds)).thenReturn(completionStage);

        resourceService.findAllByResourceIds(resourceIds)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(2);
                assertThat(result.getJsonObject(0).getLong("resource_id")).isEqualTo(1L);
                assertThat(result.getJsonObject(1).getLong("resource_id")).isEqualTo(2L);
                testContext.completeNow();
            })));
    }
}
