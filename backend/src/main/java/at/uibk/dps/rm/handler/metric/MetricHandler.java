package at.uibk.dps.rm.handler.metric;

import at.uibk.dps.rm.handler.*;

/**
 * Processes the http requests that concern the metric entity.
 *
 * @author matthi-g
 */
public class MetricHandler extends ValidationHandler {

    /**
     * Create an instance from the metricChecker and metricTypeChecker.
     *
     * @param metricChecker the metric checker
     */
    public MetricHandler(MetricChecker metricChecker) {
        super(metricChecker);
    }
}
