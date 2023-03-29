package at.uibk.dps.rm.handler.function;

import at.uibk.dps.rm.handler.ValidationHandler;
import at.uibk.dps.rm.util.HttpHelper;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RoutingContext;

public class FunctionHandler extends ValidationHandler {

    private final FunctionChecker functionChecker;

    private final RuntimeChecker runtimeChecker;

    public FunctionHandler(FunctionChecker functionChecker, RuntimeChecker runtimeChecker) {
        super(functionChecker);
        this.functionChecker = functionChecker;
        this.runtimeChecker = runtimeChecker;
    }

    @Override
    public Single<JsonObject> postOne(RoutingContext rc) {
        JsonObject requestBody = rc.body().asJsonObject();
        return runtimeChecker.checkExistsOne(requestBody
                .getJsonObject("runtime")
                .getLong("runtime_id"))
            .andThen(entityChecker.checkForDuplicateEntity(requestBody))
            .andThen(Single.defer(() -> Single.just(1L)))
            .flatMap(result -> entityChecker.submitCreate(requestBody));
    }

    @Override
    public Completable updateOne(RoutingContext rc) {
        JsonObject requestBody = rc.body().asJsonObject();
        return HttpHelper.getLongPathParam(rc, "id")
            .flatMap(entityChecker::checkFindOne)
            .flatMap(result -> checkUpdateRuntimeExists(requestBody, result))
            .flatMap(result -> functionChecker.checkUpdateNoDuplicate(requestBody, result))
            .flatMapCompletable(result -> entityChecker.submitUpdate(requestBody, result));
    }

    protected Single<JsonObject> checkUpdateRuntimeExists(JsonObject requestBody, JsonObject entity) {
        if (requestBody.containsKey("runtime")) {
            return runtimeChecker.checkExistsOne(requestBody.getJsonObject("runtime").getLong("runtime_id"))
                .andThen(Single.just(entity));
        }
        return Single.just(entity);
    }
}
