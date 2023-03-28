package at.uibk.dps.rm.service.database.resource;

import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.repository.resource.ResourceRepository;
import at.uibk.dps.rm.testutil.TestObjectProvider;
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
        Resource entity = TestObjectProvider.createResource(resourceId);
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
        CompletionStage<List<Resource>> completionStage = CompletionStages.completedFuture(List.of(entity));

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
        CompletionStage<List<Resource>> completionStage = CompletionStages.completedFuture(List.of());

        doReturn(completionStage).when(resourceRepository).findByResourceType(typeId);

        resourceService.existsOneByResourceType(typeId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isEqualTo(false);
                verify(resourceRepository).findByResourceType(typeId);
                testContext.completeNow();
        })));
    }

    @Test
    void findAll(VertxTestContext testContext) {
        Resource r1 = TestObjectProvider.createResource(1L);
        Resource r2 = TestObjectProvider.createResource(2L);
        CompletionStage<List<Resource>> completionStage = CompletionStages.completedFuture(List.of(r1, r2));

        doReturn(completionStage).when(resourceRepository).findAllAndFetch();

        resourceService.findAll()
                .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                    assertThat(result.size()).isEqualTo(2);
                    assertThat(result.getJsonObject(0).getLong("resource_id")).isEqualTo(1L);
                    assertThat(result.getJsonObject(1).getLong("resource_id")).isEqualTo(2L);
                    verify(resourceRepository).findAllAndFetch();
                    testContext.completeNow();
                })));
    }

    // TODO: refine
    @Test
    void findAllBySLOs(VertxTestContext testContext) {
        long functionId = 1L;
        List<String> metrics = List.of("availability");
        Resource entity1 = TestObjectProvider.createResource(1L);
        CompletionStage<List<Resource>> completionStage = CompletionStages.completedFuture(List.of(entity1));
        List<String> regions = new ArrayList<>();
        List<Long> resourceProviders = new ArrayList<>();
        List<Long> resourceTypes = new ArrayList<>();


        doReturn(completionStage).when(resourceRepository).findAllBySLOs(functionId, metrics, regions, resourceProviders,
            resourceTypes);

        resourceService.findAllBySLOs(functionId, metrics, regions, resourceProviders, resourceTypes)
                .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                    assertThat(result.size()).isEqualTo(1);
                    assertThat(result.getJsonObject(0).getLong("resource_id")).isEqualTo(1L);
                    testContext.completeNow();
                })));
    }

    @Test
    void findAllByFunctionId(VertxTestContext testContext) {
        long functionId = 1L;
        Resource r1 = TestObjectProvider.createResource(1L);
        Resource r2 = TestObjectProvider.createResource(2L);
        CompletionStage<List<Resource>> completionStage = CompletionStages.completedFuture(List.of(r1, r2));

        doReturn(completionStage).when(resourceRepository).findAllByFunctionIdAndFetch(functionId);

        resourceService.findAllByFunctionId(functionId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(2);
                assertThat(result.getJsonObject(0).getLong("resource_id")).isEqualTo(1L);
                assertThat(result.getJsonObject(1).getLong("resource_id")).isEqualTo(2L);
                verify(resourceRepository).findAllByFunctionIdAndFetch(functionId);
                testContext.completeNow();
            })));
    }
}
