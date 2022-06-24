package at.uibk.dps.rm.router;

import at.uibk.dps.rm.handler.Resource.ResourceHandler;
import at.uibk.dps.rm.handler.Resource.ResourceTypeErrorHandler;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.web.Router;

public class ResourceRouter {
    public static Router router(Vertx vertx) {
        Router router = Router.router(vertx);

        ResourceHandler resourceTypeHandler = new ResourceHandler(vertx);

        router.post("/")
            .produces("application/json")
            .handler(ResourceTypeErrorHandler::validatePostPatchRequest)
            .handler(resourceTypeHandler::post);

        router.get("/")
            .produces("application/json")
            .handler(resourceTypeHandler::all);

        router.get("/:resourceId")
            .produces("application/json")
            .handler(resourceTypeHandler::get);

        router.patch("/:resourceId")
            .handler(ResourceTypeErrorHandler::validatePostPatchRequest)
            .handler(resourceTypeHandler::patch);

        router.delete("/:resourceId")
            .handler(resourceTypeHandler::delete);

        return router;
    }
}
