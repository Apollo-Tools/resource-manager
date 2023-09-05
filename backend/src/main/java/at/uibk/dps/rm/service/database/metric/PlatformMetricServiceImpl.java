package at.uibk.dps.rm.service.database.metric;

import at.uibk.dps.rm.entity.model.PlatformMetric;
import at.uibk.dps.rm.repository.metric.PlatformMetricRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import at.uibk.dps.rm.service.util.RxVertxHandler;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.hibernate.reactive.stage.Stage;

import java.util.List;
import java.util.stream.Collectors;

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
    public PlatformMetricServiceImpl(PlatformMetricRepository repository, Stage.SessionFactory sessionFactory) {
        super(repository, PlatformMetric.class, sessionFactory);
        this.repository = repository;
    }

    @Override
    public void findAllByPlatformId(long platformId, Handler<AsyncResult<JsonArray>> resultHandler) {
        Single<List<PlatformMetric>> getAll = withTransactionSingle(sessionManager -> repository
            .findAllByPlatform(sessionManager, platformId)
        );
        RxVertxHandler.handleSession(
            getAll.map(platformMetrics -> {
                List<JsonObject> result = platformMetrics.stream()
                    .map(JsonObject::mapFrom)
                    .collect(Collectors.toList());
                return new JsonArray(result);
            }),
            resultHandler
        );
    }
}
