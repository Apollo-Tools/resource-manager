package at.uibk.dps.rm.entity.monitoring.kubernetes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.openapi.models.V1Pod;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Implements the monitored {@link K8sEntityData} for a k8s pod.
 *
 * @author matthi-g
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class K8sPod implements K8sEntityData {

    private String name;

    private long deploymentId;

    private long resourceDeploymentId;

    private long serviceId;

    private V1Pod v1Pod;

    private Quantity cpuLoad = new Quantity("0");

    private Quantity memoryLoad = new Quantity("0");

    private Quantity storageLoad = new Quantity("0");


    @Override
    @JsonIgnore
    public BigDecimal getTotalCPU() {
        if (v1Pod.getSpec() != null && !v1Pod.getSpec().getContainers().isEmpty() &&
                v1Pod.getSpec().getContainers().get(0).getResources() != null) {
            return  v1Pod.getSpec().getContainers().get(0).getResources().getLimits().get("cpu").getNumber();
        }
        throw new NullPointerException();
    }

    @Override
    @JsonIgnore
    public BigDecimal getTotalMemory() {
        if (v1Pod.getSpec() != null && !v1Pod.getSpec().getContainers().isEmpty() &&
                v1Pod.getSpec().getContainers().get(0).getResources() != null) {
            return  v1Pod.getSpec().getContainers().get(0).getResources().getLimits().get("memory").getNumber();
        }
        throw new NullPointerException();
    }

    @Override
    @JsonIgnore
    public BigDecimal getTotalStorage() {
        return BigDecimal.ZERO;
    }

    @Override
    @JsonIgnore
    public BigDecimal getCPUUsed() {
        return cpuLoad.getNumber();
    }

    @Override
    @JsonIgnore
    public BigDecimal getMemoryUsed() {
        return memoryLoad.getNumber();
    }

    @Override
    @JsonIgnore
    public BigDecimal getStorageUsed() {
        return BigDecimal.ZERO;
    }
}
