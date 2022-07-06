package at.uibk.dps.rm.repository.metric;

import at.uibk.dps.rm.repository.Repository;
import at.uibk.dps.rm.repository.metric.entity.MetricValue;
import org.hibernate.reactive.stage.Stage;

public class MetricValueRepository extends Repository<MetricValue> {
    public MetricValueRepository(Stage.SessionFactory sessionFactory, Class<MetricValue> entityClass) {
        super(sessionFactory, entityClass);
    }
}
