package at.uibk.dps.rm.repository.deployment;

import at.uibk.dps.rm.entity.model.ResourceDeploymentStatus;
import at.uibk.dps.rm.repository.Repository;
import at.uibk.dps.rm.service.database.util.SessionManager;
import io.reactivex.rxjava3.core.Maybe;

/**
 * Implements database operations for the resource_deployment_status entity.
 *
 * @author matthi-g
 */
public class ResourceDeploymentStatusRepository extends Repository<ResourceDeploymentStatus> {

    /**
     * Create an instance.
     */
    public ResourceDeploymentStatusRepository() {
        super(ResourceDeploymentStatus.class);
    }

    /**
     * Fine a resource deployment status by its value.
     *
     * @param sessionManager the database session manager
     * @param statusValue the value of the status
     * @return a Maybe that emits the resource deployment status if it exists, else null
     */
    public Maybe<ResourceDeploymentStatus> findOneByStatusValue(SessionManager sessionManager, String statusValue) {
        return Maybe.fromCompletionStage(sessionManager.getSession()
            .createQuery("from ResourceDeploymentStatus status " +
                "where status.statusValue=:statusValue", entityClass)
            .setParameter("statusValue", statusValue)
            .getSingleResultOrNull()
        );
    }
}
