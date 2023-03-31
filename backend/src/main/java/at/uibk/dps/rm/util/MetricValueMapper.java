package at.uibk.dps.rm.util;

import at.uibk.dps.rm.entity.model.MetricValue;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class MetricValueMapper {

    public static Map<String, MetricValue> mapMetricValues(Set<MetricValue> metricValues) {
        return metricValues
            .stream()
            .collect(Collectors.toMap(metricValue -> metricValue.getMetric().getMetric(),
                metricValue -> metricValue));
    }
}
