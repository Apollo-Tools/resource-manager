package at.uibk.dps.rm.handler.alerting;

import at.uibk.dps.rm.entity.dto.config.ConfigDTO;
import at.uibk.dps.rm.entity.dto.deployment.DeploymentAlertingDTO;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import io.reactivex.rxjava3.core.Observable;
import io.vertx.core.Handler;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.core.Vertx;
import lombok.RequiredArgsConstructor;

import java.util.Comparator;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class AlertingHandler {

    private static final Logger logger = LoggerFactory.getLogger(AlertingHandler.class);

    private final Vertx vertx;

    private final ConfigDTO configDTO;

    private long currentTimer = -1L;

    private boolean pauseLoop = false;

    private static Handler<Long> validationHandler;

    public void startValidationLoop() {
        pauseLoop = false;
        double minPeriod = Stream.of(configDTO.getAwsPriceMonitoringPeriod(), configDTO.getKubeMonitoringPeriod(),
                configDTO.getOpenfaasMonitoringPeriod(), configDTO.getRegionMonitoringPeriod(),
                configDTO.getRegionMonitoringPeriod())
            .min(Comparator.naturalOrder())
            .get() * 60 * 1000;
        ServiceProxyProvider serviceProxyProvider = new ServiceProxyProvider(vertx);
        validationHandler = id -> serviceProxyProvider.getDeploymentService().findAllActiveWithAlerting()
            .flatMapObservable(Observable::fromIterable)
            .map(deploymentAlerting -> ((JsonObject) deploymentAlerting).mapTo(DeploymentAlertingDTO.class))
            .map(deployment -> {
                logger.info("Validate resources of deployment: " + deployment.getDeploymentId());
                return deployment;
            })
            .toList()
            .map(connectivities -> {
                // TODO: alert in case of breach
                return connectivities;
            })
            .subscribe(res -> {
                logger.info("Finished: validation of deployment");
                currentTimer = pauseLoop ? currentTimer : vertx.setTimer((long) minPeriod, validationHandler);
            }, throwable -> {
                logger.error(throwable.getMessage());
                currentTimer = pauseLoop ? currentTimer : vertx.setTimer((long) minPeriod, validationHandler);
            });
        validationHandler.handle(-1L);
        //currentTimer = vertx.setTimer(0, validationHandler);
    }

    public void pauseAlertingLoop() {
        pauseLoop = true;
        if (!vertx.cancelTimer(currentTimer)) {
            vertx.cancelTimer(currentTimer);
        }
        currentTimer = -1L;
    }
}
