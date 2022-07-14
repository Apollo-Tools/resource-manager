package at.uibk.dps.rm.router;

import at.uibk.dps.rm.handler.Metric.MetricHandler;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;

public class MetricRoute {
    public static void init(Vertx vertx, RouterBuilder router) {
        MetricHandler metricHandler = new MetricHandler(vertx);

        router
            .operation("createMetric")
            .handler(metricHandler::post);

        router
            .operation("listMetrics")
            .handler(metricHandler::all);

        router
            .operation("getMetric")
            .handler(metricHandler::get);

        router
            .operation("updateMetric")
            .handler(metricHandler::patch);

        router
            .operation("deleteMetric")
            .handler(metricHandler::delete);
    }
}
