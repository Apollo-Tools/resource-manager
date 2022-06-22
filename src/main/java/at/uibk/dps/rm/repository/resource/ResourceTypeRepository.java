package at.uibk.dps.rm.repository.resource;

import at.uibk.dps.rm.repository.Repository;
import at.uibk.dps.rm.repository.resource.entity.ResourceType;
import org.hibernate.reactive.stage.Stage;

public class ResourceTypeRepository extends Repository<ResourceType> {
    public ResourceTypeRepository(Stage.SessionFactory sessionFactory,
        Class<ResourceType> entityClass) {
        super(sessionFactory, entityClass);
    }
}
