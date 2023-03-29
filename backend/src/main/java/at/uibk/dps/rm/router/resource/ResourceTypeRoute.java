package at.uibk.dps.rm.router.resource;

import at.uibk.dps.rm.handler.RequestHandler;
import at.uibk.dps.rm.handler.resource.ResourceChecker;
import at.uibk.dps.rm.handler.resource.ResourceTypeChecker;
import at.uibk.dps.rm.handler.resource.ResourceTypeHandler;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;

public class ResourceTypeRoute {
    public static void init(RouterBuilder router, ServiceProxyProvider serviceProxyProvider) {
        ResourceTypeChecker resourceTypeChecker = new ResourceTypeChecker(serviceProxyProvider.getResourceTypeService());
        ResourceChecker resourceChecker = new ResourceChecker(serviceProxyProvider.getResourceService());
        ResourceTypeHandler resourceTypeHandler = new ResourceTypeHandler(resourceTypeChecker, resourceChecker);
        RequestHandler requestHandler = new RequestHandler(resourceTypeHandler);

        router
            .operation("createResourceType")
            .handler(requestHandler::postRequest);

        router
            .operation("listResourceTypes")
            .handler(requestHandler::getAllRequest);

        router
            .operation("getResourceType")
            .handler(requestHandler::getRequest);

        router
            .operation("updateResourceType")
            .handler(requestHandler::patchRequest);

        router
            .operation("deleteResourceType")
            .handler(requestHandler::deleteRequest);
    }
}
