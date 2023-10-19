package at.uibk.dps.rm.repository.deployment;

import at.uibk.dps.rm.entity.deployment.DeploymentStatusValue;
import at.uibk.dps.rm.entity.model.ResourceDeployment;
import at.uibk.dps.rm.repository.Repository;
import at.uibk.dps.rm.service.database.util.SessionManager;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

import java.util.List;

/**
 * Implements database operations for the resource_deployment entity.
 *
 * @author matthi-g
 */
public class ResourceDeploymentRepository extends Repository<ResourceDeployment> {
    /**
     * Create an instance.
     */
    public ResourceDeploymentRepository() {
        super(ResourceDeployment.class);
    }

    /**
     * Find all resource deployments by their deployment and fetch the deployment status
     *
     * @param sessionManager the database session manager
     * @param deploymentId the id of the deployment
     * @return a Single that emits a list of all resource deployments
     */
    public Single<List<ResourceDeployment>> findAllByDeploymentIdAndFetch(SessionManager sessionManager,
            long deploymentId) {
        return Single.fromCompletionStage(sessionManager.getSession()
            .createQuery("select distinct rd from ResourceDeployment rd " +
                "left join fetch rd.status " +
                "where rd.deployment.deploymentId=:deploymentId", entityClass)
            .setParameter("deploymentId", deploymentId)
            .getResultList()
        );
    }

    /**
     * Update the trigger url of a resource deployment by its id.
     *
     * @param sessionManager the database session manager
     * @param id the id of the resource deployment
     * @param triggerUrl the new trigger url
     * @return a Completable
     */
    public Completable updateTriggerUrl(SessionManager sessionManager, long id, String triggerUrl) {
        return Single.fromCompletionStage(sessionManager.getSession()
            .createQuery("update ResourceDeployment rd " +
                "set triggerUrl=:triggerUrl, isDeployed=true " +
                "where rd.resourceDeploymentId=:id")
            .setParameter("triggerUrl", triggerUrl)
            .setParameter("id", id)
            .executeUpdate()
        ).ignoreElement();
    }

    /**
     * Update the resource deployment status to the status value by its deployment.
     *
     * @param sessionManager the database session manager
     * @param deploymentId the id of the deployment
     * @param statusValue the new status value
     * @return a Completable
     */
    public Completable updateDeploymentStatusByDeploymentId(SessionManager sessionManager, long deploymentId,
            DeploymentStatusValue statusValue) {
        return Single.fromCompletionStage(sessionManager.getSession()
            .createQuery("update ResourceDeployment rd " +
                "set status.statusId=" +
                "(select rds.statusId from ResourceDeploymentStatus rds where rds.statusValue=:statusValue)" +
                "where rd.deployment.deploymentId=:deploymentId")
            .setParameter("deploymentId", deploymentId)
            .setParameter("statusValue", statusValue.name())
            .executeUpdate()
        ).ignoreElement();
    }
}
