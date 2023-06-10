package at.uibk.dps.rm.repository.deployment;

import at.uibk.dps.rm.entity.deployment.DeploymentStatusValue;
import at.uibk.dps.rm.entity.model.ResourceDeployment;
import at.uibk.dps.rm.repository.Repository;
import org.hibernate.reactive.stage.Stage;

import java.util.List;
import java.util.concurrent.CompletionStage;

/**
 * Implements database operations for the resource_deployment entity.
 *
 * @author matthi-g
 */
public class ResourceDeploymentRepository extends Repository<ResourceDeployment> {
    /**
     * Create an instance from the sessionFactory.
     *
     * @param sessionFactory the session factory
     */
    public ResourceDeploymentRepository(Stage.SessionFactory sessionFactory) {
        super(sessionFactory, ResourceDeployment.class);
    }

    /**
     * Find all resource deployments by their deployment
     *
     * @param deploymentId the id of the deployment
     * @return a CompletionStage that emits a list of all resource deployments
     */
    public CompletionStage<List<ResourceDeployment>> findAllByDeploymentId(long deploymentId) {
        return sessionFactory.withSession(session ->
                session.createQuery("select distinct rd from ResourceDeployment rd " +
                                "left join fetch rd.status " +
                                "where rd.deployment.deploymentId=:deploymentId", entityClass)
                        .setParameter("deploymentId", deploymentId)
                        .getResultList()
        );
    }

    /**
     * Update the trigger url of a resource deployment by its id.
     *
     * @param id the id of the resource deployment
     * @param triggerUrl the new trigger url
     * @return a CompletionStage that emits the row count
     */
    public CompletionStage<Integer> updateTriggerUrl(long id, String triggerUrl) {
        return sessionFactory.withSession(session ->
            session.createQuery("update ResourceDeployment rd " +
                "set triggerUrl=:triggerUrl, isDeployed=true " +
                "where rd.resourceDeploymentId=:id")
                .setParameter("triggerUrl", triggerUrl)
                .setParameter("id", id)
                .executeUpdate()
        );
    }

    /**
     * Update the resource deployment status to the status value by its deployment.
     *
     * @param deploymentId the id of the deployment
     * @param statusValue the new status value
     * @return a CompletionStage that emits the row count
     */
    public CompletionStage<Integer> updateDeploymentStatusByDeploymentId(long deploymentId,
            DeploymentStatusValue statusValue) {
        return sessionFactory.withSession(session ->
            session.createQuery("update ResourceDeployment rd " +
                "set status.statusId=" +
                "(select rds.statusId from ResourceDeploymentStatus rds where rds.statusValue=:statusValue)" +
                "where rd.deployment.deploymentId=:deploymentId")
                .setParameter("deploymentId", deploymentId)
                .setParameter("statusValue", statusValue.name())
                .executeUpdate()
        );
    }
}
