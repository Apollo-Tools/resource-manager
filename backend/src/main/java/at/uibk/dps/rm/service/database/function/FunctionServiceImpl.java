package at.uibk.dps.rm.service.database.function;

import at.uibk.dps.rm.entity.dto.function.UpdateFunctionDTO;
import at.uibk.dps.rm.entity.dto.resource.RuntimeEnum;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.entity.model.Runtime;
import at.uibk.dps.rm.exception.BadInputException;
import at.uibk.dps.rm.repository.function.FunctionRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import at.uibk.dps.rm.util.misc.UploadFileHelper;
import at.uibk.dps.rm.util.validation.ServiceResultValidator;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.core.Vertx;
import org.hibernate.reactive.stage.Stage.SessionFactory;
import org.hibernate.reactive.util.impl.CompletionStages;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;

/**
 * This is the implementation of the #FunctionService.
 *
 * @author matthi-g
 */
public class FunctionServiceImpl extends DatabaseServiceProxy<Function> implements FunctionService {
    private final FunctionRepository repository;

    private final Vertx vertx = Vertx.currentContext().owner();

    /**
     * Create an instance from the repository.
     *
     * @param repository the function repository
     */
    public FunctionServiceImpl(FunctionRepository repository, SessionFactory sessionFactory) {
        super(repository, Function.class, sessionFactory);
        this.repository = repository;
    }

    @Override
    public Future<JsonObject> findOne(long id) {
        CompletionStage<Function> findOne = withSession(session -> repository.findByIdAndFetch(session, id));
        return Future.fromCompletionStage(findOne)
            .map(JsonObject::mapFrom);
    }

    @Override
    public Future<JsonObject> findOneByIdAndAccountId(long id, long accountId) {
        CompletionStage<Function> findOne = withSession(session -> repository
            .findByIdAndAccountId(session, id, accountId, true));
        return Future.fromCompletionStage(findOne)
            .map(JsonObject::mapFrom);
    }

    @Override
    public Future<JsonArray> findAll() {
        CompletionStage<List<Function>> findAll = withSession(repository::findAllAndFetch);
        return Future.fromCompletionStage(findAll)
            .map(FunctionServiceImpl::mapFunctionsToJsonArray);
    }

    @Override
    public Future<JsonArray> findAllAccessibleFunctions(long accountId) {
        CompletionStage<List<Function>> findAll = withSession((session) -> repository
                .findAllAccessibleAndFetch(session, accountId));
        return sessionToFuture(findAll)
            .map(FunctionServiceImpl::mapFunctionsToJsonArray);
    }

    @Override
    public Future<JsonArray> findAllByAccountId(long accountId) {
        CompletionStage<List<Function>> findAll = withSession(session -> repository
            .findAllByAccountId(session, accountId));
        return sessionToFuture(findAll)
            .map(FunctionServiceImpl::mapFunctionsToJsonArray);
    }

    private static JsonArray mapFunctionsToJsonArray(List<Function> result) {
        ArrayList<JsonObject> objects = new ArrayList<>();
        for (Function entity: result) {
            objects.add(JsonObject.mapFrom(entity));
        }
        return new JsonArray(objects);
    }

    @Override
    public Future<JsonObject> saveToAccount(long accountId, JsonObject data) {
        Function function = data.mapTo(Function.class);
        CompletionStage<Function> create = withTransaction(session ->
            session.find(Runtime.class, function.getRuntime().getRuntimeId())
                .thenCompose(runtime -> {
                    ServiceResultValidator.checkFound(runtime, Runtime.class);
                    RuntimeEnum selectedRuntime = RuntimeEnum.fromRuntime(runtime);
                    if (!function.getIsFile() && !selectedRuntime.equals(RuntimeEnum.PYTHON38)) {
                        throw new BadInputException("runtime only supports zip archives");
                    }
                    return repository.findOneByNameTypeRuntimeAndCreator(session, function.getName(),
                        function.getFunctionType().getArtifactTypeId(), runtime.getRuntimeId(), accountId);
                })
                .thenCompose(existingFunction -> {
                    ServiceResultValidator.checkExists(existingFunction, Function.class);
                    return session.find(FunctionType.class, function.getFunctionType().getArtifactTypeId());
                })
                .thenCompose(functionType -> {
                    ServiceResultValidator.checkFound(functionType, FunctionType.class);
                    function.setFunctionType(functionType);
                    return session.find(Account.class, accountId);
                })
                .thenCompose(account -> {
                    ServiceResultValidator.checkFound(account, Account.class);
                    function.setCreatedBy(account);
                    return session.persist(function);
                })
                .thenCompose(res -> {
                    if (function.getIsFile()) {
                        Vertx vertx = Vertx.currentContext().owner();
                        return UploadFileHelper.persistUploadedFile(vertx, function.getCode())
                            .toCompletionStage(function);
                    }
                    return CompletionStages.completedFuture(function);
                })
        );
        return sessionToFuture(create)
            .map(JsonObject::mapFrom);
    }

    @Override
    public Future<Void> updateOwned(long id, long accountId, JsonObject fields) {
        UpdateFunctionDTO updateFunction = fields.mapTo(UpdateFunctionDTO.class);
        CompletionStage<Void> update = withTransaction(session -> repository
            .findByIdAndAccountId(session, id, accountId, false)
            .thenCompose(function -> {
                ServiceResultValidator.checkFound(function, Function.class);
                if (updateFunction.getCode() == null) {
                    return CompletionStages.completedFuture(function);
                }
                boolean updateIsFile = updateFunction.getIsFile();
                String message = "";
                if (function.getIsFile() != updateIsFile && updateIsFile) {
                    message = "Function can't be updated with zip packaged code";
                } else if (function.getIsFile() != updateIsFile) {
                    message = "Function can only be updated with zip packaged code";
                }
                if (!message.isBlank()) {
                    throw new BadInputException(message);
                }
                function.setCode(updateNonNullValue(function.getCode(), updateFunction.getCode()));
                if (function.getIsFile()) {
                    return UploadFileHelper.updateFile(vertx, function.getCode(), updateFunction.getCode())
                        .toCompletionStage(function);
                } else {
                    return CompletionStages.completedFuture(function);
                }
            })
            .thenAccept(function -> {
                function.setTimeoutSeconds(updateNonNullValue(function.getTimeoutSeconds(),
                        updateFunction.getTimeoutSeconds()));
                function.setMemoryMegabytes(updateNonNullValue(function.getMemoryMegabytes(),
                        updateFunction.getMemoryMegabytes()));
                function.setIsPublic(updateNonNullValue(function.getIsPublic(), updateFunction.getIsPublic()));
            })
        );
        return sessionToFuture(update);
    }

    @Override
    public Future<Void> deleteFromAccount(long accountId, long id) {
        CompletionStage<Void> delete = withTransaction(session -> repository
            .findByIdAndAccountId(session, id, accountId, false)
            .thenCompose(function -> {
                ServiceResultValidator.checkFound(function, Function.class);
                CompletionStage<Void> deleteFunction = session.remove(function);
                if (function.getIsFile()) {
                    Vertx vertx = Vertx.currentContext().owner();
                    return UploadFileHelper.deleteFile(vertx, function.getCode())
                        .toCompletionStage(null)
                        .thenCompose(res -> deleteFunction);
                }
                return deleteFunction;
            })
        );
        return sessionToFuture(delete);
    }
}
