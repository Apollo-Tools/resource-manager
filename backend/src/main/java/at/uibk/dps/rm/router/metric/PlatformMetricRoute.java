package at.uibk.dps.rm.router.metric;

import at.uibk.dps.rm.handler.ResultHandler;
import at.uibk.dps.rm.handler.metric.PlatformMetricHandler;
import at.uibk.dps.rm.router.Route;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;

/**
 * Used to initialise the platform metric route.
 *
 * @author matthi-g
 */
public class PlatformMetricRoute implements Route {
    @Override
    public void init(RouterBuilder router, ServiceProxyProvider serviceProxyProvider) {
        PlatformMetricHandler handler = new PlatformMetricHandler(serviceProxyProvider.getPlatformMetricService());
        ResultHandler resultHandler = new ResultHandler(handler);

        router
            .operation("listPlatformMetrics")
            .handler(resultHandler::handleFindAllRequest);
    }
}
