package at.uibk.dps.rm.handler.function;

import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.handler.ErrorHandler;
import at.uibk.dps.rm.service.rxjava3.database.function.RuntimeService;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;

public class RuntimeChecker extends EntityChecker {
    private final RuntimeService runtimeService;

    public RuntimeChecker(RuntimeService runtimeService) {
        super(runtimeService);
        this.runtimeService = runtimeService;
    }

    @Override
    public Completable checkForDuplicateEntity(JsonObject entity) {
        Single<Boolean> existsOneByName = runtimeService
            .existsOneByName(entity.getString("name"));
        return ErrorHandler.handleDuplicates(existsOneByName).ignoreElement();
    }
}
