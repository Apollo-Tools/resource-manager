package at.uibk.dps.rm.router;

import at.uibk.dps.rm.handler.ResourceTypeHandler;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.web.Router;
import io.vertx.serviceproxy.ServiceProxyBuilder;

public class ResourceTypeRouter {
    public static Router router(Vertx vertx) {
        Router router = Router.router(vertx);

        ResourceTypeHandler resourceTypeHandler = new ResourceTypeHandler(vertx);
        ServiceProxyBuilder builder = new ServiceProxyBuilder(vertx.getDelegate())
                .setAddress("resource-type-service-address");

        router.post("/")
                .blockingHandler(resourceTypeHandler::post);

        router.get("/")
                .handler(resourceTypeHandler::all);

        router.get("/:resourceType_id")
                .handler(resourceTypeHandler::get);

        router.patch("/:resourceType_id")
                .handler(resourceTypeHandler::patch);

        router.delete("/:resourceType_id")
                .handler(resourceTypeHandler::delete);

        return router;
    }
}
