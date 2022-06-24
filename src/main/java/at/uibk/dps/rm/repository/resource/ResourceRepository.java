package at.uibk.dps.rm.repository.resource;

import at.uibk.dps.rm.repository.Repository;
import at.uibk.dps.rm.repository.resource.entity.Resource;
import at.uibk.dps.rm.repository.resource.entity.ResourceType;
import org.hibernate.reactive.stage.Stage;

public class ResourceRepository  extends Repository<Resource> {

    public ResourceRepository(Stage.SessionFactory sessionFactory,
        Class<Resource> entityClass) {
        super(sessionFactory, entityClass);
    }
}
