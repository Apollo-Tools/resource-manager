package at.uibk.dps.rm.router;

import at.uibk.dps.rm.handler.Metric.MetricHandler;
import at.uibk.dps.rm.handler.RequestHandler;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;

public class MetricRoute {
    public static void init(RouterBuilder router, ServiceProxyProvider serviceProxyProvider) {
        MetricHandler metricHandler = new MetricHandler(serviceProxyProvider.getMetricService());
        RequestHandler requestHandler = new RequestHandler(metricHandler);

        router
            .operation("createMetric")
            .handler(requestHandler::postRequest);

        router
            .operation("listMetrics")
            .handler(requestHandler::getAllRequest);

        router
            .operation("getMetric")
            .handler(requestHandler::getRequest);

        router
            .operation("updateMetric")
            .handler(requestHandler::patchRequest);

        router
            .operation("deleteMetric")
            .handler(requestHandler::deleteRequest);
    }
}
