package at.uibk.dps.rm.repository.deployment;

import at.uibk.dps.rm.entity.model.ServiceDeployment;
import at.uibk.dps.rm.repository.Repository;
import at.uibk.dps.rm.service.database.util.SessionManager;
import io.reactivex.rxjava3.core.Maybe;
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
     * Find a service deployment by its id, creator and fetch the deployment and deployment status.
     *
     * @param sessionManager the database session manger
     * @param id the id of the service deployment
     * @param accountId the id of the creator
     * @return a Maybe that emits the service deployment if it exists, else null
     */
    @Override
    public Maybe<ServiceDeployment> findByIdAndAccountId(SessionManager sessionManager, long id, long accountId) {
        return Maybe.fromCompletionStage(sessionManager.getSession()
            .createQuery(
                "from ServiceDeployment sd " +
                    "left join fetch sd.deployment " +
                    "left join fetch sd.status " +
                    "where sd.resourceDeploymentId =:id and " +
                    "sd.deployment.createdBy.accountId=:accountId", entityClass)
            .setParameter("id", id)
            .setParameter("accountId", accountId)
            .getSingleResultOrNull()
        );
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
            .createQuery("select distinct sd from ServiceDeployment sd " +
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
}
