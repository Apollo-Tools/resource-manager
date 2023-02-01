package at.uibk.dps.rm.service.database.resource;

import at.uibk.dps.rm.entity.model.ResourceType;
import at.uibk.dps.rm.repository.resource.ResourceTypeRepository;
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
public class ResourceTypeServiceImplTest {

    private ResourceTypeService resourceTypeService;

    @Mock
    ResourceTypeRepository resourceTypeRepository;

    @BeforeEach
    void initTest() {
        resourceTypeService = new ResourceTypeServiceImpl(resourceTypeRepository);
    }

    @Test
    void existsOneByResourceTypeTrue(VertxTestContext testContext) {
        String resourceType = "cloud";
        ResourceType entity = new ResourceType();
        entity.setTypeId(1L);
        entity.setResourceType("cloud");
        CompletionStage<ResourceType> completionStage = CompletionStages.completedFuture(entity);
        doReturn(completionStage).when(resourceTypeRepository).findByResourceType(resourceType);

        resourceTypeService.existsOneByResourceType(resourceType)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isEqualTo(true);
                verify(resourceTypeRepository).findByResourceType(resourceType);
                testContext.completeNow();
        })));
    }

    @Test
    void existsOneByResourceTypeFalse(VertxTestContext testContext) {
        String resourceType = "cloud";
        CompletionStage<ResourceType> completionStage = CompletionStages.completedFuture(null);
        doReturn(completionStage).when(resourceTypeRepository).findByResourceType(resourceType);

        resourceTypeService.existsOneByResourceType(resourceType)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isEqualTo(false);
                verify(resourceTypeRepository).findByResourceType(resourceType);
                testContext.completeNow();
        })));
    }
}
