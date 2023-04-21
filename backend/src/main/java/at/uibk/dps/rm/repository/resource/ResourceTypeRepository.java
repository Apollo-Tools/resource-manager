package at.uibk.dps.rm.repository.resource;

import at.uibk.dps.rm.entity.model.ResourceType;
import at.uibk.dps.rm.repository.Repository;
import org.hibernate.reactive.stage.Stage;

import java.util.concurrent.CompletionStage;

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

    /**
     * Find a resource type by its name.
     *
     * @param resourceType the name of the resource type
     * @return a CompletionStage that emits the resource type if it exists, else null
     */
    public CompletionStage<ResourceType> findByResourceType(String resourceType) {
        return this.sessionFactory.withSession(session ->
            session.createQuery("from ResourceType where resourceType=:resourceType", entityClass)
                .setParameter("resourceType", resourceType)
                .getSingleResultOrNull()
        );
    }
}
