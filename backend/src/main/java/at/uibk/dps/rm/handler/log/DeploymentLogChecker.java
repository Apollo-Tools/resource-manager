package at.uibk.dps.rm.handler.log;

import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.service.rxjava3.database.log.DeploymentLogService;

/**
 * Implements methods to perform CRUD operations on the deployment_log entity.
 *
 * @see EntityChecker
 *
 * @author matthi-g
 */
public class DeploymentLogChecker extends EntityChecker {

    /**
     * Create an instance from the deploymentLogService
     *
     * @param deploymentLogService the deployment log service
     */
    public DeploymentLogChecker(DeploymentLogService deploymentLogService) {
        super(deploymentLogService);
    }
}
