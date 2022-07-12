package at.uibk.dps.rm.service.metric;

import at.uibk.dps.rm.repository.metric.MetricValueRepository;
import at.uibk.dps.rm.repository.metric.entity.MetricValue;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class MetricValueServiceImpl implements MetricValueService{

    private final MetricValueRepository metricValueRepository;

    public MetricValueServiceImpl(MetricValueRepository metricValueRepository) {
        this.metricValueRepository = metricValueRepository;
    }

    @Override
    public Future<JsonObject> save(JsonObject data) {
        MetricValue metricValue = data.mapTo(MetricValue.class);

        return Future
                .fromCompletionStage(metricValueRepository.create(metricValue))
                .map(JsonObject::mapFrom);
    }

    @Override
    public Future<Void> saveAll(JsonArray data) {
        List<MetricValue> metricValues = data
            .stream()
            .map(object -> ((JsonObject) object).mapTo(MetricValue.class))
            .collect(Collectors.toList());

        return Future
            .fromCompletionStage(metricValueRepository.createAll(metricValues));
    }

    @Override
    public Future<JsonArray> findAllByResource(long resourceId) {
        return Future
            .fromCompletionStage(metricValueRepository.findByResourceAndFetch(resourceId))
            .map(result -> {
                ArrayList<JsonObject> objects = new ArrayList<>();
                for (MetricValue metricValue: result) {
                    objects.add(JsonObject.mapFrom(metricValue.getMetric()));
                }
                return new JsonArray(objects);
            });
    }

    @Override
    public Future<Boolean> existsOneByResourceAndMetric(long resourceId, long metricId) {
        return Future
            .fromCompletionStage(metricValueRepository.findByResourceAndMetric(resourceId, metricId))
            .map(Objects::nonNull);
    }

    @Override
    public Future<Void> deleteByResourceAndMetric(long resourceId, long metricId) {
        return Future
            .fromCompletionStage(metricValueRepository.deleteByResourceAndMetric(resourceId, metricId))
            .mapEmpty();
    }
}
