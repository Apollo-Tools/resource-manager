package at.uibk.dps.rm.entity.monitoring.kubernetes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.kubernetes.client.custom.Quantity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class K8sPod implements K8sEntityData {

    private long resourceId;

    private long deploymentId;

    private long resourceDeploymentId;

    private Quantity cpuLoad;

    private Quantity memoryLoad;

    private Quantity storageLoad = new Quantity("0");


    @Override
    @JsonIgnore
    public BigDecimal getTotalCPU() {
        return BigDecimal.ZERO;
    }

    @Override
    @JsonIgnore
    public BigDecimal getTotalMemory() {
        return BigDecimal.ZERO;
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
