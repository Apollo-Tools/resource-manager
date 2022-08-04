package at.uibk.dps.rm.router;

import at.uibk.dps.rm.handler.RequestHandler;
import at.uibk.dps.rm.handler.resource.ResourceHandler;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;

public class ResourceRoute {
    public static void init(RouterBuilder router, ServiceProxyProvider serviceProxyProvider) {
        ResourceHandler resourceHandler = new ResourceHandler(serviceProxyProvider.getResourceService(),
            serviceProxyProvider.getResourceTypeService(), serviceProxyProvider.getMetricService(),
            serviceProxyProvider.getMetricValueService());
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
