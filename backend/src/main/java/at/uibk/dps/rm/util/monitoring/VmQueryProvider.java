package at.uibk.dps.rm.util.monitoring;

import at.uibk.dps.rm.entity.monitoring.victoriametrics.VmQuery;

/**
 * This interface defines methods for {@link VmQuery}s for different monitored metrics.
 *
 * @author matthi-g
 */
public interface VmQueryProvider {

    /**
     * Get a {@link VmQuery} that queries the resource availability.
     *
     * @return the availability query
     */
    VmQuery getAvailability();

    /**
     * Get a {@link VmQuery} that queries the resource cost.
     *
     * @return the cost query
     */
    VmQuery getCost();

    /**
     * Get a {@link VmQuery} that queries the total cpu amount.
     *
     * @return the cpu query
     */
    VmQuery getCpu();

    /**
     * Get a {@link VmQuery} that queries the cpu utilisation.
     *
     * @return the cpu utilisation query
     */
    VmQuery getCpuUtil();

    /**
     * Get a {@link VmQuery} that queries the network latency.
     *
     * @return the latency query
     */
    VmQuery getLatency();

    /**
     * Get a {@link VmQuery} that queries the total memory.
     *
     * @return the memory query
     */
    VmQuery getMemory();

    /**
     * Get a {@link VmQuery} that queries the memory utilisation.
     *
     * @return the memory utilisation query
     */
    VmQuery getMemoryUtil();

    /**
     * Get a {@link VmQuery} that queries the total storage.
     *
     * @return the storage query
     */
    VmQuery getStorage();

    /**
     * Get a {@link VmQuery} that queries the storage utilisation.
     *
     * @return the storage utilisation query
     */
    VmQuery getStorageUtil();

    /**
     * Get a {@link VmQuery} that queries whether a resource is reachable or not.
     *
     * @return the up query
     */
    VmQuery getUp();
}
