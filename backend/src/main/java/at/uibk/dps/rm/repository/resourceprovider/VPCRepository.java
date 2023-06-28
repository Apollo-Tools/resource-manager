package at.uibk.dps.rm.repository.resourceprovider;

import at.uibk.dps.rm.entity.model.VPC;
import at.uibk.dps.rm.repository.Repository;
import org.hibernate.reactive.stage.Stage.Session;

import java.util.List;
import java.util.concurrent.CompletionStage;

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
     * @param session the database session
     * @param id the id of the vpc
     * @return a CompletionStage that emits the vpc if it exists, else null
     */
    public CompletionStage<VPC> findByIdAndFetch(Session session, long id) {
        return session.createQuery("from VPC vpc " +
                "left join fetch vpc.region " +
                "left join fetch vpc.region.resourceProvider " +
                "where vpc.vpcId =:id", entityClass)
            .setParameter("id", id)
            .getSingleResultOrNull();
    }

    /**
     * Find a vpc by its region and creator account.
     *
     * @param session the database session
     * @param regionId the id of the region
     * @param accountId the id of the creator account
     * @return a CompletionStage that emits the vpc if it exists, else null
     */
    public CompletionStage<VPC> findByRegionIdAndAccountId(Session session, long regionId, long accountId) {
        return session.createQuery("from VPC vpc " +
                "left join fetch vpc.region " +
                "left join fetch vpc.region.resourceProvider " +
                "where vpc.region.regionId=:regionId and vpc.createdBy.accountId=:accountId", entityClass)
            .setParameter("regionId", regionId)
            .setParameter("accountId", accountId)
            .getSingleResultOrNull();
    }

    /**
     * Find all vpcs and fetch the region and resource provider.
     *
     * @param session the database session
     * @return a CompletionStage that emits the vpc if it exists, else null
     */
    public CompletionStage<List<VPC>> findAllAndFetch(Session session) {
        return session.createQuery("select distinct vpc from VPC vpc " +
                "left join fetch vpc.region reg " +
                "left join fetch reg.resourceProvider", entityClass)
            .getResultList();
    }
}
