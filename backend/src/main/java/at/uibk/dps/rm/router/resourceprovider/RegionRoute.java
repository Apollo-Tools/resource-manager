package at.uibk.dps.rm.router.resourceprovider;

import at.uibk.dps.rm.handler.ResultHandler;
import at.uibk.dps.rm.handler.resourceprovider.RegionChecker;
import at.uibk.dps.rm.handler.resourceprovider.RegionHandler;
import at.uibk.dps.rm.handler.resourceprovider.ResourceProviderChecker;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;

public class RegionRoute {
    public static void init(final RouterBuilder router, final ServiceProxyProvider serviceProxyProvider) {
        final RegionChecker regionChecker = new RegionChecker(serviceProxyProvider.getRegionService());
        final ResourceProviderChecker resourceProviderChecker = new ResourceProviderChecker(serviceProxyProvider
            .getResourceProviderService());
        final RegionHandler regionHandler = new RegionHandler(regionChecker, resourceProviderChecker);
        final ResultHandler resultHandler = new ResultHandler(regionHandler);

        router
            .operation("createRegion")
            .handler(resultHandler::handleSaveOneRequest);

        router
            .operation("listRegions")
            .handler(resultHandler::handleFindAllRequest);

        router
            .operation("getRegion")
            .handler(resultHandler::handleFindOneRequest);

        router
            .operation("deleteRegion")
            .handler(resultHandler::handleDeleteRequest);
    }
}
