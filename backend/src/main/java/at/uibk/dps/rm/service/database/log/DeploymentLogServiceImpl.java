package at.uibk.dps.rm.service.database.log;

import at.uibk.dps.rm.entity.model.DeploymentLog;
import at.uibk.dps.rm.repository.log.DeploymentLogRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;

/**
 * This is the implementation of the #DeploymentLogService.
 *
 * @author matthi-g
 */
public class DeploymentLogServiceImpl extends DatabaseServiceProxy<DeploymentLog> implements DeploymentLogService {
    /**
     * Create an instance from the deploymentLogRepository.
     *
     * @param deploymentLogRepository the deployment log repository
     */
    public DeploymentLogServiceImpl(DeploymentLogRepository deploymentLogRepository) {
        super(deploymentLogRepository, DeploymentLog.class);
    }
}
