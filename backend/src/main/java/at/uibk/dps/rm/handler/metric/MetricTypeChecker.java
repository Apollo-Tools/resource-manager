package at.uibk.dps.rm.handler.metric;

import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.service.rxjava3.database.metric.MetricTypeService;

public class MetricTypeChecker extends EntityChecker {
    public MetricTypeChecker(MetricTypeService metricTypeService) {
        super(metricTypeService);
    }
}
