package at.uibk.dps.rm.handler.monitoring;


import at.uibk.dps.rm.entity.dto.config.ConfigDTO;
import at.uibk.dps.rm.handler.ensemble.EnsembleHandler;
import io.vertx.core.Handler;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.rxjava3.core.Vertx;
import lombok.RequiredArgsConstructor;

/**
 * The {@link MonitoringHandler} for the validation of ensembles.
 *
 * @author matthi-g
 */
@RequiredArgsConstructor
public class EnsembleValidationHandler implements MonitoringHandler{

    private static final Logger logger = LoggerFactory.getLogger(EnsembleValidationHandler.class);

    private final Vertx vertx;

    private final ConfigDTO configDTO;

    private final EnsembleHandler ensembleHandler;

    private long currentTimer = -1L;

    @Override
    public void startMonitoringLoop() {
        long period = (long) (configDTO.getEnsembleValidationPeriod() * 60 * 1000);
        Handler<Long> monitoringHandler = id -> {
            logger.info("Started: validation of ensembles");
            ensembleHandler.validateAllExistingEnsembles()
                .subscribe(() -> logger.info("Finished: validation of ensembles"),
                    throwable -> logger.error(throwable.getMessage()));
        };
        currentTimer = vertx.setPeriodic(0L, period, monitoringHandler);
    }

    @Override
    public void pauseMonitoringLoop() {
        vertx.cancelTimer(currentTimer);
        currentTimer = -1L;
    }
}
