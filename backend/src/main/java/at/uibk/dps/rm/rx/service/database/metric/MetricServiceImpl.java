package at.uibk.dps.rm.rx.service.database.metric;

import at.uibk.dps.rm.entity.model.Metric;
import at.uibk.dps.rm.rx.repository.metric.MetricRepository;
import at.uibk.dps.rm.rx.service.database.DatabaseServiceProxy;
import org.hibernate.reactive.stage.Stage;

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
    public MetricServiceImpl(MetricRepository repository, Stage.SessionFactory sessionFactory) {
        super(repository, Metric.class, sessionFactory);
    }
}
