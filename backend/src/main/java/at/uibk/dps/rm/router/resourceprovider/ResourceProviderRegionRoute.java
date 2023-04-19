package at.uibk.dps.rm.router.resourceprovider;

import at.uibk.dps.rm.handler.ResultHandler;
import at.uibk.dps.rm.handler.resourceprovider.RegionChecker;
import at.uibk.dps.rm.handler.resourceprovider.ResourceProviderRegionHandler;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;

public class ResourceProviderRegionRoute {

    public static void init(RouterBuilder router, ServiceProxyProvider serviceProxyProvider) {
        RegionChecker regionChecker = new RegionChecker(serviceProxyProvider.getRegionService());
        ResourceProviderRegionHandler providerRegionHandler = new ResourceProviderRegionHandler(regionChecker);
        ResultHandler resultHandler = new ResultHandler(providerRegionHandler);

        router
            .operation("listResourceProviderRegions")
            .handler(resultHandler::handleFindAllRequest);
    }
}
