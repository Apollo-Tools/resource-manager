package at.uibk.dps.rm.repository.deployment;

import at.uibk.dps.rm.entity.deployment.DeploymentStatusValue;
import at.uibk.dps.rm.entity.model.ServiceDeployment;
import at.uibk.dps.rm.repository.Repository;
import at.uibk.dps.rm.service.database.util.SessionManager;
import io.reactivex.rxjava3.core.Single;

import java.util.List;

/**
 * Implements database operations for the service_deployment entity.
 *
 * @author matthi-g
 */
public class ServiceDeploymentRepository extends Repository<ServiceDeployment> {
    /**
     * Create an instance.
     */
    public ServiceDeploymentRepository() {
        super(ServiceDeployment.class);
    }

    /**
     * Find all service deployments by their deployment
     *
     * @param sessionManager the database session manager
     * @param deploymentId the id of the deployment
     * @return a Single that emits a list of all service deployments
     */
    public Single<List<ServiceDeployment>> findAllByDeploymentId(SessionManager sessionManager, long deploymentId) {
        return Single.fromCompletionStage(sessionManager.getSession()
            .createQuery("select distinct sd from ServiceDeployment " +
                "sd " +
                "left join fetch sd.service s " +
                "left join fetch s.serviceType " +
                "left join fetch s.k8sServiceType " +
                "left join fetch sd.resource r " +
                "left join fetch r.metricValues mv " +
                "left join fetch mv.metric " +
                "left join fetch r.platform p " +
                "left join fetch p.resourceType " +
                "left join fetch r.region reg " +
                "left join fetch reg.resourceProvider rp " +
                "left join fetch r.mainResource mr " +
                "left join fetch mr.metricValues mmv " +
                "left join fetch mmv.metric " +
                "left join fetch mr.platform p " +
                "left join fetch p.resourceType " +
                "left join fetch mr.region mreg " +
                "left join fetch reg.resourceProvider mrp " +
                "left join fetch rp.environment " +
                "left join fetch sd.status " +
                "where sd.deployment.deploymentId=:deploymentId", entityClass)
            .setParameter("deploymentId", deploymentId)
            .getResultList()
        );
    }



    /**
     * Find the amount of service deployments by deployment, resourceDeployment, creator and
     * deployment status.
     *
     * @param sessionManager the database session manager
     * @param deploymentId the id of the deployment
     * @param resourceDeploymentId the id of the resource deployment
     * @param accountId the account id of the creator
     * @return a Single that emits the amount resource deployments
     */
    public Single<Long> countByDeploymentStatus(SessionManager sessionManager, long deploymentId,
            long resourceDeploymentId, long accountId, DeploymentStatusValue statusValue) {
        return Single.fromCompletionStage(sessionManager.getSession()
            .createQuery("select count(*) from ServiceDeployment sd " +
                "where sd.deployment.deploymentId=:deploymentId and " +
                "sd.resourceDeploymentId=:resourceDeploymentId and " +
                "sd.deployment.createdBy.accountId=:accountId and " +
                "sd.status.statusValue=:statusValue", Long.class)
            .setParameter("deploymentId", deploymentId)
            .setParameter("resourceDeploymentId", resourceDeploymentId)
            .setParameter("accountId", accountId)
            .setParameter("statusValue", statusValue.name())
            .getSingleResult()
        );
    }
}
