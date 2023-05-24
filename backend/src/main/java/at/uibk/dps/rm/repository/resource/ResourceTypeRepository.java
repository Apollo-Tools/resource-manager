package at.uibk.dps.rm.repository.resource;

import at.uibk.dps.rm.entity.model.ResourceType;
import at.uibk.dps.rm.repository.Repository;
import org.hibernate.reactive.stage.Stage;

/**
 * Implements database operations for the resource_type entity.
 *
 * @author matthi-g
 */
public class ResourceTypeRepository extends Repository<ResourceType> {

    /**
     * Create an instance from the sessionFactory.
     *
     * @param sessionFactory the session factory
     */
    public ResourceTypeRepository(Stage.SessionFactory sessionFactory) {
        super(sessionFactory, ResourceType.class);
    }
}
