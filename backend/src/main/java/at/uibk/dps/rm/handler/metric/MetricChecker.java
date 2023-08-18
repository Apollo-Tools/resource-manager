package at.uibk.dps.rm.handler.metric;

import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.service.rxjava3.database.metric.MetricService;

/**
 * Implements methods to perform CRUD operations on the metric entity.
 *
 * @see EntityChecker
 *
 * @author matthi-g
 */
public class MetricChecker extends EntityChecker {

    /**
     * Create an instance from the metricService.
     *
     * @param metricService the metric service
     */
    public MetricChecker(MetricService metricService) {
        super(metricService);
    }
}
