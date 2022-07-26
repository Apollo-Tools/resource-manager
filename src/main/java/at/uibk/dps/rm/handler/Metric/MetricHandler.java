package at.uibk.dps.rm.handler.Metric;

import at.uibk.dps.rm.handler.*;
import at.uibk.dps.rm.service.rxjava3.metric.MetricService;

public class MetricHandler extends ValidationHandler {
    public MetricHandler(MetricService metricService) {
        super(new MetricChecker(metricService));
    }
}
