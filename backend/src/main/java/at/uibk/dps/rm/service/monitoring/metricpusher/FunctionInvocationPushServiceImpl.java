package at.uibk.dps.rm.service.monitoring.metricpusher;

import at.uibk.dps.rm.entity.dto.config.ConfigDTO;
import at.uibk.dps.rm.entity.monitoring.opentsdb.OpenTSDBEntity;
import at.uibk.dps.rm.service.ServiceProxy;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.rxjava3.ext.web.client.WebClient;

import java.util.List;
import java.util.Map;

public class FunctionInvocationPushServiceImpl extends ServiceProxy implements FunctionInvocationPushService {

    private final MetricPusher monitoringPusher;

    public FunctionInvocationPushServiceImpl(WebClient webClient, ConfigDTO config) {
        monitoringPusher = new MetricPusher(webClient, config);
    }

    @Override
    public String getServiceProxyAddress() {
        return "function-invocation-pusher" + super.getServiceProxyAddress();
    }

    public void composeAndPushMetric(double execTime, long resourceDeploymentId, long functionId, long resourceId,
            String requestBody, Handler<AsyncResult<Void>> resultHandler) {
        OpenTSDBEntity metric = new OpenTSDBEntity("function_execution_duration_seconds", execTime,
            Map.of("resource_deployment", Long.toString(resourceDeploymentId), "resource", Long.toString(resourceId),
                "function", Long.toString(functionId), "requestBody", requestBody));
        monitoringPusher.pushMetrics(List.of(metric), resultHandler);
    }
}
