package at.uibk.dps.rm.service.database.metric;

import at.uibk.dps.rm.entity.model.Metric;
import at.uibk.dps.rm.repository.metric.MetricRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import at.uibk.dps.rm.service.database.util.SessionManagerProvider;

/**
 * This is the implementation of the {@link MetricService}.
 *
 * @author matthi-g
 */
public class MetricServiceImpl extends DatabaseServiceProxy<Metric> implements MetricService {

    /**
     * Create an instance from the metricRepository.
     *
     * @param repository the metric repository
     */
    public MetricServiceImpl(MetricRepository repository, SessionManagerProvider smProvider) {
        super(repository, Metric.class, smProvider);
    }
}
