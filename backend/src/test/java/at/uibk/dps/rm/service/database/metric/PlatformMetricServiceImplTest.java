package at.uibk.dps.rm.service.database.metric;

import at.uibk.dps.rm.repository.metric.PlatformMetricRepository;
import at.uibk.dps.rm.testutil.SessionMockHelper;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.hibernate.reactive.stage.Stage;
import org.hibernate.reactive.util.impl.CompletionStages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    private Stage.SessionFactory sessionFactory;

    @Mock
    private Stage.Session session;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        service = new PlatformMetricServiceImpl(repository, sessionFactory);
    }

    @ParameterizedTest
    @CsvSource({
        "3, true",
        "0, false"
    })
    void missingRequiredResourceTypeMetricsByResourceId(long missingRtms, boolean expectedResult,
                                                        VertxTestContext testContext) {
        long resourceId = 1L;

        SessionMockHelper.mockSession(sessionFactory, session);
        when(repository.countMissingRequiredMetricValuesByResourceId(session, resourceId))
            .thenReturn(CompletionStages.completedFuture(missingRtms));

        service.missingRequiredPlatformMetricsByResourceId(resourceId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isEqualTo(expectedResult);
                testContext.completeNow();
            })));
    }
}
