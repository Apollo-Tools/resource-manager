package at.uibk.dps.rm.service.database.metric;

import at.uibk.dps.rm.entity.model.MetricType;
import at.uibk.dps.rm.repository.metric.MetricTypeRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import org.hibernate.reactive.stage.Stage;

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
    public MetricTypeServiceImpl(MetricTypeRepository metricTypeRepository, Stage.SessionFactory sessionFactory) {
        super(metricTypeRepository, MetricType.class, sessionFactory);
    }
}
