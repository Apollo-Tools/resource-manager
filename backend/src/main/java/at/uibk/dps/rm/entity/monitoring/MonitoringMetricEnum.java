package at.uibk.dps.rm.entity.monitoring;

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
public enum MonitoringMetricEnum {
    /**
     * CPU%
     */
    CPU_UTIL("cpu%");

    private final String name;

    /**
     * Create an instance from a metric. This is necessary because a public method is not
     * allowed.
     * <p>
     * ref: <a href="https://stackoverflow.com/a/45082346/13164629">Source</a>
     *
     * @param metric the metric
     * @return the created object
     */
    public static MonitoringMetricEnum fromMetric(Metric metric) {
        return Arrays.stream(MonitoringMetricEnum.values())
            .filter(value -> value.name.equals(metric.getMetric()))
            .findFirst()
            .orElse(null);
    }
}
