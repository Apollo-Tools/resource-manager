package at.uibk.dps.rm.service.database.metric;

import at.uibk.dps.rm.entity.model.Platform;
import at.uibk.dps.rm.entity.model.PlatformMetric;
import at.uibk.dps.rm.repository.metric.PlatformMetricRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import at.uibk.dps.rm.util.validation.ServiceResultValidator;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.hibernate.reactive.stage.Stage;

import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

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
    public Future<JsonArray> findAllByPlatformId(long platformId) {
        CompletionStage<List<PlatformMetric>> getAll = withSession(session ->
            session.find(Platform.class, platformId)
                .thenCompose(platform -> {
                    ServiceResultValidator.checkFound(platform, Platform.class);
                    return repository.findAllByPlatform(session, platformId);
                })
        );
        return sessionToFuture(getAll)
            .map(platformMetrics -> {
                List<JsonObject> result = platformMetrics.stream()
                    .peek(platformMetric -> platformMetric.setPlatform(null))
                    .map(JsonObject::mapFrom)
                    .collect(Collectors.toList());
                return new JsonArray(result);
            });
    }
}
