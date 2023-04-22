package at.uibk.dps.rm.router.metric;

import at.uibk.dps.rm.handler.ResultHandler;
import at.uibk.dps.rm.handler.metric.MetricChecker;
import at.uibk.dps.rm.handler.metric.ResourceTypeMetricHandler;
import at.uibk.dps.rm.handler.resource.ResourceTypeChecker;
import at.uibk.dps.rm.router.Route;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;

/**
 * Used to initialise the resource type metric route.
 *
 * @author matthi-g
 */
public class ResourceTypeMetricRoute implements Route {
    @Override
    public void init(RouterBuilder router, ServiceProxyProvider serviceProxyProvider) {
        MetricChecker metricChecker = new MetricChecker(serviceProxyProvider.getMetricService());
        ResourceTypeChecker resourceTypeChecker =
            new ResourceTypeChecker(serviceProxyProvider.getResourceTypeService());
        ResourceTypeMetricHandler handler = new ResourceTypeMetricHandler(metricChecker, resourceTypeChecker);
        ResultHandler resultHandler = new ResultHandler(handler);

        router
            .operation("listResourceTypeMetrics")
            .handler(resultHandler::handleFindAllRequest);
    }
}
