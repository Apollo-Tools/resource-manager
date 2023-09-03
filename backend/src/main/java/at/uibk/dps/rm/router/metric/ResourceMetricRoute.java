package at.uibk.dps.rm.router.metric;

import at.uibk.dps.rm.rx.handler.ResultHandler;
import at.uibk.dps.rm.rx.handler.metric.MetricValueHandler;
import at.uibk.dps.rm.rx.handler.resource.ResourceInputHandler;
import at.uibk.dps.rm.router.Route;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;

/**
 * Used to initialise the resource metric route.
 *
 * @author matthi-g
 */
public class ResourceMetricRoute implements Route {

    @Override
    public void init(RouterBuilder router, ServiceProxyProvider serviceProxyProvider) {
        MetricValueHandler metricValueHandler = new MetricValueHandler(serviceProxyProvider.getMetricValueService());
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
