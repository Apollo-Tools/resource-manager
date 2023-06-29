package at.uibk.dps.rm.handler.deployment;

import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.handler.ErrorHandler;
import at.uibk.dps.rm.service.rxjava3.database.deployment.DeploymentService;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
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
     * Find all deployments by account.
     *
     * @param accountId the id of the account
     * @return a Single that emits all found deployments as JsonArray
     */
    public Single<JsonArray> checkFindAll(long accountId) {
        return ErrorHandler.handleFindAll(deploymentService.findAllByAccountId(accountId));
    }

    /**
     * Find a deployment by its id and account.
     *
     * @param id the id of the deployment
     * @param accountId  the id of the account
     * @return a Single that emits the found deployment as JsonObject
     */
    public Single<JsonObject> checkFindOne(long id, long accountId) {
        Single<JsonObject> findOneById = deploymentService.findOneByIdAndAccountId(id, accountId);
        return ErrorHandler.handleFindOne(findOneById);
    }

    /**
     * Submit the creation of a new entity.
     *
     * @param accountId id of the account
     * @return a Single that emits the persisted entity
     */
    public Single<JsonObject> submitCreateDeployment(long accountId) {
        Deployment deployment = new Deployment();
        deployment.setIsActive(true);
        Account account = new Account();
        account.setAccountId(accountId);
        deployment.setCreatedBy(account);
        return this.submitCreate(JsonObject.mapFrom(deployment));
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
