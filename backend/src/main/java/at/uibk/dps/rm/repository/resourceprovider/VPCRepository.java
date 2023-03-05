package at.uibk.dps.rm.repository.resourceprovider;

import at.uibk.dps.rm.entity.model.VPC;
import at.uibk.dps.rm.repository.Repository;
import org.hibernate.reactive.stage.Stage;

import java.util.concurrent.CompletionStage;

public class VPCRepository extends Repository<VPC> {
    public VPCRepository(Stage.SessionFactory sessionFactory) {
        super(sessionFactory, VPC.class);
    }

    public CompletionStage<VPC> findByRegionIdAndAccountId(long regionId, long accountId) {
        return sessionFactory.withSession(session ->
            session.createQuery("select vpc from VPC vpc " +
                    "where vpc.region.regionId=:regionId and vpc.createdBy.accountId=:accountId", entityClass)
                .setParameter("regionId", regionId)
                .setParameter("accountId", accountId)
                .getSingleResultOrNull()
        );
    }
}
