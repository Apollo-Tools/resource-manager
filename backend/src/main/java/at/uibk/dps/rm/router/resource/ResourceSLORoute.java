package at.uibk.dps.rm.router.resource;

import at.uibk.dps.rm.handler.ResultHandler;
import at.uibk.dps.rm.handler.metric.MetricChecker;
import at.uibk.dps.rm.handler.metric.MetricValueChecker;
import at.uibk.dps.rm.handler.resource.ResourceChecker;
import at.uibk.dps.rm.handler.resource.ResourceSLOHandler;
import at.uibk.dps.rm.handler.resource.SLOInputHandler;
import at.uibk.dps.rm.router.Route;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;

/**
 * Used to initialize the resource slo route.
 *
 * @author matthi-g
 */
public class ResourceSLORoute implements Route {
    @Override
    public void init(RouterBuilder router, ServiceProxyProvider serviceProxyProvider) {
        ResourceChecker resourceChecker = new ResourceChecker(serviceProxyProvider.getResourceService());
        MetricChecker metricChecker = new MetricChecker(serviceProxyProvider.getMetricService());
        MetricValueChecker metricValueChecker = new MetricValueChecker(serviceProxyProvider
                .getMetricValueService());
        ResourceSLOHandler sloHandler = new ResourceSLOHandler(resourceChecker, metricChecker, metricValueChecker);
        ResultHandler resultHandler = new ResultHandler(null);

        router
                .operation("listResourcesBySLOs")
                .handler(SLOInputHandler::validateGetResourcesBySLOsRequest)
                .handler(rc -> resultHandler.handleFindAllRequest(rc, sloHandler.getResourceBySLOs(rc)));
    }
}
