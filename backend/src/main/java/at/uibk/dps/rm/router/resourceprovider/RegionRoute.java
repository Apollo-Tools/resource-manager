package at.uibk.dps.rm.router.resourceprovider;

import at.uibk.dps.rm.handler.RequestHandler;
import at.uibk.dps.rm.handler.resourceprovider.RegionChecker;
import at.uibk.dps.rm.handler.resourceprovider.RegionHandler;
import at.uibk.dps.rm.handler.resourceprovider.ResourceProviderChecker;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;

public class RegionRoute {
    public static void init(RouterBuilder router, ServiceProxyProvider serviceProxyProvider) {
        RegionChecker regionChecker = new RegionChecker(serviceProxyProvider.getRegionService());
        ResourceProviderChecker resourceProviderChecker = new ResourceProviderChecker(serviceProxyProvider
            .getResourceProviderService());
        RegionHandler regionHandler = new RegionHandler(regionChecker, resourceProviderChecker);
        RequestHandler requestHandler = new RequestHandler(regionHandler);

        router
            .operation("createRegion")
            .handler(requestHandler::postRequest);

        router
            .operation("listRegions")
            .handler(requestHandler::getAllRequest);

        router
            .operation("getRegion")
            .handler(requestHandler::getRequest);

        router
            .operation("deleteRegion")
            .handler(requestHandler::deleteRequest);
    }
}
