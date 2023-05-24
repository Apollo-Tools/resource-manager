package at.uibk.dps.rm.router.resourceprovider;

import at.uibk.dps.rm.handler.ResultHandler;
import at.uibk.dps.rm.handler.resourceprovider.ResourceProviderChecker;
import at.uibk.dps.rm.handler.resourceprovider.ResourceProviderHandler;
import at.uibk.dps.rm.router.Route;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;

/**
 * Used to initialise the resource provider route.
 *
 * @author matthi-g
 */
public class ResourceProviderRoute implements Route {
    @Override
    public void init(RouterBuilder router, ServiceProxyProvider serviceProxyProvider) {
        ResourceProviderChecker providerChecker =
            new ResourceProviderChecker(serviceProxyProvider.getResourceProviderService());
        ResourceProviderHandler resourceProviderHandler = new ResourceProviderHandler(providerChecker);
        ResultHandler resultHandler = new ResultHandler(resourceProviderHandler);

        router
            .operation("listResourceProviders")
            .handler(resultHandler::handleFindAllRequest);

        router
            .operation("getResourceProvider")
            .handler(resultHandler::handleFindOneRequest);
    }
}
