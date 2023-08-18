package at.uibk.dps.rm.router.resource;

import at.uibk.dps.rm.handler.ResultHandler;
import at.uibk.dps.rm.handler.resource.ResourceChecker;
import at.uibk.dps.rm.handler.resource.SubResourceHandler;
import at.uibk.dps.rm.router.Route;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;

/**
 * Used to initialise the sub resource route.
 *
 * @author matthi-g
 */
public class SubresourceRoute implements Route {
    @Override
    public void init(RouterBuilder router, ServiceProxyProvider serviceProxyProvider) {
        ResourceChecker resourceChecker = new ResourceChecker(serviceProxyProvider.getResourceService());
        SubResourceHandler resourceHandler = new SubResourceHandler(resourceChecker);
        ResultHandler resultHandler = new ResultHandler(resourceHandler);

        router
            .operation("listSubresources")
            .handler(resultHandler::handleFindAllRequest);
    }
}
