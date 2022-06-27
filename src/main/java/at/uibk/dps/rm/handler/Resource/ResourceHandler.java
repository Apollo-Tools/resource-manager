package at.uibk.dps.rm.handler.Resource;

import at.uibk.dps.rm.handler.ResultHandler;
import at.uibk.dps.rm.service.resource.ResourceService;
import at.uibk.dps.rm.service.resource.ResourceTypeService;
import at.uibk.dps.rm.util.HttpHelper;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.web.RoutingContext;

public class ResourceHandler {
    private final ResourceService resourceService;

    private final ResourceTypeService resourceTypeService;

    public ResourceHandler(Vertx vertx) {
        resourceService = ResourceService.createProxy(vertx.getDelegate(),
            "resource-service-address");
        resourceTypeService = ResourceTypeService.createProxy(vertx.getDelegate(),
            "resource-type-service-address");
    }

    public void post(RoutingContext rc) {
        JsonObject requestBody = rc.body().asJsonObject();
        resourceService.existsOneByUrl(requestBody.getString("url"))
            .onComplete(existsHandler -> {
                if (existsHandler.failed()) {
                    rc.fail(500, existsHandler.cause());
                } else if (existsHandler.result()) {
                    rc.fail(409, new Throwable("already exists"));
                }else {
                    checkResourceTypeExists(rc, requestBody);
                }
            });
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

    private void checkResourceTypeExists(RoutingContext rc, JsonObject requestBody) {
        resourceTypeService.findOne(requestBody.getLong("resource_type"))
            .onComplete(findHandler -> {
                JsonObject result = findHandler.result();
                if (findHandler.failed()) {
                    rc.fail(500, findHandler.cause());
                } else if (result != null) {
                    submitCreate(rc, requestBody);
                } else {
                    rc.fail(404, new Throwable("not found"));
                }
            });
    }

    private void submitCreate(RoutingContext rc, JsonObject requestBody) {
        resourceService.save(requestBody)
            .onComplete(handler -> ResultHandler.handleSaveRequest(rc, handler));
    }


}
