package at.uibk.dps.rm.service.metric;

import at.uibk.dps.rm.repository.metric.MetricRepository;
import at.uibk.dps.rm.repository.metric.MetricValueRepository;
import at.uibk.dps.rm.repository.metric.entity.Metric;
import at.uibk.dps.rm.repository.metric.entity.MetricValue;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

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
    public Future<JsonObject> findOne(long id) {
        return null;
    }

    @Override
    public Future<Boolean> existsOneByResourceAndMetricId(long resourceId, long MetricId) {
        return null;
    }

    @Override
    public Future<Void> delete(long id) {
        return null;
    }
}
