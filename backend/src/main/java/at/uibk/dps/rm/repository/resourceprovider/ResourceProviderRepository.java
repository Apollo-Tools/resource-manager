package at.uibk.dps.rm.repository.resourceprovider;

import at.uibk.dps.rm.entity.model.ResourceProvider;
import at.uibk.dps.rm.repository.Repository;
import org.hibernate.reactive.stage.Stage.Session;

import java.util.List;
import java.util.concurrent.CompletionStage;

/**
 * Implements database operations for the resource_provider entity.
 *
 * @author matthi-g
 */
public class ResourceProviderRepository extends Repository<ResourceProvider> {

    /**
     * Create an instance.
     */
    public ResourceProviderRepository() {
        super(ResourceProvider.class);
    }

    /**
     * Find a platform by its id and fetch the environment.
     *
     * @param session the database session
     * @param id the id of the resource provider
     * @return a CompletionStage that emits the region if it exists, else null
     */
    public CompletionStage<ResourceProvider> findByIdAndFetch(Session session, long id) {
        return session.createQuery("from ResourceProvider rp " +
                "left join fetch rp.environment " +
                "where rp.providerId =:id", entityClass)
            .setParameter("id", id)
            .getSingleResultOrNull();
    }

    /**
     * Find all platforms and fetch the environment.
     *
     * @param session the database session
     * @return a CompletionStage that emits a list of all regions
     */
    public CompletionStage<List<ResourceProvider>> findAllAndFetch(Session session) {
        return session.createQuery("select distinct rp from ResourceProvider rp " +
                "left join fetch rp.environment " +
                "order by rp.provider", entityClass)
            .getResultList();
    }
}
