package at.uibk.dps.rm.handler.Resource;

import at.uibk.dps.rm.handler.ErrorHandler;
import at.uibk.dps.rm.handler.RequestHandler;
import at.uibk.dps.rm.service.resource.ResourceService;
import at.uibk.dps.rm.service.resource.ResourceTypeService;
import at.uibk.dps.rm.util.HttpHelper;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.web.RoutingContext;

public class ResourceTypeHandler extends RequestHandler {

    private final ResourceTypeService resourceTypeService;

    private final ResourceService resourceService;

    public ResourceTypeHandler(Vertx vertx) {
        super(ResourceTypeService.createProxy(vertx.getDelegate(),"resource-type-service-address"));
        resourceTypeService = (ResourceTypeService) super.service;
        resourceService = ResourceService.createProxy(vertx.getDelegate(),
            "resource-service-address");
    }

    @Override
    public void post(RoutingContext rc) {
        JsonObject requestBody = rc.body().asJsonObject();
        checkForDuplicateResourceType(rc, requestBody)
            .onComplete(findHandler -> {
                if (!rc.failed()) {
                    submitCreate(rc, requestBody);
                }
            });
    }

    @Override
    public void patch(RoutingContext rc) {
        HttpHelper.getLongPathParam(rc, "id")
            .subscribe(id -> checkUpdateExists(rc, id),
                throwable -> rc.fail(500, throwable))
            .dispose();
    }

    @Override
    public void delete(RoutingContext rc) {
        HttpHelper.getLongPathParam(rc, "id")
            .subscribe(
                id ->  checkDeleteResourceTypeExists(rc, id),
                throwable -> rc.fail(500, throwable))
            .dispose();
    }

    private Future<Boolean> checkForDuplicateResourceType(RoutingContext rc, JsonObject requestBody) {
        return resourceTypeService.existsOneByResourceType(requestBody.getString("resource_type"))
            .onComplete(duplicateHandler -> ErrorHandler.handleDuplicates(rc, duplicateHandler));
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
