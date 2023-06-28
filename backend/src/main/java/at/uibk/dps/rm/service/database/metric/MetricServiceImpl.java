package at.uibk.dps.rm.service.database.metric;

import at.uibk.dps.rm.repository.metric.MetricRepository;
import at.uibk.dps.rm.entity.model.Metric;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.hibernate.reactive.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;

/**
 * This is the implementation of the #MetricService.
 *
 * @author matthi-g
 */
public class MetricServiceImpl extends DatabaseServiceProxy<Metric> implements MetricService {

    private final MetricRepository repository;

    /**
     * Create an instance from the metricRepository.
     *
     * @param repository the metric repository
     */
    public MetricServiceImpl(MetricRepository repository, Stage.SessionFactory sessionFactory) {
        super(repository, Metric.class, sessionFactory);
        this.repository = repository;
    }

    @Override
    public Future<JsonArray> findAllByPlatformId(long resourceTypeId, boolean required) {
        CompletionStage<List<Metric>> findAll = withSession(session ->
            repository.findAllByPlatformId(session, resourceTypeId, required));
        return Future.fromCompletionStage(findAll)
            .map(result -> {
                ArrayList<JsonObject> objects = new ArrayList<>();
                for (Metric entity: result) {
                    objects.add(JsonObject.mapFrom(entity));
                }
                return new JsonArray(objects);
            });
    }

    @Override
    public Future<JsonObject> findOneByMetric(String metric) {
        CompletionStage<Metric> findOne = withSession(session -> repository.findByMetric(session, metric));
        return Future.fromCompletionStage(findOne)
            .map(JsonObject::mapFrom);
    }
}
