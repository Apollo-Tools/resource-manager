package at.uibk.dps.rm.rx.handler.metric;

import at.uibk.dps.rm.rx.handler.ValidationHandler;
import at.uibk.dps.rm.rx.service.rxjava3.database.metric.MetricService;

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
