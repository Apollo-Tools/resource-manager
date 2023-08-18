package at.uibk.dps.rm.entity.monitoring;

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
     * Get the free cpu units of the k8s resource.
     *
     * @return the free cpu units
     */
    BigDecimal getAvailableCPU();

    /**
     * Get the free memory of the k8s resource.
     *
     * @return the free memory in bytes
     */
    BigDecimal getAvailableMemory();

    /**
     * Get the free storage of the k8s resource.
     *
     * @return the free storage in bytes
     */
    BigDecimal getAvailableStorage();
}
