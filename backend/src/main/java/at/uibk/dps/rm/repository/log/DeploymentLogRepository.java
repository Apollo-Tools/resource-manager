package at.uibk.dps.rm.repository.log;

import at.uibk.dps.rm.entity.model.DeploymentLog;
import at.uibk.dps.rm.repository.Repository;
import org.hibernate.reactive.stage.Stage;

/**
 * Implements database operations for the deployment_log entity.
 *
 * @author matthi-g
 */
public class DeploymentLogRepository extends Repository<DeploymentLog> {

    /**
     * Create an instance from the sessionFactory.
     *
     * @param sessionFactory the session factory
     */
    public DeploymentLogRepository(Stage.SessionFactory sessionFactory) {
        super(sessionFactory, DeploymentLog.class);
    }
}
