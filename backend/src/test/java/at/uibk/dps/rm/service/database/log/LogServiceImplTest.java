package at.uibk.dps.rm.service.database.log;

import at.uibk.dps.rm.entity.model.Log;
import at.uibk.dps.rm.repository.log.LogRepository;
import at.uibk.dps.rm.service.database.util.SessionManagerProvider;
import at.uibk.dps.rm.testutil.SessionMockHelper;
import at.uibk.dps.rm.testutil.objectprovider.TestLogProvider;
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
 * Implements tests for the {@link LogServiceImpl} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class LogServiceImplTest {

    private LogService logService;

    @Mock
    private LogRepository logRepository;

    @Mock
    private SessionManagerProvider smProvider;

    @Mock
    private SessionManager sessionManager;


    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        logService = new LogServiceImpl(logRepository, smProvider);
    }

    @Test
    void findAllByDeploymentIdAndAccountId(VertxTestContext testContext) {
        long deploymentId = 1L, accountId = 2L;
        Log log1 = TestLogProvider.createLog(1L);
        Log log2 = TestLogProvider.createLog(2L);

        SessionMockHelper.mockSingle(smProvider, sessionManager);
        when(logRepository.findAllByDeploymentIdAndAccountId(sessionManager, deploymentId, accountId))
            .thenReturn(Single.just(List.of(log1, log2)));

        logService.findAllByDeploymentIdAndAccountId(deploymentId, accountId,
            testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(2);
                assertThat(result.getJsonObject(0).getLong("log_id")).isEqualTo(1L);
                assertThat(result.getJsonObject(1).getLong("log_id")).isEqualTo(2L);
                testContext.completeNow();
            })));
    }
}
