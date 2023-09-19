package at.uibk.dps.rm.service.database.metric;

import at.uibk.dps.rm.entity.model.PlatformMetric;
import at.uibk.dps.rm.repository.metric.PlatformMetricRepository;
import at.uibk.dps.rm.service.database.util.SessionManagerProvider;
import at.uibk.dps.rm.testutil.SessionMockHelper;
import at.uibk.dps.rm.testutil.objectprovider.TestMetricProvider;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.reactivex.rxjava3.core.Single;
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
import static org.mockito.Mockito.when;

/**
 * Implements tests for the {@link PlatformMetricServiceImpl} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class PlatformMetricServiceImplTest {

    private PlatformMetricService service;

    @Mock
    private PlatformMetricRepository repository;

    @Mock
    private SessionManagerProvider smProvider;

    @Mock
    private SessionManager sessionManager;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        service = new PlatformMetricServiceImpl(repository, smProvider);
    }

    @Test
    void findAllByPlatformId(VertxTestContext testContext) {
        long platformId = 1L;
        PlatformMetric pm1 = TestMetricProvider.createPlatformMetric(1L, 10L);
        PlatformMetric pm2 = TestMetricProvider.createPlatformMetric(2L, 11L);
        PlatformMetric pm3 = TestMetricProvider.createPlatformMetric(3L, 12L);

        SessionMockHelper.mockSingle(smProvider, sessionManager);
        when(repository.findAllByPlatform(sessionManager, platformId)).thenReturn(Single.just(List.of(pm1, pm2, pm3)));

        service.findAllByPlatformId(platformId, testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(3);
                assertThat(result.getJsonObject(0).getLong("platform_metric_id")).isEqualTo(1L);
                assertThat(result.getJsonObject(1).getLong("platform_metric_id")).isEqualTo(2L);
                assertThat(result.getJsonObject(2).getLong("platform_metric_id")).isEqualTo(3L);
                testContext.completeNow();
            })));
    }
}
