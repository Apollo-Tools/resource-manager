package at.uibk.dps.rm.handler.deployment;

import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.service.rxjava3.database.deployment.DeploymentService;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;

/**
 * Implements methods to perform CRUD operations on the deployment entity.
 *
 * @see EntityChecker
 *
 * @author matthi-g
 */
public class DeploymentChecker extends EntityChecker {

    private final DeploymentService deploymentService;

    /**
     * Create an instance from the deploymentService.
     *
     * @param deploymentService the deployment service
     */
    public DeploymentChecker(DeploymentService deploymentService) {
        super(deploymentService);
        this.deploymentService = deploymentService;

    }

    /**
     * Submit the cancellation of an existing deployment.
     *
     * @param deploymentId the id of the deployment
     * @param accountId id of the account
     * @return a Single that emits the updated deployment entity
     */
    public Single<JsonObject> submitCancelDeployment(long deploymentId, long accountId) {
        return deploymentService.cancelDeployment(deploymentId, accountId);
    }
}
