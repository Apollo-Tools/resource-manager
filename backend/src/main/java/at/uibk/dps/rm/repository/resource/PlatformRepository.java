package at.uibk.dps.rm.repository.resource;

import at.uibk.dps.rm.entity.model.Platform;
import at.uibk.dps.rm.repository.Repository;
import at.uibk.dps.rm.service.database.util.SessionManager;
import io.reactivex.rxjava3.core.Single;

import java.util.List;

/**
 * Implements database operations for the platform entity.
 *
 * @author matthi-g
 */
public class PlatformRepository extends Repository<Platform> {

    /**
     * Create an instance.
     */
    public PlatformRepository() {
        super(Platform.class);
    }

    /**
     * Find all platforms and fetch the resource types.
     *
     * @param sessionManager the database session manager
     * @return a Single that emits a list of all platforms
     */
    public Single<List<Platform>> findAllAndFetch(SessionManager sessionManager) {
        return Single.fromCompletionStage(sessionManager.getSession()
            .createQuery("from Platform p left join fetch p.resourceType", entityClass)
            .getResultList()
        );
    }

    /**
     * Find all platforms by resource provider.
     *
     * @param sessionManager the database session manager
     * @param resourceProvider the name of the resource provider
     * @return a Single that emits a list of all found platforms
     */
    public Single<List<Platform>> findAllByResourceProvider(SessionManager sessionManager, String resourceProvider) {
        return Single.fromCompletionStage(sessionManager.getSession()
            .createQuery(
                "select distinct pp.platform from ProviderPlatform pp " +
                    "where pp.resourceProvider.provider=:resourceProvider", Platform.class)
            .setParameter("resourceProvider", resourceProvider)
            .getResultList()
        );
    }
}
