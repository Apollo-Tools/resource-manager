package at.uibk.dps.rm.service.monitoring.metriccollector;

import at.uibk.dps.rm.entity.dto.config.ConfigDTO;
import at.uibk.dps.rm.entity.monitoring.victoriametrics.VmQueryResult;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.util.misc.RxVertxHandler;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.rxjava3.ext.web.client.WebClient;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * This class can be used to push metrics to the configured monitoring system.
 *
 * @author matthi-g
 */
@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public class MetricCollector {

    private static final Logger logger = LoggerFactory.getLogger(MetricCollector.class);

    private final WebClient webClient;

    private final ConfigDTO config;

    public Single<Double> collectInstantMetric(String query, Handler<AsyncResult<Double>> resultHandler) {
        Single<Double> pushMetrics = webClient.getAbs(config.getMonitoringQueryUrl() +
                "/api/v1/query?query=" + query)
            .send()
            .flatMap(httpResponse -> {
                if (httpResponse.statusCode() != 200) {
                    logger.error("failed to collect metrics:" + httpResponse.bodyAsString());
                    return Single.error(new NotFoundException("metric not found: " + query));
                } else {
                    VmQueryResult result = httpResponse.bodyAsJsonObject().mapTo(VmQueryResult.class);
                    if (result.getData().getResult().isEmpty()) {
                        return Single.error(new NotFoundException("metric not found: " + query));
                    }
                    return Single.just(result.getData().getResult().get(0).getValues().get(0).getValue());
                }
            })
            .onErrorResumeNext(throwable -> {
                logger.error("failed to collect metrics:" + throwable.getMessage());
                return Single.error(throwable);
            });
        return pushMetrics;
        //RxVertxHandler.handleSession(pushMetrics, resultHandler);
    }

    protected void collectInstantMetrics(String query, Handler<AsyncResult<List<Double>>> resultHandler) {
        Single<List<Double>> pushMetrics = webClient.getAbs(config.getMonitoringQueryUrl() +
                "/api/v1/query?query=" + query)
            .send()
            .flatMap(httpResponse -> {
                if (httpResponse.statusCode() != 204) {
                    logger.error("failed to retrieve metrics:" + httpResponse.bodyAsString());
                } else {
                    logger.info("retrieved metrics from monitoring storage");
                }
                return Single.just(List.of(4.20));
            })
            .onErrorResumeNext(throwable -> {
                logger.error("failed to retrieve metrics:" + throwable.getMessage());
                return Single.error(NotFoundException::new);
            });
        RxVertxHandler.handleSession(pushMetrics, resultHandler);
    }
}
