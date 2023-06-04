package at.uibk.dps.rm.service.database.metric;

import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.repository.metric.MetricValueRepository;
import at.uibk.dps.rm.entity.model.MetricValue;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * This is the implementation of the #MetricValueService.
 *
 * @author matthi-g
 */
public class MetricValueServiceImpl extends DatabaseServiceProxy<MetricValue> implements MetricValueService{

    private final MetricValueRepository metricValueRepository;

    /**
     * Create an instance from the metricValueRepository.
     *
     * @param metricValueRepository the metric value repository
     */
    public MetricValueServiceImpl(MetricValueRepository metricValueRepository) {
        super(metricValueRepository, MetricValue.class);
        this.metricValueRepository = metricValueRepository;
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

        return Future
            .fromCompletionStage(metricValueRepository.createAll(metricValues));
    }

    @Override
    public Future<JsonObject> findOne(long id) {
        return Future
                .fromCompletionStage(metricValueRepository.findByIdAndFetch(id))
                .map(JsonObject::mapFrom);
    }

    @Override
    public Future<JsonArray> findAllByResource(long resourceId, boolean includeValue) {
        return Future
            .fromCompletionStage(metricValueRepository.findByResourceAndFetch(resourceId))
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
        return Future
            .fromCompletionStage(metricValueRepository.findByResourceAndMetric(resourceId, metricId))
            .map(JsonObject::mapFrom);
    }

    @Override
    public Future<Boolean> existsOneByResourceAndMetric(long resourceId, long metricId) {
        return Future
            .fromCompletionStage(metricValueRepository.findByResourceAndMetric(resourceId, metricId))
            .map(Objects::nonNull);
    }

    @Override
    public Future<Void> updateByResourceAndMetric(long resourceId, long metricId, String valueString, Double valueNumber,
                                                  Boolean valueBool) {
        return Future
                .fromCompletionStage(metricValueRepository.updateByResourceAndMetric(resourceId, metricId, valueString,
                        valueNumber, valueBool))
                .mapEmpty();
    }

    @Override
    public Future<Void> deleteByResourceAndMetric(long resourceId, long metricId) {
        return Future
            .fromCompletionStage(metricValueRepository.deleteByResourceAndMetric(resourceId, metricId))
            .mapEmpty();
    }
}
