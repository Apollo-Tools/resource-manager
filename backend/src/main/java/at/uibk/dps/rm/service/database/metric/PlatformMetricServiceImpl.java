package at.uibk.dps.rm.service.database.metric;

import at.uibk.dps.rm.entity.model.PlatformMetric;
import at.uibk.dps.rm.repository.metric.PlatformMetricRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import io.vertx.core.Future;

/**
 * This is the implementation of the #PlatformMetricService.
 *
 * @author matthi-g
 */
public class PlatformMetricServiceImpl extends DatabaseServiceProxy<PlatformMetric> implements PlatformMetricService {

    private final PlatformMetricRepository repository;

    /**
     * Create an instance from the repository.
     *
     * @param repository the platform metric repository
     */
    public PlatformMetricServiceImpl(PlatformMetricRepository repository) {
        super(repository, PlatformMetric.class);
        this.repository = repository;
    }

    @Override
    public Future<Boolean> missingRequiredPlatformMetricsByResourceId(long resourceId) {
        return Future
            .fromCompletionStage(repository.countMissingRequiredMetricValuesByResourceId(resourceId))
            .map(result -> result > 0);
    }
}
