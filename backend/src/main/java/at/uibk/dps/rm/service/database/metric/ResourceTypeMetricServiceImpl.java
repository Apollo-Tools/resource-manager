package at.uibk.dps.rm.service.database.metric;

import at.uibk.dps.rm.entity.model.ResourceTypeMetric;
import at.uibk.dps.rm.repository.metric.ResourceTypeMetricRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import io.vertx.core.Future;

public class ResourceTypeMetricServiceImpl extends DatabaseServiceProxy<ResourceTypeMetric>
    implements ResourceTypeMetricService {

    private final ResourceTypeMetricRepository repository;

    public ResourceTypeMetricServiceImpl(ResourceTypeMetricRepository repository) {
        super(repository, ResourceTypeMetric.class);
        this.repository = repository;
    }

    @Override
    public Future<Boolean> missingRequiredResourceTypeMetricsByResourceId(long resourceId) {
        return Future
            .fromCompletionStage(repository.countMissingRequiredMetricValuesByResourceId(resourceId))
            .map(result -> result > 0);
    }
}
