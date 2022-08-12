package at.uibk.dps.rm.service.database.metric;

import at.uibk.dps.rm.repository.MetricRepository;
import at.uibk.dps.rm.entity.model.Metric;
import at.uibk.dps.rm.service.database.ServiceProxy;
import io.vertx.core.Future;

import java.util.Objects;

public class MetricServiceImpl extends ServiceProxy<Metric> implements MetricService{

    private final MetricRepository metricRepository;

    public MetricServiceImpl(MetricRepository metricRepository) {
        super(metricRepository, Metric.class);
        this.metricRepository = metricRepository;
    }

    @Override
    public Future<Boolean> existsOneByMetric(String metric) {
        return Future
            .fromCompletionStage(metricRepository.findByMetric(metric))
            .map(Objects::nonNull);
    }
}
