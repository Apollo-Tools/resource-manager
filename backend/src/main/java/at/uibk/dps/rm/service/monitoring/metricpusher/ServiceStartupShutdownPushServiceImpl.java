package at.uibk.dps.rm.service.monitoring.metricpusher;

import at.uibk.dps.rm.entity.dto.config.ConfigDTO;
import at.uibk.dps.rm.entity.monitoring.OpenTSDBEntity;
import at.uibk.dps.rm.entity.monitoring.ServiceStartupShutdownTime;
import at.uibk.dps.rm.service.ServiceProxy;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.client.WebClient;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This is the implementation of the {@link ServiceStartupShutdownPushService}.
 *
 * @author matthi-g
 */
public class ServiceStartupShutdownPushServiceImpl extends ServiceProxy implements
    ServiceStartupShutdownPushService {

    private final MetricPusher monitoringPusher;

    /**
     * Create a new instance from a webClient and config.
     *
     * @param webClient the web client
     * @param config the config
     */
    public ServiceStartupShutdownPushServiceImpl(WebClient webClient, ConfigDTO config) {
        monitoringPusher = new MetricPusher(webClient, config);
    }

    @Override
    public String getServiceProxyAddress() {
        return "service-startup-stop-pusher" + super.getServiceProxyAddress();
    }

    @Override
    public void composeAndPushMetric(JsonObject serviceStartupShutdownTime, Handler<AsyncResult<Void>> resultHandler) {
        ServiceStartupShutdownTime serviceExecutionTime = serviceStartupShutdownTime
            .mapTo(ServiceStartupShutdownTime.class);
        String metricName = "service_" + (serviceExecutionTime.getIsStartup() ? "startup" : "shutdown" ) +
            "_time_seconds";
        List<OpenTSDBEntity> openTSDBEntities = serviceExecutionTime.getServiceDeployments().stream()
            .map(serviceDeployment -> new OpenTSDBEntity(metricName, serviceExecutionTime.getExecutionTime(),
                Map.of("request_id", serviceExecutionTime.getId(),
                    "deployment", Long.toString(serviceExecutionTime.getDeploymentId()),
                    "resource_deployment", Long.toString(serviceDeployment.getResourceDeploymentId()),
                    "service", Long.toString(serviceDeployment.getService().getServiceId()),
                    "resource", Long.toString(serviceDeployment.getResource().getResourceId()))))
            .collect(Collectors.toList());

        monitoringPusher.pushMetrics(openTSDBEntities, resultHandler);
    }
}
