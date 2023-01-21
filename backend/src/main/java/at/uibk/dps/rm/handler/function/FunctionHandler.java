package at.uibk.dps.rm.handler.function;

import at.uibk.dps.rm.handler.ValidationHandler;
import at.uibk.dps.rm.service.rxjava3.database.function.FunctionService;
import at.uibk.dps.rm.service.rxjava3.database.function.RuntimeService;
import at.uibk.dps.rm.util.HttpHelper;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RoutingContext;

public class FunctionHandler extends ValidationHandler {

    private final RuntimeChecker runtimeChecker;

    public FunctionHandler(FunctionService functionService, RuntimeService runtimeService) {
        super(new FunctionChecker(functionService));
        runtimeChecker = new RuntimeChecker(runtimeService);
    }

    @Override
    public Single<JsonObject> postOne(RoutingContext rc) {
        JsonObject requestBody = rc.body().asJsonObject();
        return runtimeChecker.checkExistsOne(requestBody
                .getJsonObject("runtime")
                .getLong("runtime_id"))
            .andThen(Single.defer(() -> Single.just(1L)))
            .flatMap(result -> entityChecker.submitCreate(requestBody));
    }

    @Override
    public Completable updateOne(RoutingContext rc) {
        JsonObject requestBody = rc.body().asJsonObject();
        return HttpHelper.getLongPathParam(rc, "id")
            .flatMap(entityChecker::checkFindOne)
            .flatMap(result -> checkUpdateRuntimeExists(requestBody, result))
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
