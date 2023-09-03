package at.uibk.dps.rm.rx.repository.resourceprovider;

import at.uibk.dps.rm.entity.model.ResourceProvider;
import at.uibk.dps.rm.rx.repository.Repository;
import at.uibk.dps.rm.rx.service.database.util.SessionManager;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

import java.util.List;

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
     * @param sessionManager the database session manager
     * @param id the id of the resource provider
     * @return a Maybe that emits the region if it exists, else null
     */
    public Maybe<ResourceProvider> findByIdAndFetch(SessionManager sessionManager, long id) {
        return Maybe.fromCompletionStage(sessionManager.getSession()
            .createQuery("from ResourceProvider rp " +
                "left join fetch rp.environment " +
                "where rp.providerId =:id", entityClass)
            .setParameter("id", id)
            .getSingleResultOrNull()
        );
    }

    /**
     * Find all platforms and fetch the environment.
     *
     * @param sessionManager the database session manager
     * @return a Single that emits a list of all regions
     */
    public Single<List<ResourceProvider>> findAllAndFetch(SessionManager sessionManager) {
        return Single.fromCompletionStage(sessionManager.getSession()
            .createQuery("select distinct rp from ResourceProvider rp " +
                "left join fetch rp.environment " +
                "order by rp.provider", entityClass)
            .getResultList()
        );
    }
}
