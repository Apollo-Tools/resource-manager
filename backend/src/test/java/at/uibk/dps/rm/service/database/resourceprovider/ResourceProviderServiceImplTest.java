package at.uibk.dps.rm.service.database.resourceprovider;

import at.uibk.dps.rm.entity.dto.resource.ResourceProviderEnum;
import at.uibk.dps.rm.entity.model.ResourceProvider;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.repository.resourceprovider.ResourceProviderRepository;
import at.uibk.dps.rm.service.database.util.SessionManager;
import at.uibk.dps.rm.service.database.util.SessionManagerProvider;
import at.uibk.dps.rm.testutil.SessionMockHelper;
import at.uibk.dps.rm.testutil.objectprovider.TestResourceProviderProvider;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.reactivex.rxjava3.core.Maybe;
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
 * Implements tests for the {@link ResourceProviderServiceImpl} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class ResourceProviderServiceImplTest {


    private ResourceProviderService resourceProviderService;

    @Mock
    private ResourceProviderRepository repository;

    @Mock
    private SessionManagerProvider smProvider;

    @Mock
    private SessionManager sessionManager;

    private ResourceProvider rpAWS, rpEdge;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        resourceProviderService = new ResourceProviderServiceImpl(repository, smProvider);
        rpAWS = TestResourceProviderProvider.createResourceProvider(1L, ResourceProviderEnum.AWS.getValue());
        rpEdge = TestResourceProviderProvider.createResourceProvider(2L,
            ResourceProviderEnum.CUSTOM_EDGE.getValue());
    }

    @Test
    void findOne(VertxTestContext testContext) {
        SessionMockHelper.mockMaybe(smProvider, sessionManager);
        when(repository.findByIdAndFetch(sessionManager, rpAWS.getProviderId())).thenReturn(Maybe.just(rpAWS));

        resourceProviderService.findOne(rpAWS.getProviderId(),
            testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.getLong("provider_id")).isEqualTo(1L);
                testContext.completeNow();
            })));
    }

    @Test
    void findOneNotFound(VertxTestContext testContext) {
        SessionMockHelper.mockMaybe(smProvider, sessionManager);
        when(repository.findByIdAndFetch(sessionManager, rpAWS.getProviderId())).thenReturn(Maybe.empty());

        resourceProviderService.findOne(rpAWS.getProviderId(),
            testContext.failing(throwable -> testContext.verify(() -> {
                assertThat(throwable).isInstanceOf(NotFoundException.class);
                testContext.completeNow();
            })));
    }

    @Test
    void findAll(VertxTestContext testContext) {
        SessionMockHelper.mockSingle(smProvider, sessionManager);
        when(repository.findAllAndFetch(sessionManager)).thenReturn(Single.just(List.of(rpAWS, rpEdge)));

        resourceProviderService.findAll(testContext.succeeding(result -> testContext.verify(() -> {
            assertThat(result.size()).isEqualTo(2);
            assertThat(result.getJsonObject(0).getLong("provider_id")).isEqualTo(1L);
            assertThat(result.getJsonObject(1).getLong("provider_id")).isEqualTo(2L);
            testContext.completeNow();
        })));
    }
}
