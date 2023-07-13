package at.uibk.dps.rm.router.resource;

import at.uibk.dps.rm.handler.ResultHandler;
import at.uibk.dps.rm.handler.resource.PlatformChecker;
import at.uibk.dps.rm.handler.resource.PlatformRegionHandler;
import at.uibk.dps.rm.handler.resourceprovider.RegionChecker;
import at.uibk.dps.rm.router.Route;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;

/**
 * Used to initialise the platform region route.
 *
 * @author matthi-g
 */
public class PlatformRegionRoute  implements Route {
    @Override
    public void init(RouterBuilder router, ServiceProxyProvider serviceProxyProvider) {
        PlatformChecker platformChecker = new PlatformChecker(serviceProxyProvider.getPlatformService());
        RegionChecker regionChecker = new RegionChecker(serviceProxyProvider.getRegionService());
        PlatformRegionHandler platformRegionHandler = new PlatformRegionHandler(platformChecker, regionChecker);
        ResultHandler resultHandler = new ResultHandler(platformRegionHandler);

        router
            .operation("listPlatformRegions")
            .handler(resultHandler::handleFindAllRequest);
    }
}
