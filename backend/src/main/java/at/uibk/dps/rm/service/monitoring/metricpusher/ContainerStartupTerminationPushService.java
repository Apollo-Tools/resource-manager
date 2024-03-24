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
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.client.WebClient;

/**
 * The interface of the service proxy for pushing container metric data to the monitoring system.
 *
 * @author matthi-g
 */
@ProxyGen
@VertxGen
public interface ContainerStartupTerminationPushService extends ServiceInterface {

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    @GenIgnore
    static ContainerStartupTerminationPushService create(WebClient webClient, ConfigDTO config) {
        return new ContainerStartupTerminationPushServiceImpl(webClient, config);
    }

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    static ContainerStartupTerminationPushService createProxy(Vertx vertx) {
        return new ContainerStartupTerminationPushServiceVertxEBProxy(vertx,
            ServiceProxyAddress.getServiceProxyAddress("container-startup-pusher"));
    }

    /**
     * Compose an OpenTSDB metric and push it to the monitoring system.
     *
     * @param executionTime the start-up time and service deployments
     * @param resultHandler receives nothing if the saving was successful else an error
     */
    void composeAndPushMetric(JsonObject executionTime, Handler<AsyncResult<Void>> resultHandler);
}
