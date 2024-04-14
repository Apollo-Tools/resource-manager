package at.uibk.dps.rm.handler.monitoring;


import at.uibk.dps.rm.entity.dto.config.ConfigDTO;
import at.uibk.dps.rm.handler.ensemble.EnsembleHandler;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import io.vertx.core.Handler;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.rxjava3.core.Vertx;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class EnsembleValidationHandler implements MonitoringHandler{

    private static final Logger logger = LoggerFactory.getLogger(EnsembleValidationHandler.class);

    private final Vertx vertx;

    private final ConfigDTO configDTO;

    private final ServiceProxyProvider serviceProxyProvider;

    private long currentTimer = -1L;

    @Override
    public void startMonitoringLoop() {
        long period = (long) (configDTO.getEnsembleValidationPeriod() * 60 * 1000);
        EnsembleHandler ensembleHandler = new EnsembleHandler(serviceProxyProvider.getEnsembleService(),
            serviceProxyProvider.getResourceService(), serviceProxyProvider.getMetricService(),
            serviceProxyProvider.getMetricQueryService());
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
