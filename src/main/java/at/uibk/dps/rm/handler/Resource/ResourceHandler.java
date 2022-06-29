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
                    checkNewResourceTypeExists(rc, requestBody);
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
        HttpHelper.getLongPathParam(rc, "resourceId")
            .subscribe(id -> checkUpdateExists(rc, id),
                throwable -> rc.fail(500, throwable))
            .dispose();
    }

    public void delete(RoutingContext rc) {
        HttpHelper.getLongPathParam(rc, "resourceId")
            .subscribe(
                id -> checkDeleteResourceExists(rc, id),
                throwable -> rc.fail(500, throwable))
            .dispose();
    }

    private void checkNewResourceTypeExists(RoutingContext rc, JsonObject requestBody) {
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

    private void checkUpdateExists(RoutingContext rc, long id) {
        resourceService.findOne(id)
            .onComplete(updateHandler -> {
                if (updateHandler.failed()) {
                    rc.fail(500, new Throwable(updateHandler.cause()));
                }
                else if (updateHandler.result() == null) {
                    rc.fail(404, new Throwable("not found"));
                }
            })
            .onComplete(updateHandler -> {
                if (!rc.failed()) {
                    checkUpdateNoDuplicate(rc, updateHandler.result());
                }
            });
    }

    private void checkUpdateNoDuplicate(RoutingContext rc, JsonObject entity) {
        JsonObject requestBody = rc.body().asJsonObject();
        if (requestBody.containsKey("url")) {
            resourceService.existsOneByUrl(requestBody.getString("url"))
                .onComplete(duplicateHandler -> {
                    if (duplicateHandler.failed()){
                        rc.fail(500, duplicateHandler.cause());
                    }
                    else if (duplicateHandler.result()) {
                        rc.fail(409, new Throwable("already exists"));
                    } else {
                        checkResourceTypeExists(rc, requestBody, entity);
                    }
                });
        } else {
            checkResourceTypeExists(rc, requestBody, entity);
        }
    }


    private void checkResourceTypeExists(RoutingContext rc,JsonObject requestBody, JsonObject entity) {
        if (requestBody.containsKey("resource_type")) {
            resourceTypeService.existsOneById(requestBody
                .getJsonObject("resource_type")
                .getLong("type_id"))
                .onComplete(existsHandler -> {
                    if (existsHandler.failed()){
                        rc.fail(500, existsHandler.cause());
                    }
                    else if (!existsHandler.result()) {
                        rc.fail(404, new Throwable("not found"));
                    } else {
                        submitUpdate(rc, requestBody, entity);
                    }
                });
        } else {
            submitUpdate(rc, requestBody, entity);
        }
    }

    private void submitUpdate(RoutingContext rc, JsonObject requestBody,
        JsonObject entity) {
        for (String field : requestBody.fieldNames()) {
            entity.put(field, requestBody.getValue(field));
        }
        resourceService.update(entity)
            .onComplete(updateHandler -> {
                if (updateHandler.succeeded()) {
                    rc.response().setStatusCode(204).end();
                } else {
                    rc.fail(500, updateHandler.cause());
                }
            });
    }

    private void checkDeleteResourceExists(RoutingContext rc, long id) {
        resourceService.existsOneById(id)
            .onComplete(findHandler -> {
                if (findHandler.failed()) {
                    rc.fail(500, findHandler.cause());
                } else if (!findHandler.result()) {
                    rc.fail(404, new Throwable("not found"));
                } else {
                    submitDelete(rc, id);
                }
            });
    }

    private void submitDelete(RoutingContext rc, long id) {
        resourceService.delete(id)
            .onComplete(handler -> {
                if (handler.succeeded()) {
                    rc.response().setStatusCode(204).end();
                } else {
                    rc.fail(500, handler.cause());
                }
            });
    }
}
