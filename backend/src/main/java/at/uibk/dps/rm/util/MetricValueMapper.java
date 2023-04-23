package at.uibk.dps.rm.util;

import at.uibk.dps.rm.entity.model.MetricValue;
import lombok.experimental.UtilityClass;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A utility class to map metric values to their metric.
 *
 */
@UtilityClass
public class MetricValueMapper {

    /**
     * Map metric values to their metric name.
     *
     * @param metricValues the metric values
     * @return a Map where the metric name is the key and the metric value is the value
     */
    public static Map<String, MetricValue> mapMetricValues(Set<MetricValue> metricValues) {
        return metricValues
            .stream()
            .collect(Collectors.toMap(metricValue -> metricValue.getMetric().getMetric(),
                metricValue -> metricValue));
    }
}
