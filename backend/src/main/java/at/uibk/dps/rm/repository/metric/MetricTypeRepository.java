package at.uibk.dps.rm.repository.metric;

import at.uibk.dps.rm.entity.model.MetricType;
import at.uibk.dps.rm.repository.Repository;
import org.hibernate.reactive.stage.Stage;

/**
 * Implements database operations for the metric_type entity.
 *
 * @author matthi-g
 */
public class MetricTypeRepository extends Repository<MetricType> {

    /**
     * Create an instance from the sessionFactory.
     *
     * @param sessionFactory the session factory
     */
    public MetricTypeRepository(Stage.SessionFactory sessionFactory) {
        super(sessionFactory, MetricType.class);
    }
}
