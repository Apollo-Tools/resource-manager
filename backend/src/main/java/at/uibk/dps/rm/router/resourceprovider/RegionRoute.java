package at.uibk.dps.rm.router.resourceprovider;

import at.uibk.dps.rm.handler.ResultHandler;
import at.uibk.dps.rm.handler.resourceprovider.RegionChecker;
import at.uibk.dps.rm.handler.resourceprovider.RegionHandler;
import at.uibk.dps.rm.router.Route;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;

/**
 * Used to initialise the region route.
 *
 * @author matthi-g
 */
public class RegionRoute implements Route {
    @Override
    public void init(RouterBuilder router, ServiceProxyProvider serviceProxyProvider) {
        RegionChecker regionChecker = new RegionChecker(serviceProxyProvider.getRegionService());
        RegionHandler regionHandler = new RegionHandler(regionChecker);
        ResultHandler resultHandler = new ResultHandler(regionHandler);

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
