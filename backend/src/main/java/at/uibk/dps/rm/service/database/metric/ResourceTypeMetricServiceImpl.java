package at.uibk.dps.rm.service.database.metric;

import at.uibk.dps.rm.entity.model.ResourceTypeMetric;
import at.uibk.dps.rm.repository.metric.ResourceTypeMetricRepository;
import at.uibk.dps.rm.service.database.ServiceProxy;

public class ResourceTypeMetricServiceImpl extends ServiceProxy<ResourceTypeMetric>
    implements ResourceTypeMetricService {
    public ResourceTypeMetricServiceImpl(ResourceTypeMetricRepository repository) {
        super(repository, ResourceTypeMetric.class);
    }
}
