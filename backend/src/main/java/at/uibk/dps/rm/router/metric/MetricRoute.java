package at.uibk.dps.rm.router.metric;

import at.uibk.dps.rm.handler.ResultHandler;
import at.uibk.dps.rm.handler.metric.MetricChecker;
import at.uibk.dps.rm.handler.metric.MetricHandler;
import at.uibk.dps.rm.handler.metric.MetricTypeChecker;
import at.uibk.dps.rm.router.Route;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;

/**
 * Used to initialise the metric route.
 *
 * @author matthi-g
 */
public class MetricRoute implements Route {
    @Override
    public void init(RouterBuilder router, ServiceProxyProvider serviceProxyProvider) {
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
