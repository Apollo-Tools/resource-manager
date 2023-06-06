package at.uibk.dps.rm.service.database.metric;

import at.uibk.dps.rm.repository.metric.MetricRepository;
import at.uibk.dps.rm.entity.model.Metric;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;

/**
 * This is the implementation of the #MetricService.
 *
 * @author matthi-g
 */
public class MetricServiceImpl extends DatabaseServiceProxy<Metric> implements MetricService {

    private final MetricRepository metricRepository;

    /**
     * Create an instance from the metricRepository.
     *
     * @param metricRepository the metric repository
     */
    public MetricServiceImpl(MetricRepository metricRepository) {
        super(metricRepository, Metric.class);
        this.metricRepository = metricRepository;
    }

    @Override
    public Future<JsonArray> findAllByPlatformId(long resourceTypeId, boolean required) {
        return Future
            .fromCompletionStage(metricRepository.findAllByPlatformId(resourceTypeId, required))
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
}
