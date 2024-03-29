package at.uibk.dps.rm.service.database.metric;

import at.uibk.dps.rm.entity.model.PlatformMetric;
import at.uibk.dps.rm.repository.metric.PlatformMetricRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import at.uibk.dps.rm.util.misc.RxVertxHandler;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import at.uibk.dps.rm.service.database.util.SessionManagerProvider;

import java.util.List;

/**
 * This is the implementation of the {@link PlatformMetricService}.
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
    public PlatformMetricServiceImpl(PlatformMetricRepository repository, SessionManagerProvider smProvider) {
        super(repository, PlatformMetric.class, smProvider);
        this.repository = repository;
    }

    @Override
    public void findAllByPlatformId(long platformId, Handler<AsyncResult<JsonArray>> resultHandler) {
        Single<List<PlatformMetric>> getAll = smProvider.withTransactionSingle(sm -> repository
            .findAllByPlatform(sm, platformId)
        );
        RxVertxHandler.handleSession(getAll.map(this::mapResultListToJsonArray), resultHandler);
    }
}
