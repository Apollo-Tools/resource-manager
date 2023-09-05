package at.uibk.dps.rm.repository.log;

import at.uibk.dps.rm.entity.model.Log;
import at.uibk.dps.rm.repository.Repository;
import at.uibk.dps.rm.service.database.util.SessionManager;
import io.reactivex.rxjava3.core.Single;

import java.util.List;

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
     * @param sessionManager the database session manager
     * @param deploymentId the id of the deployment
     * @param accountId the id of the creator account
     * @return a Single that emits a list of all logs
     */
    public Single<List<Log>> findAllByDeploymentIdAndAccountId(SessionManager sessionManager, long deploymentId,
            long accountId) {
        return Single.fromCompletionStage(sessionManager.getSession()
            .createQuery("select distinct l from DeploymentLog dl " +
                "left join dl.log l " +
                "where dl.deployment.deploymentId=:deploymentId " +
                "and dl.deployment.createdBy.accountId=:accountId", entityClass)
            .setParameter("deploymentId", deploymentId)
            .setParameter("accountId", accountId)
            .getResultList()
        );
    }
}
