package at.uibk.dps.rm.rx.repository.resourceprovider;

import at.uibk.dps.rm.entity.model.VPC;
import at.uibk.dps.rm.rx.repository.Repository;
import at.uibk.dps.rm.rx.service.database.util.SessionManager;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

import java.util.List;

/**
 * Implements database operations for the vpc entity.
 *
 * @author matthi-g
 */
public class VPCRepository extends Repository<VPC> {

    /**
     * Create an instance.
     */
    public VPCRepository() {
        super(VPC.class);
    }

    /**
     * Find a vpc by its id and fetch the region and resource provider.
     *
     * @param sessionManager the database session manager
     * @param id the id of the vpc
     * @return a Maybe that emits the vpc if it exists, else null
     */
    public Maybe<VPC> findByIdAndFetch(SessionManager sessionManager, long id) {
        return Maybe.fromCompletionStage(sessionManager.getSession()
            .createQuery("from VPC vpc " +
                "left join fetch vpc.region " +
                "left join fetch vpc.region.resourceProvider " +
                "where vpc.vpcId =:id", entityClass)
            .setParameter("id", id)
            .getSingleResultOrNull()
        );
    }

    /**
     * Find a vpc by its region and creator account.
     *
     * @param sessionManager the database session manager
     * @param regionId the id of the region
     * @param accountId the id of the creator account
     * @return a Maybe that emits the vpc if it exists, else null
     */
    public Maybe<VPC> findByRegionIdAndAccountId(SessionManager sessionManager, long regionId,
            long accountId) {
        return Maybe.fromCompletionStage(sessionManager.getSession()
            .createQuery("from VPC vpc " +
                "left join fetch vpc.region " +
                "left join fetch vpc.region.resourceProvider " +
                "where vpc.region.regionId=:regionId and vpc.createdBy.accountId=:accountId", entityClass)
            .setParameter("regionId", regionId)
            .setParameter("accountId", accountId)
            .getSingleResultOrNull()
        );
    }

    /**
     * Find a vpc by its id and creator account.
     *
     * @param sessionManager the database session manager
     * @param vpcId the id of the vpc
     * @param accountId the id of the creator account
     * @return a Maybe that emits the vpc if it exists, else null
     */
    public Maybe<VPC> findByIdAndAccountId(SessionManager sessionManager, long vpcId, long accountId) {
        return Maybe.fromCompletionStage(sessionManager.getSession()
            .createQuery("from VPC vpc " +
                "where vpc.vpcId=:vpcId and vpc.createdBy.accountId=:accountId", entityClass)
            .setParameter("vpcId", vpcId)
            .setParameter("accountId", accountId)
            .getSingleResultOrNull()
        );
    }

    /**
     * Find all vpcs by account id and fetch the region and resource provider.
     *
     * @param sessionManager the database session manager
     * @param accountId the id of the creator
     * @return a Single that emits the found vpcs
     */
    public Single<List<VPC>> findAllByAccountIdAndFetch(SessionManager sessionManager, long accountId) {
        return Single.fromCompletionStage(sessionManager.getSession()
            .createQuery("select distinct vpc from VPC vpc " +
                "left join fetch vpc.region reg " +
                "left join fetch reg.resourceProvider " +
                "where vpc.createdBy.accountId=:accountId", entityClass)
            .setParameter("accountId", accountId)
            .getResultList()
        );
    }
}
