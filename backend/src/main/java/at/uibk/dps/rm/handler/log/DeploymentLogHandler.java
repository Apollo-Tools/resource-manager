package at.uibk.dps.rm.handler.log;

import at.uibk.dps.rm.handler.ValidationHandler;
import at.uibk.dps.rm.service.rxjava3.database.log.LogService;
import at.uibk.dps.rm.service.rxjava3.database.log.DeploymentLogService;
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

    private final LogService logService;

    /**
     * Create an instance from the deploymentLogService, logService and deploymentService.
     *
     * @param deploymentLogService the deployment log service
     * @param logService the log service
     */
    public DeploymentLogHandler(DeploymentLogService deploymentLogService, LogService logService) {
        super(deploymentLogService);
        this.logService = logService;
    }

    @Override
    protected Single<JsonArray> getAll(RoutingContext rc) {
        long accountId = rc.user().principal().getLong("account_id");
        return HttpHelper.getLongPathParam(rc, "id")
            .flatMap(id -> logService.findAllByDeploymentIdAndAccountId(id, accountId));
    }
}
