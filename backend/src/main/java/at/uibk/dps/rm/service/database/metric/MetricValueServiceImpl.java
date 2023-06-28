package at.uibk.dps.rm.service.database.metric;

import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.repository.metric.MetricValueRepository;
import at.uibk.dps.rm.entity.model.MetricValue;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.hibernate.reactive.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

/**
 * This is the implementation of the #MetricValueService.
 *
 * @author matthi-g
 */
public class MetricValueServiceImpl extends DatabaseServiceProxy<MetricValue> implements MetricValueService{

    private final MetricValueRepository repository;

    /**
     * Create an instance from the metricValueRepository.
     *
     * @param repository the metric value repository
     */
    public MetricValueServiceImpl(MetricValueRepository repository, Stage.SessionFactory sessionFactory) {
        super(repository, MetricValue.class, sessionFactory);
        this.repository = repository;
    }

    @Override
    public Future<Void> saveAll(JsonArray data) {
        List<MetricValue> metricValues = data
            .stream()
            .map(object -> {
                JsonObject metricValueJson = (JsonObject) object;
                Resource resource = metricValueJson.getJsonObject("resource").mapTo(Resource.class);
                MetricValue metricValue = metricValueJson.mapTo(MetricValue.class);
                metricValue.setResource(resource);
                return metricValue;
            })
            .collect(Collectors.toList());
        CompletionStage<Void> createAll = withTransaction(session -> repository.createAll(session, metricValues));
        return Future.fromCompletionStage(createAll);
    }

    @Override
    public Future<JsonObject> findOne(long id) {
        CompletionStage<MetricValue> findOne = withSession(session -> repository.findByIdAndFetch(session, id));
        return Future.fromCompletionStage(findOne)
                .map(JsonObject::mapFrom);
    }

    @Override
    public Future<JsonArray> findAllByResource(long resourceId, boolean includeValue) {
        CompletionStage<List<MetricValue>> findAll = withSession(session ->
            repository.findByResourceAndFetch(session, resourceId));
        return Future.fromCompletionStage(findAll)
            .map(result -> {
                ArrayList<JsonObject> objects = new ArrayList<>();
                for (MetricValue metricValue: result) {
                    // necessary check because when no metric values are present the result list contains one null value
                    if (metricValue == null) {
                        continue;
                    }
                    JsonObject entity;
                    if (includeValue) {
                        entity = JsonObject.mapFrom(metricValue);
                    } else {
                        entity = JsonObject.mapFrom(metricValue.getMetric());
                    }
                    objects.add(entity);
                }
                return new JsonArray(objects);
            });
    }

    @Override
    public Future<JsonObject> findOneByResourceAndMetric(long resourceId, long metricId) {
        CompletionStage<MetricValue> findOne = withSession(session ->
            repository.findByResourceAndMetric(session, resourceId, metricId));
        return Future.fromCompletionStage(findOne)
            .map(JsonObject::mapFrom);
    }

    @Override
    public Future<Boolean> existsOneByResourceAndMetric(long resourceId, long metricId) {
        CompletionStage<MetricValue> findOne = withSession(session ->
            repository.findByResourceAndMetric(session, resourceId, metricId));
        return Future.fromCompletionStage(findOne)
            .map(Objects::nonNull);
    }

    @Override
    public Future<Void> updateByResourceAndMetric(long resourceId, long metricId, String valueString,
            Double valueNumber, Boolean valueBool) {
        CompletionStage<Void> update = withTransaction(session ->
            repository.updateByResourceAndMetric(session, resourceId, metricId, valueString, valueNumber, valueBool));
        return Future.fromCompletionStage(update)
            .mapEmpty();
    }

    @Override
    public Future<Void> deleteByResourceAndMetric(long resourceId, long metricId) {
        CompletionStage<Integer> delete = withTransaction(session ->
            repository.deleteByResourceAndMetric(session, resourceId, metricId));
        return Future.fromCompletionStage(delete)
            .mapEmpty();
    }
}
