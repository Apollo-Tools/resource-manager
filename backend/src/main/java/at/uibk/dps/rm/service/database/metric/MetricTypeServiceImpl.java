package at.uibk.dps.rm.service.database.metric;

import at.uibk.dps.rm.entity.model.MetricType;
import at.uibk.dps.rm.repository.metric.MetricTypeRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;

/**
 * This is the implementation of the #MetricTypeService.
 *
 * @author matthi-g
 */
public class MetricTypeServiceImpl extends DatabaseServiceProxy<MetricType> implements MetricTypeService{
    /**
     * Create an instance from the metricTypeRepository.
     *
     * @param metricTypeRepository the metric type repository
     */
    public MetricTypeServiceImpl(MetricTypeRepository metricTypeRepository) {
        super(metricTypeRepository, MetricType.class);
    }
}
