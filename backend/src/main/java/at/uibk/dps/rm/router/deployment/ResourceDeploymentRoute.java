package at.uibk.dps.rm.router.deployment;

import at.uibk.dps.rm.handler.deploymentexecution.DeploymentExecutionChecker;
import at.uibk.dps.rm.handler.deploymentexecution.ContainerStartupHandler;
import at.uibk.dps.rm.router.Route;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.web.client.WebClient;
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
        WebClient webClient = WebClient.create(Vertx.currentContext().owner());
        ContainerStartupHandler startupHandler = new ContainerStartupHandler(deploymentChecker,
            serviceProxyProvider.getServiceDeploymentService(), serviceProxyProvider.getFunctionDeploymentService(),
            webClient);

        router
            .operation("startServiceDeployment")
            .handler(startupHandler::deployContainer);

        router
            .operation("stopServiceDeployment")
            .handler(startupHandler::terminateContainer);

        router
            .operation("invokeFunctionDeployment")
            .handler(startupHandler::invokeFunction);
    }
}
