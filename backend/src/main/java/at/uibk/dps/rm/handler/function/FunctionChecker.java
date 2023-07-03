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

    // TODO: delete file if error occurs (restructure)
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
                .andThen(Single.defer(() -> Single.just(1L)))
                .flatMap(res -> super.submitCreate(entity));
        }
        return super.submitCreate(entity);
    }

    @Override
    public Completable submitUpdate(long id, JsonObject fields) {
        return checkFindOne(id).flatMapCompletable(function ->
            super.submitUpdate(id, fields)
                .andThen(Single.defer(() -> Single.just(id)))
                .flatMapCompletable(result -> {
                    if (fields.getBoolean("is_file")) {
                        return updateNewCodeFile(function.getString("code"), fields.getString("code"));
                    }
                    return Completable.complete();
                })
        );
    }

    // TODO: fix
    public Completable submitDelete(long id, JsonObject entity) {
        if (entity.getBoolean("is_file")) {
            Vertx vertx = Vertx.currentContext().owner();
            String fileName = entity.getString("code");
            return new ConfigUtility(vertx).getConfig()
                .flatMapCompletable(config -> {
                    Path filePath = Path.of(config.getString("upload_persist_directory"), fileName);
                    return vertx.fileSystem().delete(filePath.toString());
                })
                .andThen(super.submitDelete(id));
        }
        return super.submitDelete(id);
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

    private Completable updateNewCodeFile(String oldFileName, String newFileName) {
        Vertx vertx = Vertx.currentContext().owner();
        return new ConfigUtility(vertx).getConfig()
            .flatMapCompletable(config -> {
                Path oldPath = Path.of(config.getString("upload_persist_directory"), oldFileName);
                Path tempPath = Path.of(config.getString("upload_temp_directory"), newFileName);
                Path destPath = Path.of(config.getString("upload_persist_directory"), newFileName);
                return vertx.fileSystem().delete(oldPath.toString())
                    .andThen(vertx.fileSystem().copy(tempPath.toString(), destPath.toString()));
            });
    }
}
