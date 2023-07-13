package at.uibk.dps.rm.router.resource;

import at.uibk.dps.rm.handler.ResultHandler;
import at.uibk.dps.rm.handler.resource.ResourceTypeChecker;
import at.uibk.dps.rm.handler.resource.ResourceTypeHandler;
import at.uibk.dps.rm.router.Route;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;

/**
 * Used to initialise the resource type route.
 *
 * @author matthi-g
 */
public class ResourceTypeRoute implements Route {
    @Override
    public void init(RouterBuilder router, ServiceProxyProvider serviceProxyProvider) {
        ResourceTypeChecker resourceTypeChecker = new ResourceTypeChecker(serviceProxyProvider
            .getResourceTypeService());
        ResourceTypeHandler resourceTypeHandler = new ResourceTypeHandler(resourceTypeChecker);
        ResultHandler resultHandler = new ResultHandler(resourceTypeHandler);

        router
            .operation("listResourceTypes")
            .handler(resultHandler::handleFindAllRequest);

        router
            .operation("getResourceType")
            .handler(resultHandler::handleFindOneRequest);
    }
}
