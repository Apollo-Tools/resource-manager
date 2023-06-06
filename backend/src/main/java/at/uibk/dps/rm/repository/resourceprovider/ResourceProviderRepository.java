package at.uibk.dps.rm.repository.resourceprovider;

import at.uibk.dps.rm.entity.model.ResourceProvider;
import at.uibk.dps.rm.repository.Repository;
import org.hibernate.reactive.stage.Stage;

import java.util.List;
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
     * Find a platform by its id and fetch the environment.
     *
     * @param id the id of the resource provider
     * @return a CompletionStage that emits the region if it exists, else null
     */
    public CompletionStage<ResourceProvider> findByIdAndFetch(long id) {
        return sessionFactory.withSession(session -> session.createQuery(
                "from ResourceProvider rp " +
                    "left join fetch rp.environment " +
                    "where rp.providerId =:id", entityClass)
            .setParameter("id", id)
            .getSingleResultOrNull()
        );
    }

    /**
     * Find all platforms and fetch the environment.
     *
     * @return a CompletionStage that emits a list of all regions
     */
    public CompletionStage<List<ResourceProvider>> findAllAndFetch() {
        return sessionFactory.withSession(session ->
            session.createQuery("select distinct rp from ResourceProvider rp " +
                    "left join fetch rp.environment", entityClass)
                .getResultList()
        );
    }
}
