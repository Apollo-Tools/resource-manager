package at.uibk.dps.rm.repository.log;

import at.uibk.dps.rm.entity.model.Log;
import at.uibk.dps.rm.repository.Repository;
import org.hibernate.reactive.stage.Stage.Session;

import java.util.List;
import java.util.concurrent.CompletionStage;

/**
 * Implements database operations for the log entity.
 *
 * @author matthi-g
 */
public class LogRepository extends Repository<Log> {

    /**
     * Create an instance.
     */
    public LogRepository() {
        super(Log.class);
    }

    /**
     * Find all logs by their deployment and account.
     *
     * @param session the database session
     * @param deploymentId the id of the deployment
     * @param accountId the id of the creator account
     * @return a CompletionStage that emits a list of all logs
     */
    public CompletionStage<List<Log>> findAllByDeploymentIdAndAccountId(Session session, long deploymentId,
            long accountId) {
        return session.createQuery("select distinct l from DeploymentLog dl " +
                "left join dl.log l " +
                "where dl.deployment.deploymentId=:deploymentId " +
                "and dl.deployment.createdBy.accountId=:accountId", entityClass)
            .setParameter("deploymentId", deploymentId)
            .setParameter("accountId", accountId)
            .getResultList();
    }
}
