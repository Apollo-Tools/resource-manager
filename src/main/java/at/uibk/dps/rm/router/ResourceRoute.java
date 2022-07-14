package at.uibk.dps.rm.router;

import at.uibk.dps.rm.handler.Resource.ResourceHandler;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;

public class ResourceRoute {
    public static void init(Vertx vertx, RouterBuilder router) {
        ResourceHandler resourceHandler = new ResourceHandler(vertx);

        router
            .operation("createResource")
            .handler(resourceHandler::post);

        router
            .operation("addResourceMetrics")
            .handler(resourceHandler::postMetrics);

        router
            .operation("listResources")
            .handler(resourceHandler::all);

        router
            .operation("getResource")
            .handler(resourceHandler::get);

        router
            .operation("listResourceMetrics")
            .handler(resourceHandler::getMetrics);

        router
            .operation("updateResource")
            .handler(resourceHandler::patch);

        router
            .operation("deleteResource")
            .handler(resourceHandler::delete);

        router
            .operation("deleteResourceMetric")
            .handler(resourceHandler::deleteMetric);
    }
}
