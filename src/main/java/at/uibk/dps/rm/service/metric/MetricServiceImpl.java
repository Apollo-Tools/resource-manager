package at.uibk.dps.rm.service.metric;

import at.uibk.dps.rm.repository.metric.MetricRepository;
import at.uibk.dps.rm.repository.metric.entity.Metric;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.Objects;

public class MetricServiceImpl implements MetricService{

    private final MetricRepository metricRepository;

    public MetricServiceImpl(MetricRepository metricRepository) {
        this.metricRepository = metricRepository;
    }

    @Override
    public Future<JsonObject> save(JsonObject data) {
        Metric metric = data.mapTo(Metric.class);

        return Future
            .fromCompletionStage(metricRepository.create(metric))
            .map(JsonObject::mapFrom);
    }

    @Override
    public Future<JsonObject> findOne(long id) {
        return Future
            .fromCompletionStage(metricRepository.findById(id))
            .map(JsonObject::mapFrom);
    }

    @Override
    public Future<Boolean> existsOneById(long id) {
        return Future
            .fromCompletionStage(metricRepository.findById(id))
            .map(Objects::nonNull);
    }

    @Override
    public Future<Boolean> existsOneByMetric(String url) {
        return Future
            .fromCompletionStage(metricRepository.findByMetric(url))
            .map(Objects::nonNull);
    }

    @Override
    public Future<JsonArray> findAll() {
        return Future
            .fromCompletionStage(metricRepository.findAll())
            .map(result -> {
                ArrayList<JsonObject> objects = new ArrayList<>();
                for (Metric metric: result) {
                    objects.add(JsonObject.mapFrom(metric));
                }
                return new JsonArray(objects);
            });
    }

    @Override
    public Future<Void> update(JsonObject data) {
        Metric metric = data.mapTo(Metric.class);
        return Future
            .fromCompletionStage(metricRepository.update(metric))
            .mapEmpty();
    }

    @Override
    public Future<Void> delete(long id) {
        Metric metric = new Metric();
        metric.setMetricId(id);
        return Future
            .fromCompletionStage(metricRepository.delete(metric))
            .mapEmpty();
    }
}
