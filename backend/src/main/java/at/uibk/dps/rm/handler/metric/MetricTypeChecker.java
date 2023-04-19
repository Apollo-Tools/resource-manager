package at.uibk.dps.rm.handler.metric;

import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.service.rxjava3.database.metric.MetricTypeService;

/**
 * Implements methods to perform CRUD operations on the metric_type entity.
 *
 * @see EntityChecker
 *
 * @author matthi-g
 */
public class MetricTypeChecker extends EntityChecker {

    /**
     * Create an instance from the metricTypeService.
     *
     * @param metricTypeService the metric type service
     */
    public MetricTypeChecker(MetricTypeService metricTypeService) {
        super(metricTypeService);
    }
}
