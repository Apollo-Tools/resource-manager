package at.uibk.dps.rm.handler.Resource;

import at.uibk.dps.rm.handler.ErrorHandler;
import at.uibk.dps.rm.handler.RequestHandler;
import at.uibk.dps.rm.handler.ResultHandler;
import at.uibk.dps.rm.service.rxjava3.resource.ResourceService;
import at.uibk.dps.rm.service.rxjava3.resource.ResourceTypeService;
import at.uibk.dps.rm.util.HttpHelper;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.web.RoutingContext;

public class ResourceTypeHandler extends RequestHandler {

    private final ResourceTypeService resourceTypeService;

    private final ResourceService resourceService;

    public ResourceTypeHandler(Vertx vertx) {
        super(ResourceTypeService.createProxy(vertx,"resource-type-service-address"));
        resourceTypeService = (ResourceTypeService) super.service;
        resourceService = ResourceService.createProxy(vertx, "resource-service-address");
    }

    @Override
    public Disposable post(RoutingContext rc) {
        JsonObject requestBody = rc.body().asJsonObject();
        return checkForDuplicateResourceType(requestBody.getString("resource_type"))
            .andThen(submitCreate(requestBody))
            .subscribe(result -> ResultHandler.handleSaveRequest(rc, result),
                throwable -> ErrorHandler.handleRequestError(rc, throwable));
    }

    @Override
    public Disposable patch(RoutingContext rc) {
        JsonObject requestBody = rc.body().asJsonObject();
        return HttpHelper.getLongPathParam(rc, "id")
            .flatMap(super::checkFindOne)
            .flatMap(updateEntity -> checkUpdateNoDuplicate(requestBody, updateEntity))
            .flatMapCompletable(updateEntity -> submitUpdate(requestBody, updateEntity))
            .subscribe(() -> ResultHandler.handleSaveAllUpdateDeleteRequest(rc),
                throwable -> ErrorHandler.handleRequestError(rc, throwable));
    }

    @Override
    public Disposable delete(RoutingContext rc) {
        return HttpHelper.getLongPathParam(rc, "id")
            .flatMap(id -> checkFindOne(id)
                .flatMap(this::checkDeleteResourceTypeIsUsed)
                .map(result -> id))
            .flatMapCompletable(this::submitDelete)
            .subscribe(() -> ResultHandler.handleSaveAllUpdateDeleteRequest(rc),
                throwable -> ErrorHandler.handleRequestError(rc, throwable));
    }

    private Completable checkForDuplicateResourceType(String resourceType) {
        Single<Boolean> existsOneByResourceType = resourceTypeService.existsOneByResourceType(resourceType);
        return ErrorHandler.handleDuplicates(existsOneByResourceType).ignoreElement();
    }

    private Single<JsonObject> checkUpdateNoDuplicate(JsonObject requestBody, JsonObject entity) {
        if (requestBody.containsKey("resource_type")) {
            return checkForDuplicateResourceType(requestBody.getString("resource_type"))
                .andThen(Single.just(entity));
        }
        return Single.just(entity);
    }

    private Single<Boolean> checkDeleteResourceTypeIsUsed(JsonObject entity) {
        Single<Boolean> existsOneByResourceType = resourceService.existsOneByResourceType(entity
                .getLong("type_id"));
        return ErrorHandler.handleUsedByOtherEntity(existsOneByResourceType);
    }
}
