package at.uibk.dps.rm.service.database.log;

import at.uibk.dps.rm.entity.model.Log;
import at.uibk.dps.rm.repository.log.LogRepository;
import at.uibk.dps.rm.testutil.objectprovider.TestLogProvider;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.hibernate.reactive.util.impl.CompletionStages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;

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

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        logService = new LogServiceImpl(logRepository);
    }

    @Test
    void findAllByReservationIdAndAccountId(VertxTestContext testContext) {
        long reservationId = 1L, accountId = 2L;
        Log log1 = TestLogProvider.createLog(1L);
        Log log2 = TestLogProvider.createLog(2L);
        List<Log> logs = List.of(log1, log2);
        CompletionStage<List<Log>> completionStage = CompletionStages.completedFuture(logs);

        when(logRepository.findAllByReservationIdAndAccountId(reservationId, accountId)).thenReturn(completionStage);

        logService.findAllByReservationIdAndAccountId(reservationId, accountId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(2);
                assertThat(result.getJsonObject(0).getLong("log_id")).isEqualTo(1L);
                assertThat(result.getJsonObject(1).getLong("log_id")).isEqualTo(2L);
                testContext.completeNow();
            })));
    }

    @Test
    void findAllByReservationIdAndAccountIdEmpty(VertxTestContext testContext) {
        long reservationId = 1L, accountId = 2L;
        List<Log> logs = new ArrayList<>();
        CompletionStage<List<Log>> completionStage = CompletionStages.completedFuture(logs);

        when(logRepository.findAllByReservationIdAndAccountId(reservationId, accountId)).thenReturn(completionStage);

        logService.findAllByReservationIdAndAccountId(reservationId, accountId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(0);
                testContext.completeNow();
            })));
    }

}
