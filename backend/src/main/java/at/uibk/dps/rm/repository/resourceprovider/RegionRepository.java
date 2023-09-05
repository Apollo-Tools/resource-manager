package at.uibk.dps.rm.repository.resourceprovider;

import at.uibk.dps.rm.entity.model.Region;
import at.uibk.dps.rm.repository.Repository;
import at.uibk.dps.rm.service.database.util.SessionManager;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

import java.util.List;

/**
 * Implements database operations for the region entity.
 *
 * @author matthi-g
 */
public class RegionRepository extends Repository<Region> {

    /**
     * Create an instance.
     */
    public RegionRepository() {
        super(Region.class);
    }

    /**
     * Finda a region by its id and fetch the resource provider and environment.
     *
     * @param sessionManager the database session manager
     * @param id the id of the resource provider
     * @return a Maybe that emits the region if it exists, else null
     */
    public Maybe<Region> findByIdAndFetch(SessionManager sessionManager, long id) {
        return Maybe.fromCompletionStage(sessionManager.getSession()
            .createQuery(
                "from Region r " +
                "left join fetch r.resourceProvider rp " +
                "left join fetch rp.environment " +
                "where r.regionId =:id", entityClass)
            .setParameter("id", id)
            .getSingleResultOrNull()
        );
    }

    /**
     * Fine a region by its name and resource provider.
     *
     * @param sessionManager the database session manager
     * @param name the name of the region
     * @param providerId the id of the resource provider
     * @return a CompletionStage that emits the region if it exists, else null
     */
    public Maybe<Region> findOneByNameAndProviderId(SessionManager sessionManager, String name, long providerId) {
        return Maybe.fromCompletionStage(sessionManager.getSession()
            .createQuery(
                "from Region r " +
                "left join fetch r.resourceProvider rp " +
                "left join fetch rp.environment " +
                "where r.name=:name and rp.providerId =:providerId", entityClass)
            .setParameter("name", name)
            .setParameter("providerId", providerId)
            .getSingleResultOrNull()
        );
    }

    /**
     * Find all regions and fetch the resource provider and environment.
     *
     * @param sessionManager the database session manager
     * @return a Single that emits a list of all regions
     */
    public Single<List<Region>> findAllAndFetch(SessionManager sessionManager) {
        return Single.fromCompletionStage(sessionManager.getSession()
            .createQuery("select distinct r from Region r " +
                "left join fetch r.resourceProvider rp " +
                "left join fetch rp.environment " +
                "order by rp.provider", entityClass)
            .getResultList()
        );
    }

    /**
     * Find all regions by their resource provider.
     *
     * @param sessionManager the database session
     * @param providerId the id of the resource provider
     * @return a Single that emits a list of all regions
     */
    public Single<List<Region>> findAllByProviderId(SessionManager sessionManager, long providerId) {
        return Single.fromCompletionStage(sessionManager.getSession()
            .createQuery("select distinct r from Region r " +
                "where r.resourceProvider.providerId=:providerId", entityClass)
            .setParameter("providerId", providerId)
            .getResultList()
        );
    }

    /**
     * Find all regions by their supported platform.
     *
     * @param sessionManager the database session manager
     * @param platformId the id of the platform
     * @return a Single that emits a list of all regions
     */
    public Single<List<Region>> findAllByPlatformId(SessionManager sessionManager, long platformId) {
        return Single.fromCompletionStage(sessionManager.getSession()
            .createQuery("SELECT reg FROM Region reg " +
                "LEFT JOIN fetch reg.resourceProvider rp " +
                "LEFT JOIN rp.providerPlatforms pp " +
                "WHERE pp.platform.platformId = :platformId", entityClass)
            .setParameter("platformId", platformId)
            .getResultList()
        );
    }

    /**
     * Find region by its id and the supported platform.
     *
     * @param sessionManager the database session manager
     * @param regionId the id of the region
     * @param platformId the id of the platform
     * @return a Maybe that emits a list of all regions
     */
    public Maybe<Region> findByRegionIdAndPlatformId(SessionManager sessionManager, long regionId, long platformId) {
        return Maybe.fromCompletionStage(sessionManager.getSession()
            .createQuery("SELECT reg FROM Region reg " +
                "LEFT JOIN reg.resourceProvider rp " +
                "LEFT JOIN rp.providerPlatforms pp " +
                "WHERE reg.regionId=:regionId and pp.platform.platformId=:platformId", entityClass)
            .setParameter("regionId", regionId)
            .setParameter("platformId", platformId)
            .getSingleResultOrNull()
        );
    }
}
