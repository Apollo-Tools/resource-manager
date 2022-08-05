package at.uibk.dps.rm.repository.property;

import at.uibk.dps.rm.repository.Repository;
import at.uibk.dps.rm.repository.property.entity.Property;
import org.hibernate.reactive.stage.Stage;

public class PropertyRepository extends Repository<Property> {

    public PropertyRepository(Stage.SessionFactory sessionFactory,
                              Class<Property> entityClass) {
        super(sessionFactory, entityClass);
    }
}
