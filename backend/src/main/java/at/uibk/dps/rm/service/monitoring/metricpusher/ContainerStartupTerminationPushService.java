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
     * @param startupTime the start-up time
     * @param resourceDeploymentId the resource deployment id
     * @param resourceId the resource id
     * @param serviceId the service id
     * @param isStartup whether the metric is for container startup or termination
     * @param resultHandler receives nothing if the saving was successful else an error
     */
    void composeAndPushMetric(double startupTime, long resourceDeploymentId, long resourceId, long serviceId,
        boolean isStartup, Handler<AsyncResult<Void>> resultHandler);
}
