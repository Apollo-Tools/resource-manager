package at.uibk.dps.rm.rx.repository.log;

import at.uibk.dps.rm.entity.model.DeploymentLog;
import at.uibk.dps.rm.rx.repository.Repository;

/**
 * Implements database operations for the deployment_log entity.
 *
 * @author matthi-g
 */
public class DeploymentLogRepository extends Repository<DeploymentLog> {

    /**
     * Create an instance.
     */
    public DeploymentLogRepository() {
        super(DeploymentLog.class);
    }
}
