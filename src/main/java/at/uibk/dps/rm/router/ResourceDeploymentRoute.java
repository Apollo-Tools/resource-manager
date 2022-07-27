package at.uibk.dps.rm.router;

import at.uibk.dps.rm.handler.resource.ResourceDeploymentHandler;
import at.uibk.dps.rm.handler.resource.ResourceHandler;
import at.uibk.dps.rm.handler.resource.ResourceInputHandler;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;

public class ResourceDeploymentRoute {

    public static void init(RouterBuilder router, ServiceProxyProvider serviceProxyProvider) {
        ResourceHandler resourceHandler = new ResourceHandler(serviceProxyProvider.getResourceService(), serviceProxyProvider.getResourceTypeService());
        ResourceDeploymentHandler resourceDeploymentHandler = new ResourceDeploymentHandler(resourceHandler);

        router
            .operation("getResourcesBySLOs")
            .handler(ResourceInputHandler::validateGetResourcesBySLOsRequest)
            .handler(resourceDeploymentHandler::getResourcesBySLOs);
    }
}
