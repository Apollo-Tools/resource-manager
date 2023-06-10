package at.uibk.dps.rm.repository.deployment;

import at.uibk.dps.rm.entity.deployment.DeploymentStatusValue;
import at.uibk.dps.rm.entity.model.ServiceDeployment;
import at.uibk.dps.rm.repository.Repository;
import org.hibernate.reactive.stage.Stage;

import java.util.List;
import java.util.concurrent.CompletionStage;

/**
 * Implements database operations for the service_deployment entity.
 *
 * @author matthi-g
 */
public class ServiceDeploymentRepository extends Repository<ServiceDeployment> {
    /**
     * Create an instance from the sessionFactory.
     *
     * @param sessionFactory the session factory
     */
    public ServiceDeploymentRepository(Stage.SessionFactory sessionFactory) {
        super(sessionFactory, ServiceDeployment.class);
    }

    /**
     * Find all service deployments by their deployment
     * @param deploymentId the id of the deployment
     * @return a CompletionStage that emits a list of all service deployments
     */
    public CompletionStage<List<ServiceDeployment>> findAllByDeploymentId(long deploymentId) {
        return sessionFactory.withSession(session ->
            session.createQuery("select distinct sd from ServiceDeployment sd " +
                    "left join fetch sd.service s " +
                    "left join fetch s.serviceType " +
                    "left join fetch sd.resource r " +
                    "left join fetch r.metricValues mv " +
                    "left join fetch mv.metric " +
                    "left join fetch r.platform p " +
                    "left join fetch p.resourceType " +
                    "left join fetch r.region reg " +
                    "left join fetch reg.resourceProvider rp " +
                    "left join fetch rp.environment " +
                    "left join fetch sd.status " +
                    "where sd.deployment.deploymentId=:deploymentId", entityClass)
                .setParameter("deploymentId", deploymentId)
                .getResultList()
        );
    }



    /**
     * Find a service deployment by its deployment, resourceDeployment, creator and deployment status.
     *
     * @param deploymentId the id of the deployment
     * @param resourceDeploymentId the id of the resource deployment
     * @param accountId the account id of the creator
     * @return a CompletionStage that emits the resource deployment if it exists, else null
     */
    public CompletionStage<ServiceDeployment> findOneByDeploymentStatus(long deploymentId, long resourceDeploymentId,
            long accountId, DeploymentStatusValue statusValue) {
        return sessionFactory.withSession(session ->
            session.createQuery("from ServiceDeployment sd " +
                    "where sd.deployment.deploymentId=:deploymentId and " +
                    "sd.resourceDeploymentId=:resourceDeploymentId and " +
                    "sd.deployment.createdBy.accountId=:accountId and " +
                    "sd.status.statusValue=:statusValue", entityClass)
                .setParameter("deploymentId", deploymentId)
                .setParameter("resourceDeploymentId", resourceDeploymentId)
                .setParameter("accountId", accountId)
                .setParameter("statusValue", statusValue.name())
                .getSingleResultOrNull()
        );
    }
}
