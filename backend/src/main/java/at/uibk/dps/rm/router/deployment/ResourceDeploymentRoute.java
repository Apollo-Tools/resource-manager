package at.uibk.dps.rm.router.deployment;

import at.uibk.dps.rm.handler.ResultHandler;
import at.uibk.dps.rm.handler.deploymentexecution.DeploymentExecutionChecker;
import at.uibk.dps.rm.handler.deploymentexecution.ContainerStartupHandler;
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
        DeploymentExecutionChecker deploymentChecker = new DeploymentExecutionChecker(serviceProxyProvider);
        ContainerStartupHandler startupHandler = new ContainerStartupHandler(deploymentChecker, serviceProxyProvider);

        router
            .operation("startServiceDeployment")
            .handler(rc -> startupHandler.deployContainer(rc)
                .subscribe(() -> {}, throwable -> ResultHandler.handleRequestError(rc, throwable))
            );

        router
            .operation("stopServiceDeployment")
            .handler(rc -> startupHandler.terminateContainer(rc)
                .subscribe(() -> {}, throwable -> ResultHandler.handleRequestError(rc, throwable))
            );

        router
            .operation("invokeFunctionDeployment")
            .handler(rc -> startupHandler.invokeFunction(rc)
                .subscribe(() -> {}, throwable -> ResultHandler.handleRequestError(rc, throwable))
            );
    }
}
