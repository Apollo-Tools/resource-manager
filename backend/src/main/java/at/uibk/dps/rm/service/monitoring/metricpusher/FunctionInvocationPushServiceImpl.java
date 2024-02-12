package at.uibk.dps.rm.service.monitoring.metricpusher;

import at.uibk.dps.rm.entity.dto.config.ConfigDTO;
import at.uibk.dps.rm.entity.monitoring.OpenTSDBEntity;
import at.uibk.dps.rm.service.ServiceProxy;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.rxjava3.ext.web.client.WebClient;

import java.util.List;
import java.util.Map;

/**
 * This is the implementation of the {@link FunctionInvocationPushService}.
 *
 * @author matthi-g
 */
public class FunctionInvocationPushServiceImpl extends ServiceProxy implements FunctionInvocationPushService {

    private final MetricPusher monitoringPusher;

    /**
     * Create a new instance from a webClient and config.
     *
     * @param webClient the web client
     * @param config the config
     */
    public FunctionInvocationPushServiceImpl(WebClient webClient, ConfigDTO config) {
        monitoringPusher = new MetricPusher(webClient, config);
    }

    @Override
    public String getServiceProxyAddress() {
        return "function-invocation-pusher" + super.getServiceProxyAddress();
    }

    @Override
    public void composeAndPushMetric(double execTime, long deploymentId, long resourceDeploymentId, long functionId,
            long resourceId, String requestBody, Handler<AsyncResult<Void>> resultHandler) {
        OpenTSDBEntity metric = new OpenTSDBEntity("function_execution_duration_seconds", execTime,
            Map.of("deployment", Long.toString(deploymentId), "resource_deployment",
                Long.toString(resourceDeploymentId), "resource", Long.toString(resourceId),
                "function", Long.toString(functionId), "requestBody", requestBody));
        monitoringPusher.pushMetrics(List.of(metric), resultHandler);
    }
}
