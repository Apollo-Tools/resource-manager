package at.uibk.dps.rm.repository.deployment;

import at.uibk.dps.rm.entity.model.Deployment;
import at.uibk.dps.rm.repository.Repository;
import at.uibk.dps.rm.service.database.util.SessionManager;
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
     * Find a deployment by its id and the id of the creator
     *
     * @param sessionManager the database session manager
     * @param id the id of the deployment
     * @param accountId the id of the creator account
     * @return a Completable that emits the deployment if it exists, else null
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
}
