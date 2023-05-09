package at.uibk.dps.rm.handler.function;

import at.uibk.dps.rm.entity.dto.reservation.FunctionResourceIds;
import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.handler.ErrorHandler;
import at.uibk.dps.rm.service.rxjava3.database.function.FunctionService;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.Set;

/**
 * Implements methods to perform CRUD operations on the function entity.
 *
 * @see EntityChecker
 *
 * @author matthi-g
 */
public class FunctionChecker extends EntityChecker {
    private final FunctionService functionService;

    /**
     * Create an instance from the functionService.
     *
     * @param functionService the function service
     */
    public FunctionChecker(FunctionService functionService) {
        super(functionService);
        this.functionService = functionService;
    }

    @Override
    public Completable checkForDuplicateEntity(JsonObject entity) {
        final Single<Boolean> existsOneByNameAndRuntimeId = functionService
            .existsOneByNameAndRuntimeId(entity.getString("name"),
                entity.getJsonObject("runtime").getLong("runtime_id"));
        return ErrorHandler.handleDuplicates(existsOneByNameAndRuntimeId).ignoreElement();
    }

    @Override
    public Single<JsonObject> checkUpdateNoDuplicate(JsonObject updateEntity, JsonObject entity) {
        if (updateEntity.containsKey("name") && updateEntity.containsKey("runtime")) {
            Single<Boolean> existsOneByNameAndRuntimeId = functionService
                .existsOneByNameAndRuntimeIdExcludeEntity(entity.getLong("function_id"),
                    updateEntity.getString("name"),
                    updateEntity.getJsonObject("runtime").getLong("runtime_id"));
            return ErrorHandler.handleDuplicates(existsOneByNameAndRuntimeId)
                .flatMap(result -> Single.just(entity));
        }
        return Single.just(entity);
    }

    public Completable checkExistAllByIds(List<FunctionResourceIds> functionResourceIds) {
        Single<Boolean> existsAllByFunctionIds = Observable.fromIterable(functionResourceIds)
            .map(FunctionResourceIds::getFunctionId)
            .toList()
            .map(Set::copyOf)
            .flatMap(functionService::existsAllByIds);
        return ErrorHandler.handleExistsOne(existsAllByFunctionIds).ignoreElement();
    }
}
