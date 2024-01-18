package at.uibk.dps.rm.service.monitoring.metricquery;

import at.uibk.dps.rm.entity.dto.config.ConfigDTO;
import at.uibk.dps.rm.entity.monitoring.victoriametrics.VmQueryResult;
import at.uibk.dps.rm.entity.monitoring.victoriametrics.VmResult;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.service.ServiceProxy;
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
public class MetricQueryServiceImpl extends ServiceProxy implements MetricQueryService {

    private static final Logger logger = LoggerFactory.getLogger(MetricQueryServiceImpl.class);

    private final WebClient webClient;

    private final ConfigDTO config;

    @Override
    public String getServiceProxyAddress() {
        return "metric-collector" + super.getServiceProxyAddress();
    }

    public void collectInstantMetric(String query, Handler<AsyncResult<List<VmResult>>> resultHandler) {
        // TODO: remove next line / change to debug
        logger.info(query);
        Single<List<VmResult>> collectMetrics = webClient.getAbs(config.getMonitoringQueryUrl() +
                "/api/v1/query?query=" + query.replace("+", "%2b"))
            .send()
            .flatMap(httpResponse -> {
                if (httpResponse.statusCode() != 200) {
                    logger.error("failed to collect metrics:" + httpResponse.bodyAsString());
                    return Single.error(new NotFoundException("empty query: " + query));
                } else {
                    VmQueryResult result = httpResponse.bodyAsJsonObject().mapTo(VmQueryResult.class);
                    if (result.getData().getResult().isEmpty()) {
                        logger.info("empty query: " + query);
                    }
                    return Single.just(result.getData().getResult());
                }
            })
            .onErrorResumeNext(throwable -> {
                logger.error("failed to collect metrics:" + throwable.getMessage());
                return Single.error(throwable);
            });
        RxVertxHandler.handleSession(collectMetrics, resultHandler);
    }
}
