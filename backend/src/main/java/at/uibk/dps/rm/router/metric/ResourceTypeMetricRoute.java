package at.uibk.dps.rm.router.metric;

import at.uibk.dps.rm.handler.ResultHandler;
import at.uibk.dps.rm.handler.metric.MetricChecker;
import at.uibk.dps.rm.handler.metric.ResourceTypeMetricHandler;
import at.uibk.dps.rm.handler.resource.ResourceTypeChecker;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;

public class ResourceTypeMetricRoute {
    public static void init(final RouterBuilder router, final ServiceProxyProvider serviceProxyProvider) {
        final MetricChecker metricChecker = new MetricChecker(serviceProxyProvider.getMetricService());
        final ResourceTypeChecker resourceTypeChecker =
            new ResourceTypeChecker(serviceProxyProvider.getResourceTypeService());
        final ResourceTypeMetricHandler handler = new ResourceTypeMetricHandler(metricChecker, resourceTypeChecker);
        final ResultHandler resultHandler = new ResultHandler(handler);

        router
            .operation("listResourceTypeMetrics")
            .handler(resultHandler::handleFindAllRequest);
    }
}
