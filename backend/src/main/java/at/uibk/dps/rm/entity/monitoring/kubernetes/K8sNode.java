package at.uibk.dps.rm.entity.monitoring.kubernetes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.openapi.models.V1Node;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Implements the monitored {@link K8sEntityData} for a k8s cluster node resource.
 *
 * @author matthi-g
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class K8sNode implements K8sEntityData {

    private long resourceId;

    private V1Node node;

    private Map<String, K8sPod> pods = new HashMap<>();

    private Quantity cpuLoad;

    private Quantity memoryLoad;

    private Quantity storageLoad;

    /**
     * Create an instance from the node.
     *
     * @param node a k8s node
     */
    public K8sNode(V1Node node) {
        this.node = node;
    }

    /**
     * Add a pod to the node.
     *
     * @param k8sPod the pod
     */
    @JsonIgnore
    public void addPod(K8sPod k8sPod) {
        pods.put(Objects.requireNonNull(k8sPod.getV1Pod().getMetadata()).getName(), k8sPod);
    }

    /**
     * Add multiple pods to the node.
     *
     * @param k8sPods the pods
     */
    @JsonIgnore
    public void addAllPods(List<K8sPod> k8sPods) {
        k8sPods.forEach(this::addPod);
    }

    /**
     * Get the name of the node.
     *
     * @return the name of the node
     */
    @JsonIgnore
    public String getName() {
        return Objects.requireNonNull(node.getMetadata()).getName();
    }

    /**
     * Get the value of the kubernetes.io/hostname label
     *
     * @return the hostname
     */
    @JsonIgnore
    public String getHostname() {
        if (node.getMetadata() != null && node.getMetadata().getLabels() != null) {
            return node.getMetadata().getLabels().get("kubernetes.io/hostname");
        }
        throw new NullPointerException();
    }

    @Override
    @JsonIgnore
    public BigDecimal getTotalCPU() {
        if (node.getStatus() != null && node.getStatus().getAllocatable() != null) {
            return node.getStatus().getAllocatable().get("cpu").getNumber();
        }
        throw new NullPointerException();
    }

    @Override
    @JsonIgnore
    public BigDecimal getTotalMemory() {
        if (node.getStatus() != null && node.getStatus().getAllocatable() != null) {
            return node.getStatus().getAllocatable().get("memory").getNumber();
        }
        throw new NullPointerException();
    }

    @Override
    @JsonIgnore
    public BigDecimal getTotalStorage() {
        if (node.getStatus() != null && node.getStatus().getAllocatable() != null) {
            return node.getStatus().getAllocatable().get("ephemeral-storage").getNumber();
        }
        throw new NullPointerException();
    }

    @Override
    @JsonIgnore
    public BigDecimal getCPUUsed() {
        return getCpuLoad().getNumber();
    }

    @Override
    @JsonIgnore
    public BigDecimal getMemoryUsed() {
        return getMemoryLoad().getNumber();
    }

    @Override
    @JsonIgnore
    public BigDecimal getStorageUsed() {
        return getStorageLoad().getNumber();
    }
}