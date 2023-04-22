package at.uibk.dps.rm.service.database.metric;

import at.uibk.dps.rm.entity.model.MetricType;
import at.uibk.dps.rm.repository.metric.MetricTypeRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;

public class MetricTypeServiceImpl extends DatabaseServiceProxy<MetricType> implements MetricTypeService{

    public MetricTypeServiceImpl(MetricTypeRepository metricTypeRepository) {
        super(metricTypeRepository, MetricType.class);
    }
}
