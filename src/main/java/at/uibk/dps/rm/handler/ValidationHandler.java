package at.uibk.dps.rm.handler;

import at.uibk.dps.rm.util.HttpHelper;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RoutingContext;

public abstract class ValidationHandler {

    protected final EntityChecker entityChecker;

    protected ValidationHandler(EntityChecker entityChecker) {
        this.entityChecker = entityChecker;
    }

    public Completable deleteOne(RoutingContext rc) {
        return HttpHelper.getLongPathParam(rc, "id")
            .flatMap(id -> entityChecker.checkFindOne(id)
                .flatMapCompletable(this::checkDeleteEntityIsUsed)
                .andThen(Single.just(id)))
            .flatMapCompletable(entityChecker::submitDelete);
    }

    protected Single<JsonObject> getOne(RoutingContext rc) {
        return HttpHelper.getLongPathParam(rc, "id")
            .flatMap(entityChecker::checkFindOne);
    }

    protected Single<JsonArray> getAll(RoutingContext rc) {
        return entityChecker.checkFindAll();
    }

    protected Single<JsonObject> postOne(RoutingContext rc) {
        JsonObject requestBody = rc.body().asJsonObject();
        return entityChecker.checkForDuplicateEntity(requestBody)
            // see https://stackoverflow.com/a/50670502/13164629 for further information
            .andThen(Single.defer(() -> Single.just(1L)))
            .flatMap(result -> entityChecker.submitCreate(requestBody));
    }

    protected  Completable postAll(RoutingContext rc) {
        JsonArray requestBody = rc.body().asJsonArray();
        return entityChecker.submitCreateAll(requestBody);
    }

    protected Completable updateOne(RoutingContext rc) {
        JsonObject requestBody = rc.body().asJsonObject();
        return HttpHelper.getLongPathParam(rc, "id")
            .flatMap(entityChecker::checkFindOne)
            .flatMap(result -> entityChecker.checkUpdateNoDuplicate(requestBody, result))
            .flatMapCompletable(result -> entityChecker.submitUpdate(requestBody, result));
    }

    protected Completable checkDeleteEntityIsUsed(JsonObject entity) {
        return Single.just(entity).ignoreElement();
    }
}
