package at.uibk.dps.rm.repository.deployment;

import at.uibk.dps.rm.entity.model.Deployment;
import at.uibk.dps.rm.repository.Repository;
import org.hibernate.reactive.stage.Stage;

import java.util.List;
import java.util.concurrent.CompletionStage;

/**
 * Implements database operations for the deployment entity.
 *
 * @author matthi-g
 */
public class DeploymentRepository extends Repository<Deployment> {

    /**
     * Create an instance from the sessionFactory.
     *
     * @param sessionFactory the session factory
     */
    public DeploymentRepository(Stage.SessionFactory sessionFactory) {
        super(sessionFactory, Deployment.class);
    }

    /**
     * Find all deployments created by an account.
     *
     * @param accountId the id of the account
     * @return a CompletionStage that emits a list of all deployments
     */
    public CompletionStage<List<Deployment>> findAllByAccountId(long accountId) {
        return sessionFactory.withSession(session ->
            session.createQuery("select distinct d from Deployment d " +
                    "where d.createdBy.accountId=:accountId " +
                    "order by d.id", entityClass)
                .setParameter("accountId", accountId)
                .getResultList()
        );
    }

    /**
     * Find a deployment by its id and the id of the creator
     *
     * @param id the id of the deployment
     * @param accountId the id of the creator account
     * @return a CompletionStage that emits the deployment if it exists, else null
     */
    public CompletionStage<Deployment> findByIdAndAccountId(long id, long accountId) {
        return sessionFactory.withSession(session ->
            session.createQuery("select d from Deployment d " +
                    "where d.deploymentId=:id and d.createdBy.accountId=:accountId", entityClass)
                .setParameter("id", id)
                .setParameter("accountId", accountId)
                .getSingleResultOrNull()
        );
    }
}
