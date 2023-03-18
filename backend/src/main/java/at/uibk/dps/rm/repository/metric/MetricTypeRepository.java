package at.uibk.dps.rm.repository.metric;

import at.uibk.dps.rm.entity.model.MetricType;
import at.uibk.dps.rm.repository.Repository;
import org.hibernate.reactive.stage.Stage;

public class MetricTypeRepository extends Repository<MetricType> {
    public MetricTypeRepository(Stage.SessionFactory sessionFactory) {
        super(sessionFactory, MetricType.class);
    }
}