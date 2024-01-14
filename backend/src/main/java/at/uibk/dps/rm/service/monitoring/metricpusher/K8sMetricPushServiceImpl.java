package at.uibk.dps.rm.service.monitoring.metricpusher;

import at.uibk.dps.rm.entity.dto.config.ConfigDTO;
import at.uibk.dps.rm.entity.monitoring.K8sMonitoringData;
import at.uibk.dps.rm.entity.monitoring.opentsdb.OpenTSDBEntity;
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
        if (data.getIsUp()) {
            openTSDBEntities.add(new OpenTSDBEntity("k8s_up", 1L,
                Map.of("resource", Long.toString(data.getResourceId()))));
            openTSDBEntities.add(new OpenTSDBEntity("k8s_cpu_total", data.getTotalCPU().doubleValue(),
                Map.of("resource", Long.toString(data.getResourceId()))));
            openTSDBEntities.add(new OpenTSDBEntity("k8s_cpu_available", data.getAvailableCPU().doubleValue(),
                Map.of("resource", Long.toString(data.getResourceId()))));
            openTSDBEntities.add(new OpenTSDBEntity("k8s_memory_total_bytes", data.getTotalMemory().doubleValue(),
                Map.of("resource", Long.toString(data.getResourceId()))));
            openTSDBEntities.add(new OpenTSDBEntity("k8s_memory_available_bytes", data.getAvailableMemory().doubleValue(),
                Map.of("resource", Long.toString(data.getResourceId()))));
            data.getNodes().forEach(k8sNode -> {
                openTSDBEntities.add(new OpenTSDBEntity("k8s_node_cpu_total", k8sNode.getTotalCPU().doubleValue(),
                    Map.of("main_resource", Long.toString(data.getResourceId()), "resource",
                        Long.toString(k8sNode.getResourceId()), "node", k8sNode.getName())));
                openTSDBEntities.add(new OpenTSDBEntity("k8s_node_cpu_available",
                    k8sNode.getAvailableCPU().doubleValue(), Map.of("main_resource",
                    Long.toString(data.getResourceId()), "resource", Long.toString(k8sNode.getResourceId()), "node",
                    k8sNode.getName())));
                openTSDBEntities.add(new OpenTSDBEntity("k8s_node_memory_total_bytes",
                    k8sNode.getTotalMemory().doubleValue(), Map.of("main_resource",
                    Long.toString(data.getResourceId()), "resource", Long.toString(k8sNode.getResourceId()), "node",
                    k8sNode.getName())));
                openTSDBEntities.add(new OpenTSDBEntity("k8s_node_memory_available_bytes",
                    k8sNode.getAvailableMemory().doubleValue(), Map.of("main_resource",
                    Long.toString(data.getResourceId()), "resource", Long.toString(k8sNode.getResourceId()), "node",
                    k8sNode.getName())));
            });
        } else {
            openTSDBEntities.add(new OpenTSDBEntity("k8s_up", 0L,
                Map.of("resource", Long.toString(data.getResourceId()))));
        }


        monitoringPusher.pushMetrics(openTSDBEntities, resultHandler);
    }
}
