package at.uibk.dps.rm.entity.monitoring.kubernetes;

import at.uibk.dps.rm.entity.model.Metric;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

// TODO: remove hard coded stuff, retrieve from database

/**
 * Represents the currently monitored metrics for k8s resources.
 *
 * @author matthi-g
 */
@AllArgsConstructor
@Getter
public enum K8sMonitoringMetricEnum {
    /**
     * K8s Hostname
     */
    HOSTNAME("hostname", false, true),
    /**
     * Total available CPU
     */
    CPU("cpu", true, true),
    /**
     * Allocatable CPU
     */
    CPU_AVAILABLE("cpu-available", true, true),
    /**
     * Total available memory
     */
    MEMORY_SIZE("memory-size", true, true),
    /**
     * Available memory
     */
    MEMORY_SIZE_AVAILABLE("memory-size-available", true, true),
    /**
     * Total available storage
     */
    STORAGE_SIZE("storage-size", true, true),
    /**
     * Allocatable storage
     */
    STORAGE_SIZE_AVAILABLE("storage-size-available", true, true);

    private final String name;
    private final boolean isMainResourceMetric;
    private final boolean isSubResourceMetric;

    /**
     * Create an instance from a metric. This is necessary because a public method is not
     * allowed.
     * <p>
     * ref: <a href="https://stackoverflow.com/a/45082346/13164629">Source</a>
     *
     * @param metric the metric
     * @return the created object
     */
    public static K8sMonitoringMetricEnum fromMetric(Metric metric) {
        return Arrays.stream(K8sMonitoringMetricEnum.values())
            .filter(value -> value.name.equals(metric.getMetric()))
            .findFirst()
            .orElse(null);
    }
}
