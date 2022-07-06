package at.uibk.dps.rm.router;

import at.uibk.dps.rm.handler.Resource.ResourceHandler;
import at.uibk.dps.rm.handler.Resource.ResourceErrorHandler;
import io.vertx.core.http.HttpMethod;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.web.Router;

public class ResourceRouter {
    public static Router router(Vertx vertx) {
        Router router = Router.router(vertx);

        ResourceHandler resourceHandler = new ResourceHandler(vertx);

        router.post("/")
            .produces("application/json")
            .handler(rc -> ResourceErrorHandler.validatePostPatchRequest(rc, HttpMethod.POST))
            .handler(resourceHandler::post);

        router.post("/:resourceId/metrics")
            .handler(ResourceErrorHandler::validateAddMetricsRequest)
            .handler(resourceHandler::postMetrics);

        router.get("/")
            .produces("application/json")
            .handler(resourceHandler::all);

        router.get("/:resourceId")
            .produces("application/json")
            .handler(resourceHandler::get);

        router.patch("/:resourceId")
            .handler(rc -> ResourceErrorHandler.validatePostPatchRequest(rc, HttpMethod.PATCH))
            .handler(resourceHandler::patch);

        router.delete("/:resourceId")
            .handler(resourceHandler::delete);

        return router;
    }
}
