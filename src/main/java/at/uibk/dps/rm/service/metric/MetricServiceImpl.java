package at.uibk.dps.rm.service.metric;

import at.uibk.dps.rm.repository.metric.MetricRepository;
import at.uibk.dps.rm.repository.metric.entity.Metric;
import at.uibk.dps.rm.service.ServiceProxy;
import io.vertx.core.Future;

import java.util.Objects;

public class MetricServiceImpl extends ServiceProxy<Metric> implements MetricService{

    private final MetricRepository metricRepository;

    public MetricServiceImpl(MetricRepository metricRepository) {
        super(metricRepository, Metric.class);
        this.metricRepository = metricRepository;
    }

    @Override
    public Future<Boolean> existsOneByMetric(String url) {
        return Future
            .fromCompletionStage(metricRepository.findByMetric(url))
            .map(Objects::nonNull);
    }
}
