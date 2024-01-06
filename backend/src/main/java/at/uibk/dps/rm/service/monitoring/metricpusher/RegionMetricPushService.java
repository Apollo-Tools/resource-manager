package at.uibk.dps.rm.service.monitoring.metricpusher;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.dto.config.ConfigDTO;
import at.uibk.dps.rm.service.ServiceInterface;
import at.uibk.dps.rm.service.ServiceProxyAddress;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.rxjava3.ext.web.client.WebClient;

/**
 * The interface of the service proxy for pushing region metric data to the monitoring system.
 *
 * @author matthi-g
 */
@ProxyGen
@VertxGen
public interface RegionMetricPushService extends ServiceInterface {

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    @GenIgnore
    static RegionMetricPushService create(WebClient webClient, ConfigDTO config) {
        return new RegionMetricPushServiceImpl(webClient, config);
    }

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    static RegionMetricPushService createProxy(Vertx vertx) {
        return new RegionMetricPushServiceVertxEBProxy(vertx, ServiceProxyAddress.getServiceProxyAddress(
            "region-metric-pusher"));
    }

    /**
     * Compose an OpenTSDB metric and push it to the monitoring system.
     *
     * @param regionConnectivity the connectivity to registered regions
     * @param resultHandler receives nothing if the saving was successful else an error
     */
    void composeAndPushMetrics(JsonArray regionConnectivity, Handler<AsyncResult<Void>> resultHandler);
}
