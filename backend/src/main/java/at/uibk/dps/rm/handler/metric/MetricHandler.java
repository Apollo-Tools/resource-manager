package at.uibk.dps.rm.handler.metric;

import at.uibk.dps.rm.handler.ValidationHandler;
import at.uibk.dps.rm.service.rxjava3.database.metric.MetricService;

/**
 * Processes the http requests that concern the metric entity.
 *
 * @author matthi-g
 */
public class MetricHandler extends ValidationHandler {

    /**
     * Create an instance from the metricService.
     *
     * @param metricService the service
     */
    public MetricHandler(MetricService metricService) {
        super(metricService);
    }
}
