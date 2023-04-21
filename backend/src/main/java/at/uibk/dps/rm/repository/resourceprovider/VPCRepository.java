package at.uibk.dps.rm.repository.resourceprovider;

import at.uibk.dps.rm.entity.model.VPC;
import at.uibk.dps.rm.repository.Repository;
import org.hibernate.reactive.stage.Stage;

import java.util.List;
import java.util.concurrent.CompletionStage;

/**
 * Implements database operations for the vpc entity.
 *
 * @author matthi-g
 */
public class VPCRepository extends Repository<VPC> {

    /**
     * Create an instance from the sessionFactory.
     *
     * @param sessionFactory the session factory
     */
    public VPCRepository(Stage.SessionFactory sessionFactory) {
        super(sessionFactory, VPC.class);
    }

    public CompletionStage<VPC> findByIdAndFetch(long id) {
        return sessionFactory.withSession(session -> session.createQuery(
                "from VPC vpc " +
                    "left join fetch vpc.region " +
                    "left join fetch vpc.region.resourceProvider " +
                    "where vpc.vpcId =:id", entityClass)
            .setParameter("id", id)
            .getSingleResultOrNull()
        );
    }

    public CompletionStage<VPC> findByRegionIdAndAccountId(long regionId, long accountId) {
        return sessionFactory.withSession(session ->
            session.createQuery("from VPC vpc " +
                    "left join fetch vpc.region " +
                    "left join fetch vpc.region.resourceProvider " +
                    "where vpc.region.regionId=:regionId and vpc.createdBy.accountId=:accountId", entityClass)
                .setParameter("regionId", regionId)
                .setParameter("accountId", accountId)
                .getSingleResultOrNull()
        );
    }

    public CompletionStage<List<VPC>> findAllAndFetch() {
        return sessionFactory.withSession(session ->
            session.createQuery("select distinct vpc from VPC vpc " +
                        "left join fetch vpc.region reg " +
                        "left join fetch reg.resourceProvider",
                    entityClass)
                .getResultList()
        );
    }
}
