package at.uibk.dps.rm.service.database.resourceprovider;

import at.uibk.dps.rm.entity.model.Region;
import at.uibk.dps.rm.entity.model.ResourceProvider;
import at.uibk.dps.rm.exception.AlreadyExistsException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.repository.resourceprovider.RegionRepository;
import at.uibk.dps.rm.service.database.util.SessionManagerProvider;
import at.uibk.dps.rm.testutil.SessionMockHelper;
import at.uibk.dps.rm.testutil.objectprovider.TestResourceProviderProvider;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import at.uibk.dps.rm.service.database.util.SessionManager;
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
    private SessionManagerProvider smProvider;

    @Mock
    private SessionManager sessionManager;

    private Region r1, r2;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        regionService = new RegionServiceImpl(regionRepository, smProvider);
        r1 = TestResourceProviderProvider.createRegion(1L, "us-east");
        r2 = TestResourceProviderProvider.createRegion(2L, "us-west");
    }

    @Test
    void findOne(VertxTestContext testContext) {
        SessionMockHelper.mockMaybe(smProvider, sessionManager);
        when(regionRepository.findByIdAndFetch(sessionManager, r1.getRegionId())).thenReturn(Maybe.just(r1));

        regionService.findOne(r1.getRegionId(), testContext.succeeding(result -> testContext.verify(() -> {
            assertThat(result.getLong("region_id")).isEqualTo(1L);
            testContext.completeNow();
        })));
    }

    @Test
    void findOneNotFound(VertxTestContext testContext) {
        SessionMockHelper.mockMaybe(smProvider, sessionManager);
        when(regionRepository.findByIdAndFetch(sessionManager, r1.getRegionId())).thenReturn(Maybe.empty());

        regionService.findOne(r1.getRegionId(), testContext.failing(throwable -> testContext.verify(() -> {
            assertThat(throwable).isInstanceOf(NotFoundException.class);
            testContext.completeNow();
        })));
    }

    @Test
    void findAll(VertxTestContext testContext) {
        SessionMockHelper.mockSingle(smProvider, sessionManager);
        when(regionRepository.findAllAndFetch(sessionManager)).thenReturn(Single.just(List.of(r1, r2)));

        regionService.findAll(testContext.succeeding(result -> testContext.verify(() -> {
            assertThat(result.size()).isEqualTo(2);
            assertThat(result.getJsonObject(0).getLong("region_id")).isEqualTo(1L);
            assertThat(result.getJsonObject(1).getLong("region_id")).isEqualTo(2L);
            testContext.completeNow();
        })));
    }

    @Test
    void findAllByProviderId(VertxTestContext testContext) {
        long providerId = 3L;

        SessionMockHelper.mockSingle(smProvider, sessionManager);
        when(regionRepository.findAllByProvider(sessionManager, providerId)).thenReturn(Single.just(List.of(r1, r2)));

        regionService.findAllByProviderId(providerId, testContext.succeeding(result -> testContext.verify(() -> {
            assertThat(result.size()).isEqualTo(2);
            assertThat(result.getJsonObject(0).getLong("region_id")).isEqualTo(1L);
            assertThat(result.getJsonObject(1).getLong("region_id")).isEqualTo(2L);
            testContext.completeNow();
        })));
    }

    @Test
    void findAllByPlatformId(VertxTestContext testContext) {
        long platformId = 2L;

        SessionMockHelper.mockSingle(smProvider, sessionManager);
        when(regionRepository.findAllByPlatformId(sessionManager, platformId)).thenReturn(Single.just(List.of(r1, r2)));

        regionService.findAllByPlatformId(platformId, testContext.succeeding(result -> testContext.verify(() -> {
            assertThat(result.size()).isEqualTo(2);
            assertThat(result.getJsonObject(0).getLong("region_id")).isEqualTo(1L);
            assertThat(result.getJsonObject(1).getLong("region_id")).isEqualTo(2L);
            testContext.completeNow();
        })));
    }

    @Test
    void save(VertxTestContext testContext) {
        SessionMockHelper.mockSingle(smProvider, sessionManager);
        when(regionRepository.findOneByNameAndProviderId(sessionManager, r1.getName(),
            r1.getResourceProvider().getProviderId())).thenReturn(Maybe.empty());
        when(sessionManager.find(ResourceProvider.class, r1.getResourceProvider().getProviderId()))
            .thenReturn(Maybe.just(r1.getResourceProvider()));
        when(sessionManager.persist(r1)).thenReturn(Single.just(r1));
        regionService.save(JsonObject.mapFrom(r1), testContext.succeeding(result -> testContext.verify(() -> {
            assertThat(result.getLong("region_id")).isEqualTo(1L);
            testContext.completeNow();
        })));
    }

    @Test
    void saveResourceProviderNotFound(VertxTestContext testContext) {
        SessionMockHelper.mockSingle(smProvider, sessionManager);
        when(regionRepository.findOneByNameAndProviderId(sessionManager, r1.getName(),
            r1.getResourceProvider().getProviderId())).thenReturn(Maybe.empty());
        when(sessionManager.find(ResourceProvider.class, r1.getResourceProvider().getProviderId()))
            .thenReturn(Maybe.empty());
        regionService.save(JsonObject.mapFrom(r1), testContext.failing(throwable -> testContext.verify(() -> {
            assertThat(throwable).isInstanceOf(NotFoundException.class);
            testContext.completeNow();
        })));
    }

    @Test
    void saveAlreadyExists(VertxTestContext testContext) {
        SessionMockHelper.mockSingle(smProvider, sessionManager);
        when(regionRepository.findOneByNameAndProviderId(sessionManager, r1.getName(),
            r1.getResourceProvider().getProviderId())).thenReturn(Maybe.just(r1));
        when(sessionManager.find(ResourceProvider.class, r1.getResourceProvider().getProviderId()))
            .thenReturn(Maybe.just(r1.getResourceProvider()));
        regionService.save(JsonObject.mapFrom(r1), testContext.failing(throwable -> testContext.verify(() -> {
            assertThat(throwable).isInstanceOf(AlreadyExistsException.class);
            testContext.completeNow();
        })));
    }
}
