package at.uibk.dps.rm.service.monitoring.metricpusher;

import at.uibk.dps.rm.entity.dto.config.ConfigDTO;
import at.uibk.dps.rm.entity.monitoring.OpenTSDBEntity;
import at.uibk.dps.rm.entity.monitoring.ServiceStartupTerminateTime;
import at.uibk.dps.rm.service.ServiceProxy;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.client.WebClient;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This is the implementation of the {@link ServiceStartupStopPushService}.
 *
 * @author matthi-g
 */
public class ServiceStartupStopPushServiceImpl extends ServiceProxy implements
    ServiceStartupStopPushService {

    private final MetricPusher monitoringPusher;

    /**
     * Create a new instance from a webClient and config.
     *
     * @param webClient the web client
     * @param config the config
     */
    public ServiceStartupStopPushServiceImpl(WebClient webClient, ConfigDTO config) {
        monitoringPusher = new MetricPusher(webClient, config);
    }

    @Override
    public String getServiceProxyAddress() {
        return "service-startup-stop-pusher" + super.getServiceProxyAddress();
    }

    @Override
    public void composeAndPushMetric(JsonObject executionTime, Handler<AsyncResult<Void>> resultHandler) {
        ServiceStartupTerminateTime serviceExecutionTime = executionTime.mapTo(ServiceStartupTerminateTime.class);
        String metricName = "service_" + (serviceExecutionTime.getIsStartup() ? "startup" : "termination" ) +
            "_time_seconds";
        List<OpenTSDBEntity> openTSDBEntities = serviceExecutionTime.getServiceDeployments().stream()
            .map(serviceDeployment -> new OpenTSDBEntity(metricName, serviceExecutionTime.getExecutionTime(),
                Map.of("request_id", serviceExecutionTime.getId(),
                    "resource_deployment", Long.toString(serviceDeployment.getResourceDeploymentId()),
                    "serviceId", Long.toString(serviceDeployment.getService().getServiceId()),
                    "resource", Long.toString(serviceDeployment.getResource().getResourceId()))))
            .collect(Collectors.toList());

        monitoringPusher.pushMetrics(openTSDBEntities, resultHandler);
    }
}
