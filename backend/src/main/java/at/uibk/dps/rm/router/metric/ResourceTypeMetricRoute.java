package at.uibk.dps.rm.router.metric;

import at.uibk.dps.rm.handler.RequestHandler;
import at.uibk.dps.rm.handler.metric.ResourceTypeMetricHandler;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;

public class ResourceTypeMetricRoute {
    public static void init(RouterBuilder router, ServiceProxyProvider serviceProxyProvider) {
        ResourceTypeMetricHandler handler = new ResourceTypeMetricHandler(serviceProxyProvider);
        RequestHandler requestHandler = new RequestHandler(handler);

        router
            .operation("listResourceTypeMetrics")
            .handler(requestHandler::getAllRequest);
    }
}
