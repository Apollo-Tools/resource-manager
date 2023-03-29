package at.uibk.dps.rm.router.metric;

import at.uibk.dps.rm.handler.metric.MetricChecker;
import at.uibk.dps.rm.handler.metric.MetricValueChecker;
import at.uibk.dps.rm.handler.metric.MetricValueHandler;
import at.uibk.dps.rm.handler.RequestHandler;
import at.uibk.dps.rm.handler.resource.ResourceChecker;
import at.uibk.dps.rm.handler.resource.ResourceInputHandler;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;

public class ResourceMetricRoute {

    public static void init(RouterBuilder router, ServiceProxyProvider serviceProxyProvider) {
        MetricValueChecker metricValueChecker = new MetricValueChecker(serviceProxyProvider.getMetricValueService());
        MetricChecker metricChecker = new MetricChecker(serviceProxyProvider.getMetricService());
        ResourceChecker resourceChecker = new ResourceChecker(serviceProxyProvider.getResourceService());
        MetricValueHandler metricValueHandler = new MetricValueHandler(metricValueChecker, metricChecker,
            resourceChecker);
        RequestHandler metricValueRequestHandler = new RequestHandler(metricValueHandler);

        router
            .operation("addResourceMetrics")
            .handler(ResourceInputHandler::validateAddMetricsRequest)
            .handler(metricValueRequestHandler::postAllRequest);

        router
            .operation("listResourceMetrics")
            .handler(metricValueRequestHandler::getAllRequest);

        router
            .operation("updateMetricValue")
            .handler(metricValueRequestHandler::patchRequest);

        router
            .operation("deleteResourceMetric")
            .handler(metricValueRequestHandler::deleteRequest);
    }
}
