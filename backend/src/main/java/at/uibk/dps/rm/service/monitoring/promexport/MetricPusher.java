package at.uibk.dps.rm.service.monitoring.promexport;

import at.uibk.dps.rm.entity.dto.config.ConfigDTO;
import at.uibk.dps.rm.entity.monitoring.opentsdb.OpenTSDBEntity;
import io.reactivex.rxjava3.core.Completable;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.Json;
import io.vertx.rxjava3.core.buffer.Buffer;
import io.vertx.rxjava3.ext.web.client.WebClient;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class MetricPusher {

    private static final Logger logger = LoggerFactory.getLogger(MetricPusher.class);

    private final WebClient webClient;

    private final ConfigDTO config;

    public Completable pushMetrics(List<OpenTSDBEntity> metrics) {
        String requestBody = Json.encode(metrics);
        return webClient.postAbs(config.getMonitoringPushUrl() + "/api/put")
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
    }
}
