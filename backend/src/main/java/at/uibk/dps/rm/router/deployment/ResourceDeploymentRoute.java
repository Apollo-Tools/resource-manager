package at.uibk.dps.rm.router.deployment;

import at.uibk.dps.rm.rx.handler.deploymentexecution.DeploymentExecutionChecker;
import at.uibk.dps.rm.rx.handler.deploymentexecution.ContainerStartupHandler;
import at.uibk.dps.rm.router.Route;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;

/**
 * Used to initialise the container startup and stop routes.
 *
 * @author matthi-g
 */
public class ResourceDeploymentRoute implements Route {
    @Override
    public void init(RouterBuilder router, ServiceProxyProvider serviceProxyProvider) {
        DeploymentExecutionChecker deploymentChecker = new DeploymentExecutionChecker(serviceProxyProvider
            .getDeploymentExecutionService(), serviceProxyProvider.getLogService(), serviceProxyProvider
            .getDeploymentLogService());
        ContainerStartupHandler startupHandler = new ContainerStartupHandler(deploymentChecker,
            serviceProxyProvider.getServiceDeploymentService());

        router
            .operation("startResourceDeployment")
            .handler(startupHandler::deployContainer);

        router
            .operation("stopResourceDeployment")
            .handler(startupHandler::terminateContainer);
    }
}
