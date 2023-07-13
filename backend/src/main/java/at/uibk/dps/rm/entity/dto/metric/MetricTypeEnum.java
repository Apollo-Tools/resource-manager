package at.uibk.dps.rm.entity.dto.metric;

import at.uibk.dps.rm.entity.model.MetricType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * Represents the supported metric types.
 *
 * @author matthi-g
 */
@AllArgsConstructor
@Getter
public enum MetricTypeEnum {
    /**
     * Number
     */
    NUMBER("number"),
    /**
     * String
     */
    STRING("string"),
    /**
     * Boolean
     */
    BOOLEAN("boolean");

    private final String value;

    /**
     * Create an instance from a MetricType. This is necessary because a public method is not
     * allowed.
     * <p>
     * ref: <a href="https://stackoverflow.com/a/45082346/13164629">Source</a>
     *
     * @param metricType the metric type
     * @return the created object
     */
    public static MetricTypeEnum fromMetricType(MetricType metricType) {
        return Arrays.stream(MetricTypeEnum.values())
            .filter(value -> value.value.equals(metricType.getType()))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("unknown value: " + metricType.getType()));
    }
}
