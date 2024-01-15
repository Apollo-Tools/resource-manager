package at.uibk.dps.rm.testutil.objectprovider;

import at.uibk.dps.rm.entity.monitoring.kubernetes.K8sNode;
import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.openapi.models.V1Namespace;
import io.kubernetes.client.openapi.models.V1Node;
import io.kubernetes.client.openapi.models.V1NodeStatus;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import lombok.experimental.UtilityClass;

import java.util.HashMap;
import java.util.Objects;

/**
 * Utility class to instantiate objects that are some type of monitoring data.
 *
 * @author matthi-g
 */
@UtilityClass
public class TestMonitoringDataProvider {

    public static K8sNode createK8sNode(String name) {
        return createK8sNode(name, 0.0, 0.0, 0L, 0L, 0L,
            0L);
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

    public static V1Namespace createV1Namespace(String name) {
        V1Namespace namespace = new V1Namespace();
        V1ObjectMeta meta = new V1ObjectMeta();
        meta.setName(name);
        namespace.setMetadata(meta);
        return namespace;
    }
}
