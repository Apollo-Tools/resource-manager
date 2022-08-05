package at.uibk.dps.rm.repository.property;

import at.uibk.dps.rm.repository.Repository;
import at.uibk.dps.rm.repository.property.entity.PropertyValue;
import org.hibernate.reactive.stage.Stage;

public class PropertyValueRepository extends Repository<PropertyValue> {
    public PropertyValueRepository(Stage.SessionFactory sessionFactory, Class<PropertyValue> entityClass) {
        super(sessionFactory, entityClass);
    }
}
