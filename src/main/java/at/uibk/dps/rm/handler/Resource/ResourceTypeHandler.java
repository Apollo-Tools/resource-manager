package at.uibk.dps.rm.handler.Resource;

import at.uibk.dps.rm.handler.ResultHandler;
import at.uibk.dps.rm.service.resource.ResourceService;
import at.uibk.dps.rm.service.resource.ResourceTypeService;
import at.uibk.dps.rm.util.HttpHelper;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.web.RoutingContext;

public class ResourceTypeHandler {

    private final ResourceTypeService resourceTypeService;

    private final ResourceService resourceService;

    public ResourceTypeHandler(Vertx vertx) {
        resourceTypeService = ResourceTypeService.createProxy(vertx.getDelegate(),
            "resource-type-service-address");
        resourceService = ResourceService.createProxy(vertx.getDelegate(),
            "resource-service-address");
    }

    public void post(RoutingContext rc) {
        JsonObject requestBody = rc.body().asJsonObject();
        resourceTypeService.findOneByResourceType(requestBody.getString("resource_type"))
            .onComplete(findHandler -> {
                JsonObject result = findHandler.result();
                if (findHandler.failed()) {
                    rc.fail(500, findHandler.cause());
                } else if (result != null) {
                    rc.fail(409, new Throwable("already exists"));
                } else {
                    submitCreate(rc, requestBody);
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
            .subscribe(
                id ->  checkDeleteResourceTypeExists(rc, id),
                throwable -> rc.fail(500, throwable))
            .dispose();
    }

    private void submitCreate(RoutingContext rc, JsonObject requestBody) {
        resourceTypeService.save(requestBody)
            .onComplete(handler -> ResultHandler.handleSaveRequest(rc, handler));
    }

    private void checkUpdateExists(RoutingContext rc, long id) {
        resourceTypeService.findOne(id)
            .onComplete(updateHandler -> {
                JsonObject entity = updateHandler.result();
                if (updateHandler.failed()) {
                    rc.fail(500, new Throwable(updateHandler.cause()));
                }
                else if (updateHandler.result() == null) {
                    rc.fail(404, new Throwable("not found"));
                } else {
                    checkUpdateNoDuplicate(rc, entity);
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

    private void checkDeleteResourceTypeExists(RoutingContext rc, long id) {
        resourceTypeService.findOne(id)
            .onComplete(findHandler -> {
                if (findHandler.failed()) {
                    rc.fail(500, findHandler.cause());
                } else if (findHandler.result() == null) {
                    rc.fail(404, new Throwable("not found"));
                } else {
                    checkDeleteResourceTypeIsUsed(rc, findHandler.result(), id);
                }
            });
    }

    private void checkDeleteResourceTypeIsUsed(RoutingContext rc, JsonObject entity, long id) {
        resourceService.existsOneByResourceType(entity
                .getLong("type_id"))
            .onComplete(existsHandler -> {
                if (existsHandler.failed()) {
                    rc.fail(500, existsHandler.cause());
                } else if (existsHandler.result()) {
                    rc.fail(409, new Throwable("entity is used by other entities"));
                } else {
                    submitDelete(rc, id);
                }
            });
    }
    private void submitDelete(RoutingContext rc, long id) {
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
