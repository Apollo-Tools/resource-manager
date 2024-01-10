package at.uibk.dps.rm.repository.deployment;

import at.uibk.dps.rm.entity.deployment.output.TFOutputValue;
import at.uibk.dps.rm.entity.model.FunctionDeployment;
import at.uibk.dps.rm.repository.Repository;
import at.uibk.dps.rm.service.database.util.SessionManager;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

import java.util.List;

/**
 * Implements database operations for the function_deployment entity.
 *
 * @author matthi-g
 */
public class FunctionDeploymentRepository extends Repository<FunctionDeployment> {
    /**
     * Create an instance.
     */
    public FunctionDeploymentRepository() {
        super(FunctionDeployment.class);
    }

    /**
     * Find all function deployments by their deployment
     *
     * @param sessionManager the database session manager
     * @param deploymentId the id of the deployment
     * @return a Single that emits a list of all function deployments
     */
    public Single<List<FunctionDeployment>> findAllByDeploymentId(SessionManager sessionManager, long deploymentId) {
        return Single.fromCompletionStage(sessionManager.getSession()
            .createQuery("select distinct fd from FunctionDeployment fd " +
                "left join fetch fd.function f " +
                "left join fetch f.functionType " +
                "left join fetch f.runtime " +
                "left join fetch fd.resource r " +
                "left join fetch r.metricValues mv " +
                "left join fetch mv.metric " +
                "left join fetch r.platform p " +
                "left join fetch p.resourceType " +
                "left join fetch r.region reg " +
                "left join fetch reg.resourceProvider rp " +
                "left join fetch rp.environment " +
                "left join fetch fd.status " +
                "where fd.deployment.deploymentId=:deploymentId", entityClass)
            .setParameter("deploymentId", deploymentId)
            .getResultList()
        );
    }

    /**
     * Update the trigger url of a resource deployment by its id.
     *
     * @param sessionManager the database session manager
     * @param id the id of the resource deployment
     * @param rmTriggerUrl the new rm trigger url
     * @param tfOutputValue the terraform output
     * @return a Completable
     */
    public Completable updateTriggerUrls(SessionManager sessionManager, long id, String rmTriggerUrl,
            TFOutputValue tfOutputValue) {
        return Single.fromCompletionStage(sessionManager.getSession()
            .createQuery("update FunctionDeployment fd " +
                "set rmTriggerUrl=:rmTriggerUrl, directTriggerUrl=:directTriggerUrl, baseUrl=:baseUrl , " +
                "path=:path, metricsPort=:metricsPort, openfaasPort=:openFaasPort " +
                "where fd.resourceDeploymentId=:id")
            .setParameter("rmTriggerUrl", rmTriggerUrl)
            .setParameter("directTriggerUrl", tfOutputValue.getFullUrl())
            .setParameter("baseUrl", tfOutputValue.getBaseUrl())
            .setParameter("path", tfOutputValue.getPath())
            .setParameter("metricsPort", tfOutputValue.getMetricsPort())
            .setParameter("openFaasPort", tfOutputValue.getOpenfaasPort())
            .setParameter("id", id)
            .executeUpdate()
        ).ignoreElement();
    }

    /**
     * Find a function deployment by its id and the id of the creator and fetch the status
     *
     * @param sessionManager the database session manager
     * @param id the id of the function deployment
     * @param accountId the id of the creator account
     * @return a Maybe that emits the function deployment if it exists, else null
     */
    @Override
    public Maybe<FunctionDeployment> findByIdAndAccountId(SessionManager sessionManager, long id, long accountId) {
        return Maybe.fromCompletionStage(sessionManager.getSession()
            .createQuery("from FunctionDeployment fd " +
                "left join fetch fd.status " +
                "left join fetch fd.function " +
                "left join fetch fd.resource " +
                "where fd.resourceDeploymentId=:id and fd.deployment.createdBy.accountId=:accountId", entityClass)
            .setParameter("id", id)
            .setParameter("accountId", accountId)
            .getSingleResultOrNull()
        );
    }
}
