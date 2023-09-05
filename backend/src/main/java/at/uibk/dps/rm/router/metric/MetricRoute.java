package at.uibk.dps.rm.router.metric;

import at.uibk.dps.rm.handler.ResultHandler;
import at.uibk.dps.rm.handler.metric.MetricHandler;
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
        MetricHandler metricHandler = new MetricHandler(serviceProxyProvider.getMetricService());
        ResultHandler resultHandler = new ResultHandler(metricHandler);

        router
            .operation("listMetrics")
            .handler(resultHandler::handleFindAllRequest);

        router
            .operation("getMetric")
            .handler(resultHandler::handleFindOneRequest);
    }
}
