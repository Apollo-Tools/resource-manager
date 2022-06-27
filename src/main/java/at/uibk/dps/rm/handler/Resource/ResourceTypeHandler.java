package at.uibk.dps.rm.handler.Resource;

import at.uibk.dps.rm.handler.ResultHandler;
import at.uibk.dps.rm.service.resource.ResourceTypeService;
import at.uibk.dps.rm.util.HttpHelper;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.web.RoutingContext;

public class ResourceTypeHandler {

    private final ResourceTypeService resourceTypeService;

    public ResourceTypeHandler(Vertx vertx) {
        resourceTypeService = ResourceTypeService.createProxy(vertx.getDelegate(),
                "resource-type-service-address");
    }

    public void post(RoutingContext rc) {
        JsonObject requestBody = rc.body().asJsonObject();
        resourceTypeService.findOneByResourceType(requestBody.getString("resource_type"))
            .onComplete(findHandler -> {
                JsonObject result = findHandler.result();
                if (findHandler.succeeded()) {
                    submitCreate(rc, requestBody, result);
                } else {
                    rc.fail(500, findHandler.cause());
                }
            });
    }

    public void get(RoutingContext rc) {
        HttpHelper.getLongPathParam(rc, "resourceTypeId")
            .subscribe(
                id ->  resourceTypeService.findOne(id)
                    .onComplete(
                        handler -> ResultHandler.handleGetOneRequest(rc, handler)),
                throwable -> rc.fail(500, throwable))
            .dispose();
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

    public void patch(RoutingContext rc) {
        HttpHelper.getLongPathParam(rc, "resourceTypeId")
            .subscribe(id -> checkUpdateExists(rc, id),
                throwable -> rc.fail(500, throwable))
            .dispose();
    }

    public void delete(RoutingContext rc) {
        HttpHelper.getLongPathParam(rc, "resourceTypeId")
            .subscribe(id ->  resourceTypeService.delete(id)
                    .onComplete(handler -> {
                        if (handler.succeeded()) {
                            rc.response().setStatusCode(204).end();
                        } else {
                            rc.fail(500, handler.cause());
                        }
                    }),
                throwable -> rc.fail(500, throwable))
            .dispose();
    }

    private void submitCreate(RoutingContext rc, JsonObject requestBody, JsonObject existingEntity) {
        if (existingEntity != null) {
            rc.fail(409, new Throwable("already exists"));
        } else {
            resourceTypeService.save(requestBody)
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
    }

    private void checkUpdateExists(RoutingContext rc, long id) {
        resourceTypeService.findOne(id)
            .onComplete(updateHandler -> {
                JsonObject entity = updateHandler.result();
                if (updateHandler.failed()) {
                    rc.fail(500, new Throwable(updateHandler.cause()));
                }
                else if (updateHandler.result() != null) {
                    checkUpdateNoDuplicate(rc, entity);
                } else {
                    rc.fail(404, new Throwable("not found"));
                }
            });
    }

    private void checkUpdateNoDuplicate (RoutingContext rc, JsonObject entity) {
        JsonObject requestBody = rc.body().asJsonObject();
        resourceTypeService.findOneByResourceType(requestBody.getString("resource_type"))
            .onComplete(duplicateHandler -> {
                if (duplicateHandler.failed()){
                    rc.fail(500, duplicateHandler.cause());
                }
                else if (duplicateHandler.result() == null) {
                    submitUpdate(rc, requestBody, entity);
                } else {
                    rc.fail(409, new Throwable("already exists"));
                }
            });
    }

    private void submitUpdate(RoutingContext rc, JsonObject requestBody,
        JsonObject entity) {
        entity.put("resource_type", requestBody.getValue("resource_type"));
        resourceTypeService.update(entity)
            .onComplete(updateHandler -> {
                if (updateHandler.succeeded()) {
                    rc.response().setStatusCode(204).end();
                } else {
                    rc.fail(500, updateHandler.cause());
                }
            });
    }
}
