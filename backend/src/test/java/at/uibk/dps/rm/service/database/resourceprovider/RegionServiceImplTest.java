package at.uibk.dps.rm.service.database.resourceprovider;

import at.uibk.dps.rm.entity.model.Region;
import at.uibk.dps.rm.entity.model.ResourceProvider;
import at.uibk.dps.rm.repository.resourceprovider.RegionRepository;
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

import java.util.List;
import java.util.concurrent.CompletionStage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class RegionServiceImplTest {

    private RegionService regionService;

    @Mock
    RegionRepository regionRepository;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        regionService = new RegionServiceImpl(regionRepository);
    }

    @Test
    void findEntityExists(VertxTestContext testContext) {
        long regionId = 1L;
        Region entity = TestObjectProvider.createRegion(regionId, "us-east");
        CompletionStage<Region> completionStage = CompletionStages.completedFuture(entity);

        when(regionRepository.findByIdAndFetch(regionId)).thenReturn(completionStage);

        regionService.findOne(regionId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.getLong("region_id")).isEqualTo(1L);
                assertThat(result.getJsonObject("resource_provider")).isNotNull();
                testContext.completeNow();
            })));
    }

    @Test
    void findEntityNotExists(VertxTestContext testContext) {
        long regionId = 1L;
        CompletionStage<Region> completionStage = CompletionStages.completedFuture(null);

        when(regionRepository.findByIdAndFetch(regionId)).thenReturn(completionStage);

        regionService.findOne(regionId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isNull();
                testContext.completeNow();
            })));
    }

    @Test
    void findAll(VertxTestContext testContext) {
        Region r1 = TestObjectProvider.createRegion(1L, "us-east");
        Region r2 = TestObjectProvider.createRegion(2L, "us-west");
        CompletionStage<List<Region>> completionStage = CompletionStages.completedFuture(List.of(r1, r2));

        when(regionRepository.findAllAndFetch()).thenReturn(completionStage);

        regionService.findAll()
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(2);
                assertThat(result.getJsonObject(0).getLong("region_id")).isEqualTo(1L);
                assertThat(result.getJsonObject(1).getLong("region_id")).isEqualTo(2L);
                assertThat(result.getJsonObject(0).getJsonObject("resource_provider")).isNotNull();
                assertThat(result.getJsonObject(1).getJsonObject("resource_provider")).isNotNull();
                testContext.completeNow();
            })));
    }

    @Test
    void findAllByProviderId(VertxTestContext testContext) {
        long providerId = 3L;
        ResourceProvider resourceProvider = TestObjectProvider.createResourceProvider(providerId);
        Region r1 = TestObjectProvider.createRegion(1L, "us-east", resourceProvider);
        Region r2 = TestObjectProvider.createRegion(2L, "us-west", resourceProvider);
        CompletionStage<List<Region>> completionStage = CompletionStages.completedFuture(List.of(r1, r2));

        when(regionRepository.findAllByProviderId(providerId)).thenReturn(completionStage);

        regionService.findAllByProviderId(providerId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(2);
                assertThat(result.getJsonObject(0).getLong("region_id")).isEqualTo(1L);
                assertThat(result.getJsonObject(1).getLong("region_id")).isEqualTo(2L);
                assertThat(result.getJsonObject(0).getJsonObject("resource_provider")).isNull();
                assertThat(result.getJsonObject(1).getJsonObject("resource_provider")).isNull();
                testContext.completeNow();
            })));
    }

    @Test
    void checkEntityByNameAndProviderIdExists(VertxTestContext testContext) {
        String name = "us-east";
        long providerId = 1L;
        Region entity = new Region();
        CompletionStage<Region> completionStage = CompletionStages.completedFuture(entity);

        when(regionRepository.findOneByNameAndProviderId(name, providerId)).thenReturn(completionStage);

        regionService.existsOneByNameAndProviderId(name, providerId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isEqualTo(true);
                testContext.completeNow();
            })));
    }

    @Test
    void checkEntityByNameAndProviderIdNotExists(VertxTestContext testContext) {
        String name = "us-east";
        long providerId = 1L;
        CompletionStage<Region> completionStage = CompletionStages.completedFuture(null);

        when(regionRepository.findOneByNameAndProviderId(name, providerId)).thenReturn(completionStage);

        regionService.existsOneByNameAndProviderId(name, providerId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isEqualTo(false);
                testContext.completeNow();
            })));
    }
}
