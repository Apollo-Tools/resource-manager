package at.uibk.dps.rm.handler;

import at.uibk.dps.rm.service.resource.ResourceTypeService;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.web.RoutingContext;

public class ResourceTypeHandler {

    private final ResourceTypeService resourceTypeService;

    public ResourceTypeHandler(Vertx vertx) {
        resourceTypeService = ResourceTypeService.createProxy(vertx.getDelegate(),
                "resource-type-service-address");
    }

    public void post(RoutingContext rc) {
        resourceTypeService.save(rc.body().asJsonObject())
            .onComplete(handler -> {
                if (handler.succeeded()) {
                    rc.response()
                        .setStatusCode(201)
                        .end(handler.result().encodePrettily());
                } else {
                    rc.fail(500, handler.cause());
                }
            });
    }

    public void get(RoutingContext rc) {
        int id = Integer.parseInt(rc.pathParam("resourceTypeId"));
        resourceTypeService.findOne(id)
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

    public void all(RoutingContext rc) {
        resourceTypeService.findAll()
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

    public void put(RoutingContext rc) {
        resourceTypeService.update(rc.body().asJsonObject())
            .onComplete(handler -> {
                if (handler.succeeded()) {
                    rc.response().setStatusCode(204).end();
                } else {
                    rc.fail(500, handler.cause());
                }
            });
    }

    public void delete(RoutingContext rc) {
        int id = Integer.parseInt(rc.pathParam("resourceTypeId"));
        resourceTypeService.delete(id)
            .onComplete(handler -> {
                if (handler.succeeded()) {
                    rc.response().setStatusCode(204).end();
                } else {
                    rc.fail(500, handler.cause());
                }
            });
    }
}
