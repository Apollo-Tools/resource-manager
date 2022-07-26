package at.uibk.dps.rm.router;

import at.uibk.dps.rm.handler.RequestHandler;
import at.uibk.dps.rm.handler.Resource.ResourceTypeHandler;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;

public class ResourceTypeRoute {
    public static void init(RouterBuilder router, ServiceProxyProvider serviceProxyProvider) {
        ResourceTypeHandler resourceTypeHandler = new ResourceTypeHandler(serviceProxyProvider.getResourceTypeService(), serviceProxyProvider.getResourceService());
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
