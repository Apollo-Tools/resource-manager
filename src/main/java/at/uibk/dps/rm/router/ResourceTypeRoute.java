package at.uibk.dps.rm.router;

import at.uibk.dps.rm.handler.Resource.ResourceTypeHandler;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;

public class ResourceTypeRoute {
    public static void init(Vertx vertx, RouterBuilder router) {
        ResourceTypeHandler resourceTypeHandler = new ResourceTypeHandler(vertx);

        router
            .operation("createResourceType")
            .handler(resourceTypeHandler::post);

        router
            .operation("listResourceTypes")
            .handler(resourceTypeHandler::all);

        router
            .operation("getResourceType")
            .handler(resourceTypeHandler::get);

        router
            .operation("updateResourceType")
            .handler(resourceTypeHandler::patch);

        router
            .operation("deleteResourceType")
            .handler(resourceTypeHandler::delete);
    }
}
