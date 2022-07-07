package at.uibk.dps.rm.service.metric;

import at.uibk.dps.rm.repository.metric.MetricValueRepository;
import at.uibk.dps.rm.repository.metric.entity.MetricValue;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

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
    public Future<JsonObject> findOne(long id) {
        return null;
    }

    @Override
    public Future<Boolean> existsOneByResourceAndMetricId(long resourceId, long metricId) {
        return Future
            .fromCompletionStage(metricValueRepository.findByResourceAndMetric(resourceId, metricId))
            .map(Objects::nonNull);
    }

    @Override
    public Future<Void> delete(long id) {
        return null;
    }
}
