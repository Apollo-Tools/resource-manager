package at.uibk.dps.rm.service.monitoring.metricpusher;

import at.uibk.dps.rm.entity.dto.config.ConfigDTO;
import at.uibk.dps.rm.entity.monitoring.kubernetes.K8sMonitoringData;
import at.uibk.dps.rm.entity.monitoring.OpenTSDBEntity;
import at.uibk.dps.rm.service.ServiceProxy;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.rxjava3.ext.web.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This is the implementation of the {@link K8sMetricPushService}.
 *
 * @author matthi-g
 */
public class K8sMetricPushServiceImpl extends ServiceProxy implements K8sMetricPushService {

    private final MetricPusher monitoringPusher;

    public K8sMetricPushServiceImpl(WebClient webClient, ConfigDTO config) {
        monitoringPusher = new MetricPusher(webClient, config);
    }

    @Override
    public String getServiceProxyAddress() {
        return "k8s-metric-pusher" + super.getServiceProxyAddress();
    }

    public void composeAndPushMetrics(K8sMonitoringData data, Handler<AsyncResult<Void>> resultHandler) {
        List<OpenTSDBEntity> openTSDBEntities = new ArrayList<>();
        String mainResource = Long.toString(data.getResourceId());
        if (data.getIsUp()) {
            openTSDBEntities.add(new OpenTSDBEntity("k8s_up", 1L,
                Map.of("resource", mainResource)));
            if (data.getLatencySeconds() != null) {
                openTSDBEntities.add(new OpenTSDBEntity("k8s_latency_seconds", data.getLatencySeconds(),
                    Map.of("resource", mainResource)));
            }
            data.getPods().entries().forEach(entry -> {
                String deployment = entry.getKey();
                String resourceDeployment = Long.toString(entry.getValue().getResourceDeploymentId());
                openTSDBEntities.add(new OpenTSDBEntity("k8s_pod_cpu_used", entry.getValue().getCPUUsed().doubleValue(),
                    Map.of("resource", Long.toString(data.getResourceId()), "deployment", deployment,
                        "resource_deployment", resourceDeployment)));
                openTSDBEntities.add(new OpenTSDBEntity("k8s_pod_memory_used_bytes",
                    entry.getValue().getMemoryUsed().doubleValue(),
                    Map.of("resource", mainResource, "deployment", deployment, "resource_deployment",
                        resourceDeployment)));
            });
            openTSDBEntities.add(new OpenTSDBEntity("k8s_cpu_total", data.getTotalCPU().doubleValue(),
                Map.of("resource", mainResource)));
            openTSDBEntities.add(new OpenTSDBEntity("k8s_cpu_used", data.getCPUUsed().doubleValue(),
                Map.of("resource", mainResource)));
            openTSDBEntities.add(new OpenTSDBEntity("k8s_memory_total_bytes", data.getTotalMemory().doubleValue(),
                Map.of("resource", mainResource)));
            openTSDBEntities.add(new OpenTSDBEntity("k8s_memory_used_bytes", data.getMemoryUsed().doubleValue(),
                Map.of("resource", mainResource)));
            data.getNodes().forEach(k8sNode -> {
                String resource = Long.toString(k8sNode.getResourceId());
                openTSDBEntities.add(new OpenTSDBEntity("k8s_node_cpu_total", k8sNode.getTotalCPU().doubleValue(),
                    Map.of("main_resource", mainResource, "resource", resource, "node", k8sNode.getName())));
                openTSDBEntities.add(new OpenTSDBEntity("k8s_node_cpu_used", k8sNode.getCPUUsed().doubleValue(),
                    Map.of("main_resource", mainResource, "resource", resource, "node", k8sNode.getName())));
                openTSDBEntities.add(new OpenTSDBEntity("k8s_node_memory_total_bytes",
                    k8sNode.getTotalMemory().doubleValue(), Map.of("main_resource", mainResource, "resource",
                    mainResource, "node", k8sNode.getName())));
                openTSDBEntities.add(new OpenTSDBEntity("k8s_node_memory_used_bytes",
                    k8sNode.getMemoryUsed().doubleValue(), Map.of("main_resource", mainResource, "resource",
                    mainResource, "node", k8sNode.getName())));
            });
        } else {
            openTSDBEntities.add(new OpenTSDBEntity("k8s_up", 0L, Map.of("resource", mainResource)));
        }

        monitoringPusher.pushMetrics(openTSDBEntities, resultHandler);
    }
}
