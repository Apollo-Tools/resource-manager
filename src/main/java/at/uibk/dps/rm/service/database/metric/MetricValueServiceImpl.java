package at.uibk.dps.rm.service.database.metric;

import at.uibk.dps.rm.repository.metric.MetricValueRepository;
import at.uibk.dps.rm.repository.metric.entity.MetricValue;
import at.uibk.dps.rm.repository.resource.entity.Resource;
import at.uibk.dps.rm.service.database.ServiceProxy;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class MetricValueServiceImpl extends ServiceProxy<MetricValue> implements MetricValueService{

    private final MetricValueRepository metricValueRepository;

    public MetricValueServiceImpl(MetricValueRepository metricValueRepository) {
        super(metricValueRepository, MetricValue.class);
        this.metricValueRepository = metricValueRepository;
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
        return Future
                .fromCompletionStage(metricValueRepository.findByIdAndFetch(id))
                .map(result -> {
                    if (result != null) {
                        result.setResource(null);
                    }
                    return JsonObject.mapFrom(result);
                });
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
    public Future<JsonArray> findAllByMultipleMetrics(List<String> metrics) {
        return Future
            .fromCompletionStage(metricValueRepository.findByMultipleMetricsAndFetch(metrics))
            .map(result -> {
                ArrayList<JsonObject> objects = new ArrayList<>();
                for (Resource resource: result) {
                    for (MetricValue metricValue : resource.getMetricValues()) {
                        metricValue.setMetric(null);
                        metricValue.setResource(null);
                    }
                    objects.add(JsonObject.mapFrom(resource));
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
