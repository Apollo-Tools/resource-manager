package at.uibk.dps.rm.repository.deployment;

import at.uibk.dps.rm.entity.deployment.DeploymentStatusValue;
import at.uibk.dps.rm.entity.model.Deployment;
import at.uibk.dps.rm.repository.Repository;
import at.uibk.dps.rm.service.database.util.SessionManager;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

import java.util.List;

/**
 * Implements database operations for the deployment entity.
 *
 * @author matthi-g
 */
public class DeploymentRepository extends Repository<Deployment> {

    /**
     * Create an instance.
     */
    public DeploymentRepository() {
        super(Deployment.class);
    }

    /**
     * Find all deployments created by an account.
     *
     * @param sessionManager the database session manager
     * @param accountId the id of the account
     * @return a Single that emits a list of all deployments
     */
    public Single<List<Deployment>> findAllByAccountId(SessionManager sessionManager, long accountId) {
        return Single.fromCompletionStage(sessionManager.getSession()
            .createQuery("select distinct d from Deployment d " +
                "where d.createdBy.accountId=:accountId " +
                "order by d.id", entityClass)
            .setParameter("accountId", accountId)
            .getResultList()
        );
    }


    /**
     * Find all deployments that are active and selected for alerting.
     *
     * @param sessionManager the database session manager
     * @return a Single that emits a list of all found deployments
     */
    public Single<List<Deployment>> findAllActiveWithAlerting(SessionManager sessionManager) {
        return Single.fromCompletionStage(sessionManager.getSession()
            .createQuery("select distinct d from ResourceDeployment rd " +
                "left join rd.deployment d " +
                "left join fetch d.ensemble e " +
                "where d.ensemble!=null and rd.status.statusValue=:deployedStatus " +
                "order by d.id", entityClass)
            .setParameter("deployedStatus", DeploymentStatusValue.DEPLOYED.getValue())
            .getResultList()
        );
    }

    /**
     * Find a deployment by its id and the id of the creator
     *
     * @param sessionManager the database session manager
     * @param id the id of the deployment
     * @param accountId the id of the creator account
     * @return a Maybe that emits the deployment if it exists, else null
     */
    public Maybe<Deployment> findByIdAndAccountId(SessionManager sessionManager, long id, long accountId) {
        return Maybe.fromCompletionStage(sessionManager.getSession()
            .createQuery("select d from Deployment d " +
                "where d.deploymentId=:id and d.createdBy.accountId=:accountId", entityClass)
            .setParameter("id", id)
            .setParameter("accountId", accountId)
            .getSingleResultOrNull()
        );
    }

    /**
     * Set deployment finished time to current timestamp.
     *
     * @param sessionManager the database session manager
     * @param id the id of the deployment
     * @return a Completable
     */
    public Completable setDeploymentFinishedTime(SessionManager sessionManager, long id) {
        return Single.fromCompletionStage(sessionManager.getSession()
            .createQuery("update Deployment d set d.finishedAt = current_timestamp where d.deploymentId=:deploymentId")
            .setParameter("deploymentId", id)
            .executeUpdate()
        ).ignoreElement();
    }
}
