package at.uibk.dps.rm.service.database.metric;

import at.uibk.dps.rm.entity.model.PlatformMetric;
import at.uibk.dps.rm.repository.metric.PlatformMetricRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import io.vertx.core.Future;
import org.hibernate.reactive.stage.Stage;

import java.util.concurrent.CompletionStage;

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
    public PlatformMetricServiceImpl(PlatformMetricRepository repository, Stage.SessionFactory sessionFactory) {
        super(repository, PlatformMetric.class, sessionFactory);
        this.repository = repository;
    }

    @Override
    public Future<Boolean> missingRequiredPlatformMetricsByResourceId(long resourceId) {
        CompletionStage<Long> count = withSession(session ->
            repository.countMissingRequiredMetricValuesByResourceId(session, resourceId));
        return Future.fromCompletionStage(count)
            .map(result -> result > 0);
    }
}
