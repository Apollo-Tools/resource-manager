package at.uibk.dps.rm.router.log;

import at.uibk.dps.rm.handler.ResultHandler;
import at.uibk.dps.rm.handler.log.DeploymentLogHandler;
import at.uibk.dps.rm.router.Route;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;

/**
 * Used to initialise the deployment log route.
 *
 * @author matthi-g
 */
public class DeploymentLogRoute implements Route {

    @Override
    public void init(RouterBuilder router, ServiceProxyProvider serviceProxyProvider) {
        DeploymentLogHandler deploymentLogHandler = new DeploymentLogHandler(serviceProxyProvider
            .getDeploymentLogService(), serviceProxyProvider.getLogService());
        ResultHandler resultHandler = new ResultHandler(deploymentLogHandler);

        router
            .operation("listDeploymentLogs")
            .handler(resultHandler::handleFindAllRequest);
    }
}
