package at.uibk.dps.rm.handler.function;

import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.handler.ErrorHandler;
import at.uibk.dps.rm.service.rxjava3.database.function.FunctionService;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;

public class FunctionChecker extends EntityChecker {
    private final FunctionService functionService;

    public FunctionChecker(FunctionService functionService) {
        super(functionService);
        this.functionService = functionService;
    }

    @Override
    public Completable checkForDuplicateEntity(JsonObject entity) {
        Single<Boolean> existsOneByNameAndRuntimeId = functionService
            .existsOneByNameAndRuntimeId(entity.getString("name"),
                entity.getJsonObject("runtime").getLong("runtime_id"));
        return ErrorHandler.handleDuplicates(existsOneByNameAndRuntimeId).ignoreElement();
    }

    @Override
    public Single<JsonObject> checkUpdateNoDuplicate(JsonObject requestBody, JsonObject entity) {
        if (requestBody.containsKey("name") && requestBody.containsKey("runtime")) {
            Single<Boolean> existsOneByNameAndRuntimeId = functionService
                .existsOneByNameAndRuntimeIdExcludeEntity(entity.getLong("function_id"),
                    requestBody.getString("name"),
                    requestBody.getJsonObject("runtime").getLong("runtime_id"));
            return ErrorHandler.handleDuplicates(existsOneByNameAndRuntimeId)
                .flatMap(result -> Single.just(entity));
        }
        return Single.just(entity);
    }
}
