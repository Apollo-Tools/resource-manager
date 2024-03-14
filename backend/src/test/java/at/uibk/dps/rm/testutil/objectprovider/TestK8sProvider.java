package at.uibk.dps.rm.testutil.objectprovider;

import at.uibk.dps.rm.entity.monitoring.kubernetes.K8sMonitoringData;
import at.uibk.dps.rm.entity.monitoring.kubernetes.K8sNode;
import at.uibk.dps.rm.entity.monitoring.kubernetes.K8sPod;
import io.kubernetes.client.custom.ContainerMetrics;
import io.kubernetes.client.custom.NodeMetrics;
import io.kubernetes.client.custom.PodMetrics;
import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.openapi.models.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Utility class to instantiate objects that are different instances of various kubernetes classes.
 *
 * @author matthi-g
 */
public class TestK8sProvider {

    public static V1Secret createSecret() {
        V1Secret secret = new V1Secret();
        HashMap<String, byte[]> data = new HashMap<>();
        data.put("cluster1", "secret-value".getBytes());
        data.put("cluster2", "value-secret".getBytes());
        secret.setData(data);
        return secret;
    }

    public static V1Namespace createNamespace(String name) {
        V1Namespace namespace = new V1Namespace();
        V1ObjectMeta metadata = new V1ObjectMeta();
        metadata.setName(name);
        namespace.setMetadata(metadata);
        return namespace;
    }

    public static V1Node createNode(String name) {
        V1Node node = new V1Node();
        V1ObjectMeta metadata = new V1ObjectMeta();
        metadata.setName(name);
        node.setMetadata(metadata);
        return node;
    }

    public static V1Pod createPod(String name) {
        V1Pod v1Pod = new V1Pod();
        V1ObjectMeta podMetadata = new V1ObjectMeta();
        podMetadata.setName(name);
        v1Pod.setMetadata(podMetadata);
        return v1Pod;
    }

    public static NodeMetrics createNodeMetrics(String name, double cpuUsage, double memoryUsage) {
        NodeMetrics nodeMetrics = new NodeMetrics();
        V1ObjectMeta metadata = new V1ObjectMeta();
        metadata.setName(name);
        nodeMetrics.setMetadata(metadata);
        nodeMetrics.getUsage().put("cpu", new Quantity(String.valueOf(cpuUsage)));
        nodeMetrics.getUsage().put("memory", new Quantity(String.valueOf(memoryUsage)));
        return nodeMetrics;
    }

    public static ContainerMetrics createContainerMetrics(BigDecimal cpu, BigDecimal memory) {
        ContainerMetrics containerMetrics = new ContainerMetrics();
        containerMetrics.setUsage(Map.of("cpu", new Quantity(cpu, Quantity.Format.DECIMAL_SI), "memory",
            new Quantity(memory, Quantity.Format.DECIMAL_SI)));
        return  containerMetrics;
    }


    public static PodMetrics createPodMetrics(BigDecimal cpu, BigDecimal memory) {
        PodMetrics podMetrics = new PodMetrics();
        ContainerMetrics con1 = createContainerMetrics(cpu.subtract(new BigDecimal("0.1")), memory);
        ContainerMetrics con2 = createContainerMetrics(new BigDecimal("0.1"), BigDecimal.ZERO);
        podMetrics.setContainers(List.of(con1, con2));
        return podMetrics;
    }

    public static K8sNode createK8sNode(String name) {
        return createK8sNode(name, 0.0, 0.0, 0L, 0L, 0L, 0L);
    }

    public static K8sNode createK8sNode(String name, double totalCPU, double cpuLoad, long totalMemory, long memoryLoad,
                                        long totalStorage, long storageLoad) {
        K8sNode k8sNode = new K8sNode();
        V1Node v1Node = new V1Node();
        V1ObjectMeta meta = new V1ObjectMeta();
        meta.setName(name);
        meta.setLabels(new HashMap<>());
        Objects.requireNonNull(meta.getLabels()).put("kubernetes.io/hostname", name);
        v1Node.setMetadata(meta);
        V1NodeStatus nodeStatus = new V1NodeStatus();
        nodeStatus.putAllocatableItem("cpu", Quantity.fromString(String.valueOf(totalCPU)));
        nodeStatus.putAllocatableItem("memory", Quantity.fromString(String.valueOf(totalMemory)));
        nodeStatus.putAllocatableItem("ephemeral-storage", Quantity.fromString(String.valueOf(totalStorage)));
        v1Node.setStatus(nodeStatus);
        k8sNode.setNode(v1Node);
        k8sNode.setCpuLoad(Quantity.fromString(String.valueOf(cpuLoad)));
        k8sNode.setMemoryLoad(Quantity.fromString(String.valueOf(memoryLoad)));
        k8sNode.setStorageLoad(Quantity.fromString(String.valueOf(storageLoad)));
        return k8sNode;
    }

    public static K8sPod createK8sPod(String name) {
        K8sPod k8sPod = new K8sPod();
        k8sPod.setName(name);
        V1Pod v1Pod = createPod(name);
        k8sPod.setV1Pod(v1Pod);
        k8sPod.setDeploymentId(1L);
        k8sPod.setResourceDeploymentId(2L);
        return k8sPod;
    }

    public static K8sMonitoringData createK8sMonitoringData(String name, String basePath, long resourceId,
            List<K8sNode> nodes, List<V1Namespace> namespaces, boolean isUp, double latencySeconds) {
        return new K8sMonitoringData(name, basePath, resourceId, nodes, namespaces,
            isUp, latencySeconds);
    }
}
