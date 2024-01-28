package at.uibk.dps.rm.service.monitoring.metricpusher;

import at.uibk.dps.rm.entity.dto.config.ConfigDTO;
import at.uibk.dps.rm.entity.monitoring.OpenFaasConnectivity;
import at.uibk.dps.rm.entity.monitoring.OpenTSDBEntity;
import at.uibk.dps.rm.service.ServiceProxy;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This is the implementation of the {@link FunctionInvocationPushService}.
 *
 * @author matthi-g
 */
public class OpenFaasMetricPushServiceImpl extends ServiceProxy implements OpenFaasMetricPushService {

    private final MetricPusher monitoringPusher;

    public OpenFaasMetricPushServiceImpl(WebClient webClient, ConfigDTO config) {
        monitoringPusher = new MetricPusher(webClient, config);
    }

    @Override
    public String getServiceProxyAddress() {
        return "openfaas-metric-pusher" + super.getServiceProxyAddress();
    }

    public void composeAndPushMetrics(JsonArray connectivities, Handler<AsyncResult<Void>> resultHandler) {
        OpenFaasConnectivity[] connectivityList = connectivities.stream()
            .map(value -> ((JsonObject) value).mapTo(OpenFaasConnectivity.class))
            .toArray(OpenFaasConnectivity[]::new);
        List<OpenTSDBEntity> metrics = Arrays.stream(connectivityList)
            .map(connectivity -> new OpenTSDBEntity("openfaas_latency_seconds", connectivity.getLatencySeconds(),
                Map.of("resource", Long.toString(connectivity.getResourceId()))))
            .collect(Collectors.toList());

        monitoringPusher.pushMetrics(metrics, resultHandler);
    }
}
