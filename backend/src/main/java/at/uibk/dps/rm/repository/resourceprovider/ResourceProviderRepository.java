package at.uibk.dps.rm.repository.resourceprovider;

import at.uibk.dps.rm.entity.model.ResourceProvider;
import at.uibk.dps.rm.repository.Repository;
import org.hibernate.reactive.stage.Stage;

import java.util.concurrent.CompletionStage;

/**
 * Implements database operations for the resource_provider entity.
 *
 * @author matthi-g
 */
public class ResourceProviderRepository extends Repository<ResourceProvider> {

    /**
     * Create an instance from the sessionFactory.
     *
     * @param sessionFactory the session factory
     */
    public ResourceProviderRepository(Stage.SessionFactory sessionFactory) {
        super(sessionFactory, ResourceProvider.class);
    }

    /**
     * Find a resource provider by its name.
     *
     * @param provider the name of the resource provider
     * @return a CompletionStage that emits the resource provider if it exists, else null
     */
    public CompletionStage<ResourceProvider> findByProvider(String provider) {
        return this.sessionFactory.withSession(session ->
            session.createQuery("from ResourceProvider where provider=:provider", entityClass)
                .setParameter("provider", provider)
                .getSingleResultOrNull()
        );
    }
}
