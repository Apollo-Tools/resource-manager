package at.uibk.dps.rm.handler.alerting;

import at.uibk.dps.rm.entity.alerting.AlertMessage;
import at.uibk.dps.rm.entity.alerting.AlertType;
import at.uibk.dps.rm.entity.dto.config.ConfigDTO;
import at.uibk.dps.rm.entity.dto.deployment.DeploymentAlertingDTO;
import at.uibk.dps.rm.service.rxjava3.database.deployment.DeploymentService;
import at.uibk.dps.rm.service.rxjava3.monitoring.metricquery.MetricQueryService;
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

/**
 * The alerting handler validates active deployments against their specified service level
 * objectives and notifies their clients in case of an SLO violation.
 *
 * @author matthi-g
 */
@RequiredArgsConstructor
public class AlertingHandler {
    private static final Logger logger = LoggerFactory.getLogger(AlertingHandler.class);

    private final Vertx vertx;

    private final WebClient webClient;

    private final ConfigDTO configDTO;

    private final DeploymentService deploymentService;

    private final MetricQueryService metricQueryService;


    private long currentTimer = -1L;

    private boolean pauseLoop = false;

    private static Handler<Long> validationHandler;

    /**
     * Start the validation loop.
     */
    public void startValidationLoop() {
        pauseLoop = false;
        double minPeriod = Stream.of(configDTO.getAwsPriceMonitoringPeriod(), configDTO.getKubeMonitoringPeriod(),
                configDTO.getOpenfaasMonitoringPeriod(), configDTO.getRegionMonitoringPeriod())
            .min(Comparator.naturalOrder())
            .get() * 1000;
        validationHandler = id -> deploymentService.findAllActiveWithAlerting()
            .flatMapObservable(Observable::fromIterable)
            .map(deploymentAlerting -> ((JsonObject) deploymentAlerting).mapTo(DeploymentAlertingDTO.class))
            .flatMap(deployment -> {
                logger.info("Validate resources of deployment: " + deployment.getDeploymentId());
                SLOValidator sloValidator = new SLOValidator(metricQueryService, configDTO, deployment.getResources());
                return sloValidator.validateResourcesByMonitoredMetrics(deployment)
                    .flatMapObservable(Observable::fromIterable)
                    .flatMap(resource -> Observable.fromIterable(resource.getMonitoredMetricValues())
                        .map(monitoredMetricValue ->
                            new AlertMessage(AlertType.SLO_VIOLATION, resource.getResourceId(), monitoredMetricValue)
                        )
                    )
                    .map(alertMessage -> {
                        JsonObject requestBody = JsonObject.mapFrom(alertMessage);
                        logger.info(requestBody.encode());
                        return webClient.postAbs(deployment.getAlertingUrl()).timeout(5000)
                            .sendJsonObject(requestBody)
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
                            })
                            .subscribe();
                    });
            })
            .toList()
            .subscribe(res -> {
                logger.info("Finished: validation of deployments");
                currentTimer = pauseLoop ? currentTimer : vertx.setTimer((long) minPeriod, validationHandler);
            }, throwable -> {
                logger.error(throwable.getMessage());
                currentTimer = pauseLoop ? currentTimer : vertx.setTimer((long) minPeriod, validationHandler);
            });
        validationHandler.handle(-99L);
    }

    /**
     * Pause the validation loop.
     */
    public void pauseValidationLoop() {
        pauseLoop = true;
        vertx.cancelTimer(currentTimer);
        currentTimer = -1L;
    }
}
