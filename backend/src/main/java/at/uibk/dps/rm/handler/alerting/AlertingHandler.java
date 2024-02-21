package at.uibk.dps.rm.handler.alerting;

import at.uibk.dps.rm.entity.alerting.AlertMessage;
import at.uibk.dps.rm.entity.alerting.AlertType;
import at.uibk.dps.rm.entity.dto.config.ConfigDTO;
import at.uibk.dps.rm.entity.dto.deployment.DeploymentAlertingDTO;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import at.uibk.dps.rm.util.validation.SLOValidator;
import io.reactivex.rxjava3.core.Observable;
import io.vertx.core.Handler;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.web.client.HttpResponse;
import io.vertx.rxjava3.ext.web.client.WebClient;
import lombok.RequiredArgsConstructor;

import java.net.HttpURLConnection;
import java.util.*;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class AlertingHandler {

    private static final Logger logger = LoggerFactory.getLogger(AlertingHandler.class);

    private final Vertx vertx;

    private final WebClient webClient;

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
            .flatMapCompletable(deployment -> {
                logger.info("Validate resources of deployment: " + deployment.getDeploymentId());
                SLOValidator sloValidator = new SLOValidator(serviceProxyProvider.getMetricQueryService(), configDTO,
                    deployment.getResources());
                return sloValidator.validateResourcesByMonitoredMetrics(deployment)
                    .flatMapObservable(Observable::fromIterable)
                    .flatMap(resource -> Observable.fromIterable(resource.getMonitoredMetricValues())
                        .map(monitoredMetricValue ->
                            new AlertMessage(AlertType.SLO_BREACH, resource.getResourceId(), monitoredMetricValue)
                        )
                    )
                    .map(alertMessage -> {
                        JsonObject requestBody = JsonObject.mapFrom(alertMessage);
                        logger.info(requestBody.encode());
                        return webClient.postAbs(deployment.getAlertingUrl()).sendJsonObject(requestBody)
                            .map(HttpResponse::statusCode)
                            .onErrorReturn(throwable -> {
                                // Misdirected Request
                                return 421;
                            })
                            .map(code -> {
                                if (code != HttpURLConnection.HTTP_NO_CONTENT) {
                                    logger.info("Failed to notify client status code: " + code);
                                }
                                return code;
                            });
                    })
                    .ignoreElements();
            })
            .subscribe(() -> {
                logger.info("Finished: validation of deployment");
                currentTimer = pauseLoop ? currentTimer : vertx.setTimer((long) minPeriod, validationHandler);
            }, throwable -> {
                logger.error(throwable.getMessage());
                currentTimer = pauseLoop ? currentTimer : vertx.setTimer((long) minPeriod, validationHandler);
            });
        validationHandler.handle(-1L);
    }

    public void pauseAlertingLoop() {
        pauseLoop = true;
        if (!vertx.cancelTimer(currentTimer)) {
            vertx.cancelTimer(currentTimer);
        }
        currentTimer = -1L;
    }
}
