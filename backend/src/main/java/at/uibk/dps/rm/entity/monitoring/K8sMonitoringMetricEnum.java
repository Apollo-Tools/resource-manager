package at.uibk.dps.rm.entity.monitoring;

import at.uibk.dps.rm.entity.dto.metric.MetricTypeEnum;
import at.uibk.dps.rm.entity.model.Metric;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@AllArgsConstructor
@Getter
public enum K8sMonitoringMetricEnum {
    /**
     * K8s Hostname
     */
    HOSTNAME("hostname", MetricTypeEnum.STRING),
    /**
     * Total available CPU
     */
    CPU("cpu", MetricTypeEnum.NUMBER),
    /**
     * Allocatable CPU
     */
    CPU_AVAILABLE("cpu-available", MetricTypeEnum.NUMBER),
    /**
     * Total available memory
     */
    MEMORY_SIZE("memory-size", MetricTypeEnum.NUMBER),
    /**
     * Available memory
     */
    MEMORY_SIZE_AVAILABLE("memory-size-available", MetricTypeEnum.NUMBER),
    /**
     * Total available storage
     */
    STORAGE_SIZE("storage-size", MetricTypeEnum.NUMBER),
    /**
     * Allocatable storage
     */
    STORAGE_SIZE_AVAILABLE("storage-size-available", MetricTypeEnum.NUMBER);

    private final String name;

    private final MetricTypeEnum metricType;

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
