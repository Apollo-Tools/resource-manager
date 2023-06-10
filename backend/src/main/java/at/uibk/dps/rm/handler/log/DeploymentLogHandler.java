package at.uibk.dps.rm.handler.log;

import at.uibk.dps.rm.handler.ValidationHandler;
import at.uibk.dps.rm.handler.deployment.DeploymentChecker;
import at.uibk.dps.rm.util.misc.HttpHelper;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.rxjava3.ext.web.RoutingContext;

/**
 * Processes the http requests that concern the deployment_log entity.
 *
 * @author matthi-g
 */
public class DeploymentLogHandler extends ValidationHandler {

    private final LogChecker logChecker;

    private final DeploymentChecker deploymentChecker;

    /**
     * Create an instance from the deploymentLogChecker, logChecker and deploymentChecker.
     *
     * @param deploymentLogChecker the deployment log checker
     * @param logChecker the log checker
     * @param deploymentChecker the deployment checker
     */
    public DeploymentLogHandler(DeploymentLogChecker deploymentLogChecker, LogChecker logChecker,
                                 DeploymentChecker deploymentChecker) {
        super(deploymentLogChecker);
        this.logChecker = logChecker;
        this.deploymentChecker = deploymentChecker;
    }

    @Override
    protected Single<JsonArray> getAll(RoutingContext rc) {
        long accountId = rc.user().principal().getLong("account_id");
        return HttpHelper.getLongPathParam(rc, "id")
            .flatMap(id -> deploymentChecker.checkFindOne(id, accountId).map(res -> id))
            .flatMap(id -> logChecker.checkFindAllByReservationId(id, accountId));
    }
}
