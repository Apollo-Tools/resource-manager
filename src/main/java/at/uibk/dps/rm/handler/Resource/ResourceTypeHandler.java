package at.uibk.dps.rm.handler.Resource;

import at.uibk.dps.rm.handler.ErrorHandler;
import at.uibk.dps.rm.handler.ResultHandler;
import at.uibk.dps.rm.service.resource.ResourceService;
import at.uibk.dps.rm.service.resource.ResourceTypeService;
import at.uibk.dps.rm.util.HttpHelper;
import io.vertx.core.Future;
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
        checkForDuplicateResourceType(rc, requestBody)
            .onComplete(findHandler -> {
                if (!rc.failed()) {
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
            .onComplete(handler -> ResultHandler.handleGetAllRequest(rc, handler));
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

    private void submitUpdate(RoutingContext rc, JsonObject requestBody,
        JsonObject entity) {
        entity.put("resource_type", requestBody.getValue("resource_type"));
        resourceTypeService.update(entity)
            .onComplete(updateHandler -> ResultHandler.handleUpdateDeleteRequest(rc, updateHandler));
    }

    private void submitDelete(RoutingContext rc, long id) {
        resourceTypeService.delete(id)
            .onComplete(deleteHandler -> ResultHandler.handleUpdateDeleteRequest(rc, deleteHandler));
    }

    private Future<Boolean> checkForDuplicateResourceType(RoutingContext rc, JsonObject requestBody) {
        return resourceTypeService.existsOneByResourceType(requestBody.getString("resource_type"))
            .onComplete(duplicateHandler -> ErrorHandler.handleDuplicates(rc, duplicateHandler));
    }

    private Future<JsonObject> checkFindOne(RoutingContext rc, long id) {
        return resourceTypeService.findOne(id)
            .onComplete(updateHandler -> ErrorHandler.handleFindOne(rc, updateHandler));
    }

    private void checkUpdateExists(RoutingContext rc, long id) {
        checkFindOne(rc, id)
            .onComplete(updateHandler -> {
                if (!rc.failed()) {
                    checkUpdateNoDuplicate(rc, updateHandler.result());
                }
            });
    }

    private void checkUpdateNoDuplicate (RoutingContext rc, JsonObject entity) {
        JsonObject requestBody = rc.body().asJsonObject();
        checkForDuplicateResourceType(rc, requestBody)
            .onComplete(duplicateHandler -> {
                if (!rc.failed()) {
                    submitUpdate(rc, requestBody, entity);
                }
            });
    }

    private void checkDeleteResourceTypeExists(RoutingContext rc, long id) {
        checkFindOne(rc, id)
            .onComplete(findHandler -> {
                if (!rc.failed()) {
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
}
