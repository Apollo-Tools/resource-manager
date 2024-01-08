package at.uibk.dps.rm.service.monitoring.metricpusher;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.dto.config.ConfigDTO;
import at.uibk.dps.rm.entity.monitoring.K8sMonitoringData;
import at.uibk.dps.rm.service.ServiceInterface;
import at.uibk.dps.rm.service.ServiceProxyAddress;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.rxjava3.ext.web.client.WebClient;

/**
 * The interface of the service proxy for pushing region metric data to the monitoring system.
 *
 * @author matthi-g
 */
@ProxyGen
@VertxGen
public interface K8sMetricPushService extends ServiceInterface {

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    @GenIgnore
    static K8sMetricPushService create(WebClient webClient, ConfigDTO config) {
        return new K8sMetricPushServiceImpl(webClient, config);
    }

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    static K8sMetricPushService createProxy(Vertx vertx) {
        return new K8sMetricPushServiceVertxEBProxy(vertx, ServiceProxyAddress.getServiceProxyAddress(
            "k8s-metric-pusher"));
    }

    /**
     * Compose OpenTSDB metrics and push them to the monitoring system.
     *
     * @param data the k8s monitoring data
     * @param resultHandler receives nothing if the saving was successful else an error
     */
    void composeAndPushMetrics(K8sMonitoringData data, Handler<AsyncResult<Void>> resultHandler);
}
