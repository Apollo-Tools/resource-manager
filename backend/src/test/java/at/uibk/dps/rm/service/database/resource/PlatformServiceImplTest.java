package at.uibk.dps.rm.service.database.resource;

import at.uibk.dps.rm.entity.dto.resource.ResourceProviderEnum;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.repository.resource.PlatformRepository;
import at.uibk.dps.rm.service.database.util.SessionManager;
import at.uibk.dps.rm.service.database.util.SessionManagerProvider;
import at.uibk.dps.rm.testutil.SessionMockHelper;
import at.uibk.dps.rm.testutil.objectprovider.TestPlatformProvider;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.reactivex.rxjava3.core.Single;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Implements tests for the {@link PlatformServiceImpl} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class PlatformServiceImplTest {

    private PlatformService platformService;

    @Mock
    private PlatformRepository platformRepository;

    @Mock
    private SessionManagerProvider smProvider;

    @Mock
    private SessionManager sessionManager;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        platformService = new PlatformServiceImpl(platformRepository, smProvider);

    }

    @Test
    void findAll(VertxTestContext testContext) {
        Platform p1 = TestPlatformProvider.createPlatformFaas(1L, "lambda");
        Platform p2 = TestPlatformProvider.createPlatformFaas(2L, "openfaas");

        SessionMockHelper.mockSingle(smProvider, sessionManager);
        when(platformRepository.findAllAndFetch(sessionManager)).thenReturn(Single.just(List.of(p1, p2)));

        platformService.findAll(testContext.succeeding(result -> testContext.verify(() -> {
            assertThat(result.size()).isEqualTo(2);
            assertThat(result.getJsonObject(0).getLong("platform_id")).isEqualTo(1L);
            assertThat(result.getJsonObject(1).getLong("platform_id")).isEqualTo(2L);
            testContext.completeNow();
        })));
    }

    @Test
    void findAllByResourceProvider(VertxTestContext testContext) {
        Platform p1 = TestPlatformProvider.createPlatformFaas(1L, "lambda");
        Platform p2 = TestPlatformProvider.createPlatformFaas(2L, "openfaas");

        SessionMockHelper.mockSingle(smProvider, sessionManager);
        when(platformRepository.findAllByResourceProvider(sessionManager, ResourceProviderEnum.AWS.getValue()))
            .thenReturn(Single.just(List.of(p1, p2)));

        platformService.findAllByResourceProvider(ResourceProviderEnum.AWS.getValue(),
            testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(2);
                assertThat(result.getJsonObject(0).getLong("platform_id")).isEqualTo(1L);
                assertThat(result.getJsonObject(1).getLong("platform_id")).isEqualTo(2L);
                testContext.completeNow();
        })));
    }
}
