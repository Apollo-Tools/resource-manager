package at.uibk.dps.rm.handler.function;

import at.uibk.dps.rm.entity.dto.deployment.FunctionResourceIds;
import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.handler.ErrorHandler;
import at.uibk.dps.rm.service.rxjava3.database.function.FunctionService;
import at.uibk.dps.rm.util.configuration.ConfigUtility;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.core.Vertx;

import java.nio.file.Path;
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
    public Single<JsonObject> submitCreate(JsonObject entity) {
        if (entity.getBoolean("is_file")) {
            Vertx vertx = Vertx.currentContext().owner();
            String fileName = entity.getString("code");
            return new ConfigUtility(vertx).getConfig()
                .flatMapCompletable(config -> {
                    Path tempPath = Path.of(config.getString("upload_temp_directory"), fileName);
                    Path destPath = Path.of(config.getString("upload_persist_directory"), fileName);
                    return vertx.fileSystem().copy(tempPath.toString(), destPath.toString());
                })
                .andThen(super.submitCreate(entity));
        }
        return super.submitCreate(entity);
    }

    @Override
    public Completable submitUpdate(JsonObject updateEntity, JsonObject entity) {
        if (entity.getBoolean("is_file")) {
            Vertx vertx = Vertx.currentContext().owner();
            String newFileName = updateEntity.getString("code");
            String oldFileName = entity.getString("code");
            return new ConfigUtility(vertx).getConfig()
                .flatMapCompletable(config -> {
                    Path oldPath = Path.of(config.getString("upload_persist_directory"), oldFileName);
                    Path tempPath = Path.of(config.getString("upload_temp_directory"), newFileName);
                    Path destPath = Path.of(config.getString("upload_persist_directory"), newFileName);
                    return vertx.fileSystem().delete(oldPath.toString())
                        .andThen(vertx.fileSystem().copy(tempPath.toString(), destPath.toString()));
                })
                .andThen(super.submitUpdate(updateEntity, entity));
        }
        return super.submitUpdate(updateEntity, entity);
    }

    @Override
    public Completable checkForDuplicateEntity(JsonObject entity) {
        final Single<Boolean> existsOneByNameAndRuntimeId = functionService
            .existsOneByNameAndRuntimeId(entity.getString("name"),
                entity.getJsonObject("runtime").getLong("runtime_id"));
        return ErrorHandler.handleDuplicates(existsOneByNameAndRuntimeId).ignoreElement();
    }

    /**
     * Check if all functions from the given list exist by their function id.
     *
     * @param functionResourceIds the list of function resources
     * @return a Completable if all functions exist, else an NotFoundException
     * gets thrown
     */
    public Completable checkExistAllByIds(List<FunctionResourceIds> functionResourceIds) {
        Single<Boolean> existsAllByFunctionIds = Observable.fromIterable(functionResourceIds)
            .map(FunctionResourceIds::getFunctionId)
            .toList()
            .map(Set::copyOf)
            .flatMap(functionService::existsAllByIds);
        return ErrorHandler.handleExistsOne(existsAllByFunctionIds).ignoreElement();
    }
}
