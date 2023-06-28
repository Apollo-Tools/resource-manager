package at.uibk.dps.rm.repository.metric;

import at.uibk.dps.rm.entity.model.MetricType;
import at.uibk.dps.rm.repository.Repository;

/**
 * Implements database operations for the metric_type entity.
 *
 * @author matthi-g
 */
public class MetricTypeRepository extends Repository<MetricType> {

    /**
     * Create an instance.
     */
    public MetricTypeRepository() {
        super(MetricType.class);
    }
}
