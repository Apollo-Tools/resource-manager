package at.uibk.dps.rm.router.resourceprovider;

import at.uibk.dps.rm.rx.handler.ResultHandler;
import at.uibk.dps.rm.rx.handler.resourceprovider.RegionHandler;
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
        RegionHandler regionHandler = new RegionHandler(serviceProxyProvider.getRegionService());
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
