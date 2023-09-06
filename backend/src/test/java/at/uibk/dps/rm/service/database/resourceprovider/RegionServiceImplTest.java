package at.uibk.dps.rm.service.database.resourceprovider;

import at.uibk.dps.rm.entity.model.Region;
import at.uibk.dps.rm.entity.model.ResourceProvider;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.repository.resourceprovider.RegionRepository;
import at.uibk.dps.rm.repository.resourceprovider.ResourceProviderRepository;
import at.uibk.dps.rm.testutil.SessionMockHelper;
import at.uibk.dps.rm.testutil.objectprovider.TestResourceProviderProvider;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import at.uibk.dps.rm.service.database.util.SessionManager;
import org.hibernate.reactive.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Implements tests for the {@link RegionServiceImpl} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class RegionServiceImplTest {

    private RegionService regionService;

    @Mock
    private RegionRepository regionRepository;

    @Mock
    private ResourceProviderRepository providerRepository;

    @Mock
    private Stage.SessionFactory sessionFactory;

    @Mock
    private Stage.Session session;
    
    private SessionManager sessionManager;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        regionService = new RegionServiceImpl(regionRepository, providerRepository, sessionFactory);
    }

    @Test
    void findEntityExists(VertxTestContext testContext) {
        long regionId = 1L;
        Region entity = TestResourceProviderProvider.createRegion(regionId, "us-east");

        sessionManager = SessionMockHelper.mockTransaction(sessionFactory, session);
        when(regionRepository.findByIdAndFetch(sessionManager, regionId))
            .thenReturn(Maybe.just(entity));

        regionService.findOne(regionId, testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.getLong("region_id")).isEqualTo(1L);
                assertThat(result.getJsonObject("resource_provider")).isNotNull();
                testContext.completeNow();
            })));
    }

    @Test
    void findEntityNotExists(VertxTestContext testContext) {
        long regionId = 1L;

        sessionManager = SessionMockHelper.mockTransaction(sessionFactory, session);
        when(regionRepository.findByIdAndFetch(sessionManager, regionId))
            .thenReturn(Maybe.empty());

        regionService.findOne(regionId, testContext.failing(throwable -> testContext.verify(() -> {
            assertThat(throwable).isInstanceOf(NotFoundException.class);
            testContext.completeNow();
        })));
    }

    @Test
    void findAll(VertxTestContext testContext) {
        Region r1 = TestResourceProviderProvider.createRegion(1L, "us-east");
        Region r2 = TestResourceProviderProvider.createRegion(2L, "us-west");

        sessionManager = SessionMockHelper.mockTransaction(sessionFactory, session);
        when(regionRepository.findAllAndFetch(sessionManager))
            .thenReturn(Single.just(List.of(r1, r2)));

        regionService.findAll(testContext.succeeding(result -> testContext.verify(() -> {
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
        ResourceProvider resourceProvider = TestResourceProviderProvider.createResourceProvider(providerId);
        Region r1 = TestResourceProviderProvider.createRegion(1L, "us-east", resourceProvider);
        Region r2 = TestResourceProviderProvider.createRegion(2L, "us-west", resourceProvider);

        sessionManager = SessionMockHelper.mockTransaction(sessionFactory, session);
        when(regionRepository.findAllByProviderId(sessionManager, providerId))
            .thenReturn(Single.just(List.of(r1, r2)));

        regionService.findAllByProviderId(providerId, testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(2);
                assertThat(result.getJsonObject(0).getLong("region_id")).isEqualTo(1L);
                assertThat(result.getJsonObject(1).getLong("region_id")).isEqualTo(2L);
                testContext.completeNow();
            })));
    }
}
