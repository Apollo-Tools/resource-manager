package at.uibk.dps.rm.router.metric;

import at.uibk.dps.rm.handler.ResultHandler;
import at.uibk.dps.rm.handler.metric.MetricChecker;
import at.uibk.dps.rm.handler.metric.MetricHandler;
import at.uibk.dps.rm.handler.metric.MetricTypeChecker;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;

public class MetricRoute {
    public static void init(RouterBuilder router, ServiceProxyProvider serviceProxyProvider) {
        MetricChecker metricChecker = new MetricChecker(serviceProxyProvider.getMetricService());
        MetricTypeChecker metricTypeChecker = new MetricTypeChecker(serviceProxyProvider.getMetricTypeService());
        MetricHandler metricHandler = new MetricHandler(metricChecker, metricTypeChecker);
        ResultHandler resultHandler = new ResultHandler(metricHandler);

        router
            .operation("createMetric")
            .handler(resultHandler::handleSaveOneRequest);

        router
            .operation("listMetrics")
            .handler(resultHandler::handleFindAllRequest);

        router
            .operation("getMetric")
            .handler(resultHandler::handleFindOneRequest);

        router
            .operation("updateMetric")
            .handler(resultHandler::handleUpdateRequest);

        router
            .operation("deleteMetric")
            .handler(resultHandler::handleDeleteRequest);
    }
}
