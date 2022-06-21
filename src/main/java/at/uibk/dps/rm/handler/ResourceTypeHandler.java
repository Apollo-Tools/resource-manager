package at.uibk.dps.rm.handler;

import at.uibk.dps.rm.service.resource.ResourceTypeService;
import io.reactivex.rxjava3.core.Completable;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.web.RoutingContext;

public class ResourceTypeHandler {

    private final ResourceTypeService resourceTypeService;

    public ResourceTypeHandler(Vertx vertx) {
        resourceTypeService = ResourceTypeService.createProxy(vertx.getDelegate(),
                "resource-type-service-address");
    }

    public Completable post(RoutingContext rc) {
        return rc.response().end();
    }

    public void all(RoutingContext rc) {
        resourceTypeService.findAll()
            .onComplete(handler -> {
                if (handler.succeeded()) {
                    rc.response().end(handler.result().encodePrettily());
                }
                else {
                    rc.fail(500);
                }
            });
    }

    public Completable get(RoutingContext rc) {
        return rc.response().end();
    }

    public Completable patch(RoutingContext rc) {
        return rc.response().end();
    }

    public Completable delete(RoutingContext rc) {
        return rc.response().end();
    }
}
