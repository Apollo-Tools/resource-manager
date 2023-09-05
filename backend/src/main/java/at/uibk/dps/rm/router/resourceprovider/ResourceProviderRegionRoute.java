package at.uibk.dps.rm.router.resourceprovider;

import at.uibk.dps.rm.handler.ResultHandler;
import at.uibk.dps.rm.handler.resourceprovider.ResourceProviderRegionHandler;
import at.uibk.dps.rm.router.Route;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;

/**
 * Used to initialise the resource provider region route.
 *
 * @author matthi-g
 */
public class ResourceProviderRegionRoute implements Route {

    @Override
    public void init(RouterBuilder router, ServiceProxyProvider serviceProxyProvider) {
        ResourceProviderRegionHandler providerRegionHandler = new ResourceProviderRegionHandler(serviceProxyProvider
            .getRegionService());
        ResultHandler resultHandler = new ResultHandler(providerRegionHandler);

        router
            .operation("listResourceProviderRegions")
            .handler(resultHandler::handleFindAllRequest);
    }
}
