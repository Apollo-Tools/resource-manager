package at.uibk.dps.rm.entity.monitoring;

import java.math.BigDecimal;

public interface K8sEntityData {
    BigDecimal getTotalCPU();
    BigDecimal getTotalMemory();
    BigDecimal getTotalStorage();
    BigDecimal getAvailableCPU();
    BigDecimal getAvailableMemory();
    BigDecimal getAvailableStorage();
}
