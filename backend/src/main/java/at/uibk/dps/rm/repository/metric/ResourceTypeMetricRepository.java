package at.uibk.dps.rm.repository.metric;

import at.uibk.dps.rm.entity.model.ResourceTypeMetric;
import at.uibk.dps.rm.repository.Repository;
import org.hibernate.reactive.stage.Stage;

public class ResourceTypeMetricRepository extends Repository<ResourceTypeMetric> {
    public ResourceTypeMetricRepository(Stage.SessionFactory sessionFactory) {
        super(sessionFactory, ResourceTypeMetric.class);
    }
}
