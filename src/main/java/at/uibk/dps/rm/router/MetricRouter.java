package at.uibk.dps.rm.router;

import at.uibk.dps.rm.handler.Metric.MetricErrorHandler;
import at.uibk.dps.rm.handler.Metric.MetricHandler;
import io.vertx.core.http.HttpMethod;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.web.Router;

public class MetricRouter {

    public static Router router(Vertx vertx) {
        Router router = Router.router(vertx);

        MetricHandler metricHandler = new MetricHandler(vertx);

        router.post("/")
            .produces("application/json")
            .handler(rc -> MetricErrorHandler.validatePostPatchRequest(rc, HttpMethod.POST))
            .handler(metricHandler::post);

        router.get("/")
            .produces("application/json")
            .handler(metricHandler::all);

        router.get("/:metricId")
            .produces("application/json")
            .handler(metricHandler::get);

        router.patch("/:metricId")
            .handler(rc -> MetricErrorHandler.validatePostPatchRequest(rc, HttpMethod.PATCH))
            .handler(metricHandler::patch);

        router.delete("/:metricId")
            .handler(metricHandler::delete);

        return router;
    }
}
