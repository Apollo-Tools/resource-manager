package at.uibk.dps.rm.router.resourceprovider;

import at.uibk.dps.rm.handler.RequestHandler;
import at.uibk.dps.rm.handler.resourceprovider.ResourceProviderChecker;
import at.uibk.dps.rm.handler.resourceprovider.ResourceProviderHandler;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;

public class ResourceProviderRoute {
    public static void init(RouterBuilder router, ServiceProxyProvider serviceProxyProvider) {
        ResourceProviderChecker providerChecker =
            new ResourceProviderChecker(serviceProxyProvider.getResourceProviderService());
        ResourceProviderHandler resourceProviderHandler = new ResourceProviderHandler(providerChecker);
        RequestHandler requestHandler = new RequestHandler(resourceProviderHandler);

        router
            .operation("createResourceProvider")
            .handler(requestHandler::postRequest);

        router
            .operation("listResourceProviders")
            .handler(requestHandler::getAllRequest);

        router
            .operation("getResourceProvider")
            .handler(requestHandler::getRequest);

        router
            .operation("deleteResourceProvider")
            .handler(requestHandler::deleteRequest);
    }
}
