package at.uibk.dps.rm.handler.deployment;

import at.uibk.dps.rm.handler.ValidationHandler;
import at.uibk.dps.rm.util.misc.HttpHelper;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RoutingContext;

/**
 * Processes the http requests that concern the deployment entity.
 *
 * @author matthi-g
 */
public class DeploymentHandler extends ValidationHandler {

    private final DeploymentChecker deploymentChecker;

    /**
     * Create an instance from the deploymentChecker
     *
     * @param deploymentChecker the deployment checker
     */
    public DeploymentHandler(DeploymentChecker deploymentChecker) {
        super(deploymentChecker);
        this.deploymentChecker = deploymentChecker;
    }

    @Override
    public Single<JsonObject> postOneToAccount(RoutingContext rc) {
        return super.postOneToAccount(rc);
    }

    /**
     * Cancel an existing deployment and deploy all resources.
     *
     * @param rc the routing context
     * @return a Single that emits the updated deployment entity
     */
    public Single<JsonObject> cancelDeployment(RoutingContext rc) {
        long accountId = rc.user().principal().getLong("account_id");
        return HttpHelper.getLongPathParam(rc, "id")
            .flatMap(id -> deploymentChecker.submitCancelDeployment(id, accountId));
    }
}
