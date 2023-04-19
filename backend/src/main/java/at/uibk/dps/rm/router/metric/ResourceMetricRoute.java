package at.uibk.dps.rm.router.metric;

import at.uibk.dps.rm.handler.ResultHandler;
import at.uibk.dps.rm.handler.metric.MetricChecker;
import at.uibk.dps.rm.handler.metric.MetricValueChecker;
import at.uibk.dps.rm.handler.metric.MetricValueHandler;
import at.uibk.dps.rm.handler.resource.ResourceChecker;
import at.uibk.dps.rm.handler.resource.ResourceInputHandler;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;

public class ResourceMetricRoute {

    public static void init(RouterBuilder router, ServiceProxyProvider serviceProxyProvider) {
        MetricValueChecker metricValueChecker = new MetricValueChecker(serviceProxyProvider
            .getMetricValueService());
        MetricChecker metricChecker = new MetricChecker(serviceProxyProvider.getMetricService());
        ResourceChecker resourceChecker = new ResourceChecker(serviceProxyProvider.getResourceService());
        MetricValueHandler metricValueHandler = new MetricValueHandler(metricValueChecker, metricChecker,
            resourceChecker);
        ResultHandler resultHandler = new ResultHandler(metricValueHandler);

        router
            .operation("addResourceMetrics")
            .handler(ResourceInputHandler::validateAddMetricsRequest)
            .handler(resultHandler::handleSaveAllRequest);

        router
            .operation("listResourceMetrics")
            .handler(resultHandler::handleFindAllRequest);

        router
            .operation("updateMetricValue")
            .handler(resultHandler::handleUpdateRequest);

        router
            .operation("deleteResourceMetric")
            .handler(resultHandler::handleDeleteRequest);
    }
}
