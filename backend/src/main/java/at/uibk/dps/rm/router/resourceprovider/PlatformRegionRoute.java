package at.uibk.dps.rm.router.resourceprovider;

import at.uibk.dps.rm.rx.handler.ResultHandler;
import at.uibk.dps.rm.rx.handler.resourceprovider.PlatformRegionHandler;
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
        PlatformRegionHandler platformRegionHandler = new PlatformRegionHandler(serviceProxyProvider
            .getRegionService());
        ResultHandler resultHandler = new ResultHandler(platformRegionHandler);

        router
            .operation("listPlatformRegions")
            .handler(resultHandler::handleFindAllRequest);
    }
}
