package at.uibk.dps.rm.service.monitoring.metricpusher;

import at.uibk.dps.rm.entity.dto.config.ConfigDTO;
import at.uibk.dps.rm.entity.monitoring.opentsdb.OpenTSDBEntity;
import at.uibk.dps.rm.service.ServiceProxy;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.rxjava3.ext.web.client.WebClient;

import java.util.List;
import java.util.Map;

/**
 * This is the implementation of the {@link ContainerStartupTerminationPushService}.
 *
 * @author matthi-g
 */
public class ContainerStartupTerminationPushServiceImpl extends ServiceProxy implements
        ContainerStartupTerminationPushService {

    private final MetricPusher monitoringPusher;

    public ContainerStartupTerminationPushServiceImpl(WebClient webClient, ConfigDTO config) {
        monitoringPusher = new MetricPusher(webClient, config);
    }

    @Override
    public String getServiceProxyAddress() {
        return "container-startup-pusher" + super.getServiceProxyAddress();
    }

    public void composeAndPushMetric(double startupTime, long resourceDeploymentId, long resourceId,
            long serviceId, boolean isStartup, Handler<AsyncResult<Void>> resultHandler) {
        String metricName = "container_" + (isStartup ? "startup" : "termination" ) + "_time_seconds";
        OpenTSDBEntity metric = new OpenTSDBEntity(metricName, startupTime,
            Map.of("resource_deployment", Long.toString(resourceDeploymentId), "serviceId",
                Long.toString(serviceId), "resource", Long.toString(resourceId)));

        monitoringPusher.pushMetrics(List.of(metric), resultHandler);
    }
}
