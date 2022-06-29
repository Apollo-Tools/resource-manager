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
        checkForDuplicateUrl(rc, requestBody.getString("url"))
            .onComplete(existsHandler -> {
                if (!rc.failed()) {
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
            .onComplete(handler -> ResultHandler.handleGetAllRequest(rc, handler));
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

    private void submitCreate(RoutingContext rc, JsonObject requestBody) {
        resourceService.save(requestBody)
            .onComplete(handler -> ResultHandler.handleSaveRequest(rc, handler));
    }

    private void submitUpdate(RoutingContext rc, JsonObject requestBody,
        JsonObject entity) {
        for (String field : requestBody.fieldNames()) {
            entity.put(field, requestBody.getValue(field));
        }
        resourceService.update(entity)
            .onComplete(updateHandler -> ResultHandler.handleUpdateDeleteRequest(rc, updateHandler));
    }

    private void submitDelete(RoutingContext rc, long id) {
        resourceService.delete(id)
            .onComplete(deleteHandler -> ResultHandler.handleUpdateDeleteRequest(rc, deleteHandler));
    }

    private Future<Boolean> checkForDuplicateUrl(RoutingContext rc, String url) {
        return resourceService.existsOneByUrl(url)
            .onComplete(duplicateHandler -> ErrorHandler.handleDuplicates(rc, duplicateHandler));
    }

    private Future<Boolean> checkResourceTypeExists(RoutingContext rc, long id) {
        return resourceTypeService.existsOneById(id)
            .onComplete(updateHandler -> ErrorHandler.handleExistsOne(rc, updateHandler));
    }

    private void checkNewResourceTypeExists(RoutingContext rc, JsonObject requestBody) {
        checkResourceTypeExists(rc, requestBody.getJsonObject("resource_type").getLong("type_id"))
            .onComplete(findHandler -> {
                if (!rc.failed()) {
                    submitCreate(rc, requestBody);
                }
            });
    }

    private void checkUpdateExists(RoutingContext rc, long id) {
        resourceService.findOne(id)
            .onComplete(updateHandler -> ErrorHandler.handleFindOne(rc, updateHandler))
            .onComplete(updateHandler -> {
                if (!rc.failed()) {
                    checkUpdateNoDuplicate(rc, updateHandler.result());
                }
            });
    }

    private void checkUpdateNoDuplicate(RoutingContext rc, JsonObject entity) {
        JsonObject requestBody = rc.body().asJsonObject();
        if (requestBody.containsKey("url")) {
            checkForDuplicateUrl(rc, requestBody.getString("url"))
                .onComplete(duplicateHandler -> {
                    if (!rc.failed()) {
                        checkUpdateResourceTypeExists(rc, requestBody, entity);
                    }
                });
        } else {
            checkUpdateResourceTypeExists(rc, requestBody, entity);
        }
    }


    private void checkUpdateResourceTypeExists(RoutingContext rc,JsonObject requestBody, JsonObject entity) {
        if (requestBody.containsKey("resource_type")) {
            checkResourceTypeExists(rc, requestBody.getJsonObject("resource_type").getLong("type_id"))
                .onComplete(findHandler -> {
                    if (!rc.failed()) {
                        submitUpdate(rc, requestBody, entity);
                    }
                });
        } else {
            submitUpdate(rc, requestBody, entity);
        }
    }

    private void checkDeleteResourceExists(RoutingContext rc, long id) {
        resourceService.existsOneById(id)
            .onComplete(findHandler -> ErrorHandler.handleExistsOne(rc, findHandler))
            .onComplete(findHandler -> {
                if (!rc.failed()) {
                    submitDelete(rc, id);
                }
            });
    }
}
