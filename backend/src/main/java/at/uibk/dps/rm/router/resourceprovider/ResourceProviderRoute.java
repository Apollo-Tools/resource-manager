package at.uibk.dps.rm.router.resourceprovider;

import at.uibk.dps.rm.handler.ResultHandler;
import at.uibk.dps.rm.handler.resourceprovider.ResourceProviderChecker;
import at.uibk.dps.rm.handler.resourceprovider.ResourceProviderHandler;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;

public class ResourceProviderRoute {
    public static void init(RouterBuilder router, ServiceProxyProvider serviceProxyProvider) {
        ResourceProviderChecker providerChecker =
            new ResourceProviderChecker(serviceProxyProvider.getResourceProviderService());
        ResourceProviderHandler resourceProviderHandler = new ResourceProviderHandler(providerChecker);
        ResultHandler resultHandler = new ResultHandler(resourceProviderHandler);

        router
            .operation("createResourceProvider")
            .handler(resultHandler::handleSaveOneRequest);

        router
            .operation("listResourceProviders")
            .handler(resultHandler::handleFindAllRequest);

        router
            .operation("getResourceProvider")
            .handler(resultHandler::handleFindOneRequest);

        router
            .operation("deleteResourceProvider")
            .handler(resultHandler::handleDeleteRequest);
    }
}
