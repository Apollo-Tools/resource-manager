package at.uibk.dps.rm.handler.resource;

import at.uibk.dps.rm.handler.*;
import at.uibk.dps.rm.util.HttpHelper;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RoutingContext;

public class ResourceHandler extends ValidationHandler {

    private final ResourceTypeChecker resourceTypeChecker;

    public ResourceHandler(ResourceChecker resourceChecker, ResourceTypeChecker resourceTypeChecker) {
        super(resourceChecker);
        this.resourceTypeChecker = resourceTypeChecker;
    }

    // TODO: delete check if resource has metric values

    @Override
    public Single<JsonObject> postOne(RoutingContext rc) {
        JsonObject requestBody = rc.body().asJsonObject();
        return resourceTypeChecker.checkExistsOne(requestBody
                .getJsonObject("resource_type")
                .getLong("type_id"))
            .andThen(Single.defer(() -> Single.just(1L)))
            .flatMap(result -> entityChecker.submitCreate(requestBody));
    }

    // TODO: remove
    @Override
    public Completable updateOne(RoutingContext rc) {
        JsonObject requestBody = rc.body().asJsonObject();
        return HttpHelper.getLongPathParam(rc, "id")
            .flatMap(entityChecker::checkFindOne)
            .flatMap(result -> checkUpdateResourceTypeExists(requestBody, result))
            .flatMapCompletable(result -> entityChecker.submitUpdate(requestBody, result));
    }

    protected Single<JsonObject> checkUpdateResourceTypeExists(JsonObject requestBody, JsonObject entity) {
        if (requestBody.containsKey("resource_type")) {
            return resourceTypeChecker.checkExistsOne(requestBody.getJsonObject("resource_type").getLong("type_id"))
                .andThen(Single.just(entity));
        }
        return Single.just(entity);
    }
}
