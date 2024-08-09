package at.uibk.dps.rm.handler.monitoring;

import at.uibk.dps.rm.entity.dto.config.ConfigDTO;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.handler.ensemble.EnsembleHandler;
import at.uibk.dps.rm.testutil.objectprovider.TestConfigProvider;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.reactivex.rxjava3.core.Completable;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.core.Vertx;
import nl.altindag.log.LogCaptor;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Implements tests for the {@link EnsembleValidationHandlerTest} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class EnsembleValidationHandlerTest {
    private EnsembleValidationHandler validationHandler;

    private Vertx spyVertx;

    @Mock
    private EnsembleHandler ensembleHandler;

    private static LogCaptor logCaptor;

    @BeforeAll
    static void initAll() {
        logCaptor = LogCaptor.forClass(EnsembleValidationHandler.class);
    }

    @AfterAll
    static void cleanupAll() {
        logCaptor.close();
    }

    @BeforeEach
    void initTest(Vertx vertx) {
        JsonMapperConfig.configJsonMapper();
        ConfigDTO configDTO = TestConfigProvider.getConfigDTO();
        spyVertx = spy(vertx);
        validationHandler = new EnsembleValidationHandler(spyVertx, configDTO, ensembleHandler);
    }

    @AfterEach
    void cleanupEach() {
        logCaptor.clearLogs();
    }

    @Test
    public void startMonitoringLoop(VertxTestContext testContext) throws InterruptedException {
        when(ensembleHandler.validateAllExistingEnsembles()).thenReturn(Completable.complete());

        validationHandler.startMonitoringLoop();
        testContext.awaitCompletion(1, TimeUnit.SECONDS);
        verify(spyVertx).setPeriodic(eq(0L), eq(3600000L), any());
        assertThat(logCaptor.getInfoLogs()).isEqualTo(List.of("Started: validation of ensembles",
            "Finished: validation of ensembles"));
        testContext.completeNow();
    }

    @Test
    public void startMonitoringLoopException(VertxTestContext testContext) throws InterruptedException {
        when(ensembleHandler.validateAllExistingEnsembles()).thenReturn(Completable.error(NotFoundException::new));

        validationHandler.startMonitoringLoop();
        testContext.awaitCompletion(1, TimeUnit.SECONDS);
        verify(spyVertx).setPeriodic(eq(0L), eq(3600000L), any());
        assertThat(logCaptor.getInfoLogs()).isEqualTo(List.of("Started: validation of ensembles"));
        assertThat(logCaptor.getErrorLogs()).isEqualTo(List.of("not found"));
        testContext.completeNow();
    }


    @Test
    public void pauseMonitoringLoop() {
        validationHandler.pauseMonitoringLoop();
        validationHandler.pauseMonitoringLoop();

        verify(spyVertx, times(2)).cancelTimer(-1L);
    }
}
