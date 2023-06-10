package at.uibk.dps.rm.repository.deployment;

import at.uibk.dps.rm.entity.model.ResourceDeploymentStatus;
import at.uibk.dps.rm.repository.Repository;
import org.hibernate.reactive.stage.Stage;

import java.util.concurrent.CompletionStage;

/**
 * Implements database operations for the resource_deployment_status entity.
 *
 * @author matthi-g
 */
public class ResourceDeploymentStatusRepository extends Repository<ResourceDeploymentStatus> {

    /**
     * Create an instance from the sessionFactory.
     *
     * @param sessionFactory the session factory
     */
    public ResourceDeploymentStatusRepository(Stage.SessionFactory sessionFactory) {
        super(sessionFactory, ResourceDeploymentStatus.class);
    }

    /**
     * Fine a resource deployment status by its value.
     *
     * @param statusValue the value of the status
     * @return a CompletionStage that emits the resource deployment status if it exists, else null
     */
    public CompletionStage<ResourceDeploymentStatus> findOneByStatusValue(String statusValue) {
        return sessionFactory.withSession(session ->
            session.createQuery("from ResourceDeploymentStatus status " +
                    "where status.statusValue=:statusValue", entityClass)
                .setParameter("statusValue", statusValue)
                .getSingleResult()
        );
    }
}
