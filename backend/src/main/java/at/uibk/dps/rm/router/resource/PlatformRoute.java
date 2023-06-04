package at.uibk.dps.rm.router.resource;

import at.uibk.dps.rm.handler.ResultHandler;
import at.uibk.dps.rm.handler.resource.PlatformChecker;
import at.uibk.dps.rm.handler.resource.PlatformHandler;
import at.uibk.dps.rm.router.Route;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;

public class PlatformRoute implements Route {
    @Override
    public void init(RouterBuilder router, ServiceProxyProvider serviceProxyProvider) {
        PlatformChecker platformChecker = new PlatformChecker(serviceProxyProvider.getPlatformService());
        PlatformHandler platformHandler = new PlatformHandler(platformChecker);
        ResultHandler resultHandler = new ResultHandler(platformHandler);

        router
            .operation("listPlatforms")
            .handler(resultHandler::handleFindAllRequest);
    }
}
