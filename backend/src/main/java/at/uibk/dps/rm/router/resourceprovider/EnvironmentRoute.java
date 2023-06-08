package at.uibk.dps.rm.router.resourceprovider;

import at.uibk.dps.rm.handler.ResultHandler;
import at.uibk.dps.rm.handler.resourceprovider.*;
import at.uibk.dps.rm.router.Route;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;

/**
 * Used to initialise the environment route.
 *
 * @author matthi-g
 */
public class EnvironmentRoute implements Route {
    @Override
    public void init(RouterBuilder router, ServiceProxyProvider serviceProxyProvider) {
        EnvironmentChecker environmentChecker = new EnvironmentChecker(serviceProxyProvider.getEnvironmentService());
        EnvironmentHandler environmentHandler = new EnvironmentHandler(environmentChecker);
        ResultHandler resultHandler = new ResultHandler(environmentHandler);

        router
            .operation("listEnvironments")
            .handler(resultHandler::handleFindAllRequest);
    }
}
