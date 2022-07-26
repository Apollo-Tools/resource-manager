package at.uibk.dps.rm.router;

import at.uibk.dps.rm.handler.Metric.MetricValueHandler;
import at.uibk.dps.rm.handler.RequestHandler;
import at.uibk.dps.rm.handler.Resource.ResourceInputHandler;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;

public class ResourceMetricRoute {

    public static void init(RouterBuilder router, ServiceProxyProvider serviceProxyProvider) {
        MetricValueHandler metricValueHandler = new MetricValueHandler(serviceProxyProvider.getMetricValueService(),
            serviceProxyProvider.getMetricService(), serviceProxyProvider.getResourceService());
        RequestHandler metricValueRequestHandler = new RequestHandler(metricValueHandler);

        router
            .operation("addResourceMetrics")
            .handler(ResourceInputHandler::validateAddMetricsRequest)
            .handler(metricValueRequestHandler::postAllRequest);

        router
            .operation("listResourceMetrics")
            .handler(metricValueRequestHandler::getAllRequest);

        router
            .operation("deleteResourceMetric")
            .handler(metricValueRequestHandler::deleteRequest);
    }
}
