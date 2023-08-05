package at.uibk.dps.rm.entity.monitoring;

import at.uibk.dps.rm.entity.dto.metric.MetricTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum K8sMonitoringMetricEnum {
    /**
     * K8s Hostname
     */
    HOSTNAME("hostname", MetricTypeEnum.STRING),
    /**
     * Available CPU
     */
    CPU("cpu", MetricTypeEnum.NUMBER),
    /**
     * Available memory
     */
    MEMORY_SIZE("memory-size", MetricTypeEnum.NUMBER);

    private final String name;

    private final MetricTypeEnum metricType;
}
