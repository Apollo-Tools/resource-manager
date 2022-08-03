package at.uibk.dps.rm.handler.metric;

import at.uibk.dps.rm.handler.*;
import at.uibk.dps.rm.service.rxjava3.database.metric.MetricService;

public class MetricHandler extends ValidationHandler {
    public MetricHandler(MetricService metricService) {
        super(new MetricChecker(metricService));
    }
}
