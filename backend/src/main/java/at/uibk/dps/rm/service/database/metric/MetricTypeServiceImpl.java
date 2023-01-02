package at.uibk.dps.rm.service.database.metric;

import at.uibk.dps.rm.entity.model.MetricType;
import at.uibk.dps.rm.repository.MetricTypeRepository;
import at.uibk.dps.rm.service.database.ServiceProxy;

public class MetricTypeServiceImpl extends ServiceProxy<MetricType> implements MetricTypeService{

    public MetricTypeServiceImpl(MetricTypeRepository metricTypeRepository) {
        super(metricTypeRepository, MetricType.class);
    }
}
