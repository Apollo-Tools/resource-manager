package at.uibk.dps.rm.handler.Resource;

import at.uibk.dps.rm.handler.ResultHandler;
import at.uibk.dps.rm.service.resource.ResourceService;
import at.uibk.dps.rm.util.HttpHelper;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.web.RoutingContext;

public class ResourceHandler {
    private final ResourceService resourceService;

    public ResourceHandler(Vertx vertx) {
        resourceService = ResourceService.createProxy(vertx.getDelegate(),
            "resource-service-address");
    }

    public void post(RoutingContext rc) {
        rc.end();
    }

    public void get(RoutingContext rc) {
        HttpHelper.getLongPathParam(rc, "resourceId")
            .subscribe(
                id ->  resourceService.findOne(id)
                    .onComplete(
                        handler -> ResultHandler.handleGetOneRequest(rc, handler)),
                throwable -> rc.fail(500, throwable))
            .dispose();
    }

    public void all(RoutingContext rc) {
        resourceService.findAll()
            .onComplete(handler -> {
                if (handler.succeeded()) {
                    rc.response()
                        .setStatusCode(200)
                        .end(handler.result().encodePrettily());
                } else {
                    rc.fail(500, handler.cause());
                }
            });
    }

    public void patch(RoutingContext rc) {
        rc.end();
    }

    public void delete(RoutingContext rc) {
        rc.end();
    }
}
