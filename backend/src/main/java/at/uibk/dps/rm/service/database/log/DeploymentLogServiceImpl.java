package at.uibk.dps.rm.service.database.log;

import at.uibk.dps.rm.entity.model.DeploymentLog;
import at.uibk.dps.rm.repository.log.DeploymentLogRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import at.uibk.dps.rm.service.database.util.SessionManagerProvider;

/**
 * This is the implementation of the {@link DeploymentLogService}.
 *
 * @author matthi-g
 */
public class DeploymentLogServiceImpl extends DatabaseServiceProxy<DeploymentLog> implements DeploymentLogService {
    /**
     * Create an instance from the deploymentLogRepository.
     *
     * @param deploymentLogRepository the deployment log repository
     */
    public DeploymentLogServiceImpl(DeploymentLogRepository deploymentLogRepository, SessionManagerProvider smProvider) {
        super(deploymentLogRepository, DeploymentLog.class, smProvider);
    }
}
