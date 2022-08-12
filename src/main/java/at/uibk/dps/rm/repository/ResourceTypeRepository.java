package at.uibk.dps.rm.repository;

import at.uibk.dps.rm.repository.Repository;
import at.uibk.dps.rm.entity.model.ResourceType;
import org.hibernate.reactive.stage.Stage;

import java.util.concurrent.CompletionStage;

public class ResourceTypeRepository extends Repository<ResourceType> {
    public ResourceTypeRepository(Stage.SessionFactory sessionFactory,
        Class<ResourceType> entityClass) {
        super(sessionFactory, entityClass);
    }

    public CompletionStage<ResourceType> findByResourceType(String resourceType) {
        return this.sessionFactory.withSession(session ->
            session.createQuery("from ResourceType where resourceType=:resourceType", entityClass)
                .setParameter("resourceType", resourceType)
                .getSingleResultOrNull()
        );
    }
}
