package at.uibk.dps.rm.handler.resource;

import at.uibk.dps.rm.handler.*;
import at.uibk.dps.rm.service.rxjava3.resource.ResourceService;
import at.uibk.dps.rm.service.rxjava3.resource.ResourceTypeService;
import at.uibk.dps.rm.util.HttpHelper;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RoutingContext;

public class ResourceHandler extends ValidationHandler {

    private final ResourceTypeChecker resourceTypeChecker;

    public ResourceHandler(ResourceService resourceService, ResourceTypeService resourceTypeService) {
        super(new ResourceChecker(resourceService));
        resourceTypeChecker = new ResourceTypeChecker(resourceTypeService);
    }

    @Override
    public Single<JsonObject> postOne(RoutingContext rc) {
        JsonObject requestBody = rc.body().asJsonObject();
        return entityChecker.checkForDuplicateEntity(requestBody)
            .andThen(resourceTypeChecker.checkExistsOne(requestBody
                                                            .getJsonObject("resource_type")
                                                            .getLong("type_id")))
            .andThen(entityChecker.submitCreate(requestBody));
    }

    @Override
    public Completable updateOne(RoutingContext rc) {
        JsonObject requestBody = rc.body().asJsonObject();
        return HttpHelper.getLongPathParam(rc, "id")
            .flatMap(entityChecker::checkFindOne)
            .flatMap(result -> entityChecker.checkUpdateNoDuplicate(requestBody, result))
            .flatMap(result -> checkUpdateResourceTypeExists(requestBody, result))
            .flatMapCompletable(result -> entityChecker.submitUpdate(requestBody, result));
    }

    protected Single<JsonObject> getResourceBySLOs(RoutingContext rc) {
        return Single.just(new JsonObject("{\"out\": \"Hello World\"}"));
    }


    private Single<JsonObject> checkUpdateResourceTypeExists(JsonObject requestBody, JsonObject entity) {
        if (requestBody.containsKey("resource_type")) {
            return resourceTypeChecker.checkExistsOne(requestBody.getJsonObject("resource_type").getLong("type_id"))
                .andThen(Single.just(entity));
        }
        return Single.just(entity);
    }
}
