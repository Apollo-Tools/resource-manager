package at.uibk.dps.rm.service.monitoring.metricpusher;

import at.uibk.dps.rm.entity.dto.config.ConfigDTO;
import at.uibk.dps.rm.entity.monitoring.kubernetes.K8sMonitoringData;
import at.uibk.dps.rm.entity.monitoring.OpenTSDBEntity;
import at.uibk.dps.rm.entity.monitoring.kubernetes.K8sNode;
import at.uibk.dps.rm.entity.monitoring.kubernetes.K8sPod;
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
            composeClusterMetrics(data, openTSDBEntities, mainResource);
            data.getNodes().forEach(k8sNode -> {
                String resource = Long.toString(k8sNode.getResourceId());
                composeNodeMetrics(k8sNode, openTSDBEntities, mainResource, resource);
                k8sNode.getPods().forEach((name, pod) ->
                    composePodMetrics(name, pod, openTSDBEntities, resource, mainResource));
            });
        } else {
            openTSDBEntities.add(new OpenTSDBEntity("k8s_up", 0L, Map.of("resource", mainResource)));
        }

        monitoringPusher.pushMetrics(openTSDBEntities, resultHandler);
    }

    private void composeNodeMetrics(K8sNode k8sNode, List<OpenTSDBEntity> openTSDBEntities, String mainResource,
            String resource) {
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
    }

    private void composeClusterMetrics(K8sMonitoringData data, List<OpenTSDBEntity> openTSDBEntities,
            String mainResource) {
        openTSDBEntities.add(new OpenTSDBEntity("k8s_up", 1L, Map.of("resource", mainResource)));
        if (data.getLatencySeconds() != null) {
            openTSDBEntities.add(new OpenTSDBEntity("k8s_latency_seconds", data.getLatencySeconds(),
                Map.of("resource", mainResource)));
        }
        openTSDBEntities.add(new OpenTSDBEntity("k8s_cpu_total", data.getTotalCPU().doubleValue(),
            Map.of("resource", mainResource)));
        openTSDBEntities.add(new OpenTSDBEntity("k8s_cpu_used", data.getCPUUsed().doubleValue(),
            Map.of("resource", mainResource)));
        openTSDBEntities.add(new OpenTSDBEntity("k8s_memory_total_bytes", data.getTotalMemory().doubleValue(),
            Map.of("resource", mainResource)));
        openTSDBEntities.add(new OpenTSDBEntity("k8s_memory_used_bytes", data.getMemoryUsed().doubleValue(),
            Map.of("resource", mainResource)));
    }

    private void composePodMetrics(String name, K8sPod pod, List<OpenTSDBEntity> openTSDBEntities,
            String resource, String mainResource) {
        String resourceDeployment = Long.toString(pod.getResourceDeploymentId());
        String deployment = Long.toString(pod.getDeploymentId());
        String service = Long.toString(pod.getServiceId());
        openTSDBEntities.add(new OpenTSDBEntity("k8s_pod_cpu_total", pod.getTotalCPU().doubleValue(),
            Map.of("name", name, "resource", resource, "main_resource", mainResource, "deployment",
                deployment, "resource_deployment", resourceDeployment, "service", service)));
        openTSDBEntities.add(new OpenTSDBEntity("k8s_pod_cpu_used", pod.getCPUUsed().doubleValue(),
            Map.of("name", name, "resource", resource, "main_resource", mainResource,
                "deployment", deployment, "resource_deployment", resourceDeployment, "service", service)));
        openTSDBEntities.add(new OpenTSDBEntity("k8s_pod_memory_total_bytes",
            pod.getTotalMemory().doubleValue(),
            Map.of("name", name, "resource", resource, "main_resource", mainResource, "deployment",
                deployment, "resource_deployment", resourceDeployment, "service", service)));
        openTSDBEntities.add(new OpenTSDBEntity("k8s_pod_memory_used_bytes",
            pod.getMemoryUsed().doubleValue(),
            Map.of("name", name, "resource", resource, "main_resource", mainResource, "deployment",
                deployment, "resource_deployment", resourceDeployment, "service", service)));
    }
}
