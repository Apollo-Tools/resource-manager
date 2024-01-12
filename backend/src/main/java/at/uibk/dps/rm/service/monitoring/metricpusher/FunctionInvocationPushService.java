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
 * The interface of the service proxy for pushing function invocation metric data to the monitoring
 * system.
 *
 * @author matthi-g
 */
@ProxyGen
@VertxGen
public interface FunctionInvocationPushService extends ServiceInterface {

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    @GenIgnore
    static FunctionInvocationPushService create(WebClient webClient, ConfigDTO config) {
        return new FunctionInvocationPushServiceImpl(webClient, config);
    }

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    static FunctionInvocationPushService createProxy(Vertx vertx) {
        return new FunctionInvocationPushServiceVertxEBProxy(vertx, ServiceProxyAddress.getServiceProxyAddress(
            "function-invocation-pusher"));
    }

    /**
     * Compose an OpenTSDB metric and push it to the monitoring system.
     *
     * @param execTime the execution time
     * @param resourceDeploymentId the resource deployment id
     * @param functionId the function id
     * @param resourceId the resource id
     * @param requestBody the request body
     * @param resultHandler receives nothing if the saving was successful else an error
     */
    void composeAndPushMetric(double execTime, long deploymentId, long resourceDeploymentId, long functionId,
        long resourceId, String requestBody, Handler<AsyncResult<Void>> resultHandler);
}
