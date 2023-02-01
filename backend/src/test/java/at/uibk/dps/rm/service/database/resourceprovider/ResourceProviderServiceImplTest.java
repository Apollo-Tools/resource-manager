package at.uibk.dps.rm.service.database.resourceprovider;

import at.uibk.dps.rm.entity.model.AccountCredentials;
import at.uibk.dps.rm.entity.model.ResourceProvider;
import at.uibk.dps.rm.repository.resourceprovider.ResourceProviderRepository;
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

import java.util.concurrent.CompletionStage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class ResourceProviderServiceImplTest {

    private ResourceProviderService resourceProviderService;

    @Mock
    ResourceProviderRepository resourceProviderRepository;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        resourceProviderService = new ResourceProviderServiceImpl(resourceProviderRepository);
    }

    @Test
    void checkEntityByProviderExists(VertxTestContext testContext) {
        String provider = "aws";
        ResourceProvider entity = TestObjectProvider.createResourceProvider(1L, provider);
        CompletionStage<ResourceProvider> completionStage = CompletionStages.completedFuture(entity);
        doReturn(completionStage).when(resourceProviderRepository).findByProvider(provider);

        resourceProviderService.existsOneByProvider(provider)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isEqualTo(true);
                testContext.completeNow();
            })));
    }
    @Test
    void checkEntityByProviderNotExists(VertxTestContext testContext) {
        String provider = "aws";
        CompletionStage<AccountCredentials> completionStage = CompletionStages.completedFuture(null);
        doReturn(completionStage).when(resourceProviderRepository).findByProvider(provider);

        resourceProviderService.existsOneByProvider(provider)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isEqualTo(false);
                testContext.completeNow();
            })));
    }
}
