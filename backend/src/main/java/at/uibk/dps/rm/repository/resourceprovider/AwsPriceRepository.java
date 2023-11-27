package at.uibk.dps.rm.repository.resourceprovider;

import at.uibk.dps.rm.entity.model.AwsPrice;
import at.uibk.dps.rm.repository.Repository;
import at.uibk.dps.rm.service.database.util.SessionManager;
import io.reactivex.rxjava3.core.Maybe;

/**
 * Implements database operations for the aws price entity.
 *
 * @author matthi-g
 */
public class AwsPriceRepository extends Repository<AwsPrice> {
    /**
     * Create an instance.
     */
    public AwsPriceRepository() {
        super(AwsPrice.class);
    }

    /**
     * Find an aws price by its region, platform and instance type.
     *
     * @param sessionManager the database session manager
     * @param regionId the id of the region
     * @param platformId the id of the platform
     * @param instanceType the instance type
     * @return a Maybe that emits the aws price if it exists, else null
     */
    public Maybe<AwsPrice> findByRegionIdPlatformIdAndInstanceType(SessionManager sessionManager, long regionId,
            long platformId, String instanceType) {
        return Maybe.fromCompletionStage(sessionManager.getSession()
            .createQuery("from AwsPrice p " +
                "where p.region.regionId=:regionId and p.platform.platformId=:platformId and " +
                    "p.instanceType=:instanceType", entityClass)
            .setParameter("regionId", regionId)
            .setParameter("platformId", platformId)
            .setParameter("instanceType", instanceType)
            .getSingleResultOrNull()
        );
    }
}
