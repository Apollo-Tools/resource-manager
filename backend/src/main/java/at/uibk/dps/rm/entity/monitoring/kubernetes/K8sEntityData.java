package at.uibk.dps.rm.entity.monitoring.kubernetes;

import java.math.BigDecimal;

/**
 * Defines the methods to retrieve monitored data from a k8s resource.
 *
 * @author matthi-g
 */
public interface K8sEntityData {
    /**
     * Get the total cpu units of the k8s resource.
     *
     * @return the total cpu units
     */
    BigDecimal getTotalCPU();

    /**
     * Get the total memory of the k8s resource.
     *
     * @return the total memory in bytes
     */
    BigDecimal getTotalMemory();

    /**
     * Get the total storage of the k8s resource.
     *
     * @return the total storage in bytes
     */
    BigDecimal getTotalStorage();

    /**
     * Get the used cpu units of the k8s resource.
     *
     * @return the used cpu units
     */
    BigDecimal getCPUUsed();

    /**
     * Get the used memory of the k8s resource.
     *
     * @return the used memory in bytes
     */
    BigDecimal getMemoryUsed();

    /**
     * Get the used storage of the k8s resource.
     *
     * @return the used storage in bytes
     */
    BigDecimal getStorageUsed();
}
