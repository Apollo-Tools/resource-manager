package at.uibk.dps.rm.router.resource;

import at.uibk.dps.rm.handler.RequestHandler;
import at.uibk.dps.rm.handler.resource.ResourceChecker;
import at.uibk.dps.rm.handler.resource.ResourceHandler;
import at.uibk.dps.rm.handler.resource.ResourceTypeChecker;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;

public class ResourceRoute {
    public static void init(RouterBuilder router, ServiceProxyProvider serviceProxyProvider) {
        ResourceChecker resourceChecker = new ResourceChecker(serviceProxyProvider.getResourceService());
        ResourceTypeChecker resourceTypeChecker = new ResourceTypeChecker(serviceProxyProvider.getResourceTypeService());
        ResourceHandler resourceHandler = new ResourceHandler(resourceChecker, resourceTypeChecker);
        RequestHandler resourceRequestHandler = new RequestHandler(resourceHandler);

        router
            .operation("createResource")
            .handler(resourceRequestHandler::postRequest);

        router
            .operation("listResources")
            .handler(resourceRequestHandler::getAllRequest);

        router
            .operation("getResource")
            .handler(resourceRequestHandler::getRequest);

        router
            .operation("updateResource")
            .handler(resourceRequestHandler::patchRequest);

        router
            .operation("deleteResource")
            .handler(resourceRequestHandler::deleteRequest);
    }
}
