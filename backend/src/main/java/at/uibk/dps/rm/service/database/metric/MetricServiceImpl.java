package at.uibk.dps.rm.service.database.metric;

import at.uibk.dps.rm.repository.metric.MetricRepository;
import at.uibk.dps.rm.entity.model.Metric;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.Objects;

public class MetricServiceImpl extends DatabaseServiceProxy<Metric> implements MetricService {

    private final MetricRepository metricRepository;

    public MetricServiceImpl(MetricRepository metricRepository) {
        super(metricRepository, Metric.class);
        this.metricRepository = metricRepository;
    }

    @Override
    public Future<JsonArray> findAllByResourceTypeId(long resourceTypeId, boolean required) {
        return Future
            .fromCompletionStage(metricRepository.findAllByResourceTypeId(resourceTypeId, required))
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
        return Future
            .fromCompletionStage(metricRepository.findByMetric(metric))
            .map(JsonObject::mapFrom);
    }

    @Override
    public Future<Boolean> existsOneByMetric(String metric) {
        return Future
            .fromCompletionStage(metricRepository.findByMetric(metric))
            .map(Objects::nonNull);
    }
}
