package at.uibk.dps.rm.repository.log;

import at.uibk.dps.rm.entity.model.Log;
import at.uibk.dps.rm.repository.Repository;
import org.hibernate.reactive.stage.Stage;

import java.util.List;
import java.util.concurrent.CompletionStage;

/**
 * Implements database operations for the log entity.
 *
 * @author matthi-g
 */
public class LogRepository extends Repository<Log> {

    /**
     * Create an instance from the sessionFactory.
     *
     * @param sessionFactory the session factory
     */
    public LogRepository(Stage.SessionFactory sessionFactory) {
        super(sessionFactory, Log.class);
    }

    /**
     * Find all logs by their deployment and account.
     *
     * @param deploymentId the id of the deployment
     * @param accountId the id of the creator account
     * @return a CompletionStage that emits a list of all logs
     */
    public CompletionStage<List<Log>> findAllByDeploymentIdAndAccountId(long deploymentId, long accountId) {
        return sessionFactory.withSession(session ->
            session.createQuery("select distinct l from DeploymentLog dl " +
                    "left join dl.log l " +
                    "where dl.deployment.deploymentId=:deploymentId " +
                    "and dl.deployment.createdBy.accountId=:accountId", entityClass)
                .setParameter("deploymentId", deploymentId)
                .setParameter("accountId", accountId)
                .getResultList()
        );
    }
}
