package at.uibk.dps.rm.repository.deployment;

import at.uibk.dps.rm.entity.model.FunctionDeployment;
import at.uibk.dps.rm.repository.Repository;
import org.hibernate.reactive.stage.Stage;

import java.util.List;
import java.util.concurrent.CompletionStage;

/**
 * Implements database operations for the function_deployment entity.
 *
 * @author matthi-g
 */
public class FunctionDeploymentRepository extends Repository<FunctionDeployment> {
    /**
     * Create an instance from the sessionFactory.
     *
     * @param sessionFactory the session factory
     */
    public FunctionDeploymentRepository(Stage.SessionFactory sessionFactory) {
        super(sessionFactory, FunctionDeployment.class);
    }

    /**
     * Find all function deployments by their deployment
     * @param deploymentId the id of the deployment
     * @return a CompletionStage that emits a list of all function deployments
     */
    public CompletionStage<List<FunctionDeployment>> findAllByDeploymentId(long deploymentId) {
        return sessionFactory.withSession(session ->
            session.createQuery("select distinct fd from FunctionDeployment fd " +
                    "left join fetch fd.function f " +
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
}
