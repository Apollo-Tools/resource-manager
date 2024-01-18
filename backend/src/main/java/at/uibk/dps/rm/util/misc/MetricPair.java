package at.uibk.dps.rm.util.misc;

import org.apache.commons.lang3.tuple.ImmutablePair;

public class MetricPair extends ImmutablePair<Long, Double> {
    public MetricPair(Long left, Double right) {
        super(left, right);
    }
}
