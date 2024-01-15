package at.uibk.dps.rm.service.monitoring.metricpusher;

import at.uibk.dps.rm.entity.dto.config.ConfigDTO;
import at.uibk.dps.rm.entity.monitoring.OpenTSDBEntity;
import at.uibk.dps.rm.util.misc.RxVertxHandler;
import io.reactivex.rxjava3.core.Completable;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.Json;
import io.vertx.rxjava3.core.buffer.Buffer;
import io.vertx.rxjava3.ext.web.client.WebClient;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * This class can be used to push metrics to the configured monitoring system.
 *
 * @author matthi-g
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class MetricPusher {

    private static final Logger logger = LoggerFactory.getLogger(MetricPusher.class);

    private final WebClient webClient;

    private final ConfigDTO config;

    protected void pushMetrics(List<OpenTSDBEntity> metrics, Handler<AsyncResult<Void>> resultHandler) {
        String requestBody = Json.encode(metrics);
        Completable pushMetrics = webClient.postAbs(config.getMonitoringPushUrl() + "/api/put")
            .sendBuffer(Buffer.buffer(requestBody))
            .flatMapCompletable(httpResponse -> {
                if (httpResponse.statusCode() != 204) {
                    logger.error("failed to push metrics:" + httpResponse.bodyAsString());
                } else {
                    logger.info("pushed metrics to monitoring storage");
                }
                return Completable.complete();
            })
            .onErrorResumeNext(throwable -> {
                logger.error("failed to push metrics:" + throwable.getMessage());
                return Completable.complete();
            });
        RxVertxHandler.handleSession(pushMetrics, resultHandler);
    }
}
