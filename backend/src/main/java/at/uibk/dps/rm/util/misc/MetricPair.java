package at.uibk.dps.rm.util.misc;

import org.apache.commons.lang3.tuple.ImmutablePair;

/**
 * Represents a timestamp - value pair.
 *
 * @author matthi-g
 */
public class MetricPair extends ImmutablePair<Long, Double> {
    /**
     * Create a new instance from a timestamp and metricValue.
     *
     * @param timestamp the timestamp
     * @param metricValue the metric value
     */
    public MetricPair(Long timestamp, Double metricValue) {
        super(timestamp, metricValue);
    }
}
