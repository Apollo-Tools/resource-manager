package at.uibk.dps.rm.service.database.function;

import at.uibk.dps.rm.entity.dto.function.UpdateFunctionDTO;
import at.uibk.dps.rm.entity.dto.resource.RuntimeEnum;
import at.uibk.dps.rm.entity.model.Account;
import at.uibk.dps.rm.entity.model.Function;
import at.uibk.dps.rm.entity.model.FunctionType;
import at.uibk.dps.rm.entity.model.Runtime;
import at.uibk.dps.rm.exception.AlreadyExistsException;
import at.uibk.dps.rm.exception.BadInputException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.repository.function.FunctionRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import at.uibk.dps.rm.service.util.RxVertxHandler;
import at.uibk.dps.rm.util.misc.UploadFileHelper;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.core.Vertx;
import org.hibernate.reactive.stage.Stage.SessionFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * This is the implementation of the {@link FunctionService}.
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
    public void findOne(long id, Handler<AsyncResult<JsonObject>> resultHandler) {
        Maybe<Function> findOne = withTransactionMaybe(sessionManager -> repository
            .findByIdAndFetch(sessionManager, id)
            .switchIfEmpty(Maybe.error(new NotFoundException(Function.class))));
        RxVertxHandler.handleSession(findOne.map(JsonObject::mapFrom), resultHandler);
    }

    @Override
    public void findOneByIdAndAccountId(long id, long accountId, Handler<AsyncResult<JsonObject>> resultHandler) {
        Maybe<Function> findOne = withTransactionMaybe(sessionManager -> repository
            .findByIdAndAccountId(sessionManager, id, accountId, true)
            .switchIfEmpty(Maybe.error(new NotFoundException(Function.class))));
        RxVertxHandler.handleSession(findOne.map(JsonObject::mapFrom), resultHandler);
    }

    @Override
    public void findAll(Handler<AsyncResult<JsonArray>> resultHandler) {
        Single<List<Function>> findAll = withTransactionSingle(repository::findAllAndFetch);
        RxVertxHandler.handleSession(findAll.map(FunctionServiceImpl::mapFunctionsToJsonArray), resultHandler);
    }

    @Override
    public void findAllAccessibleFunctions(long accountId, Handler<AsyncResult<JsonArray>> resultHandler) {
        Single<List<Function>> findAll = withTransactionSingle(sessionManager -> repository
                .findAllAccessibleAndFetch(sessionManager, accountId));
        RxVertxHandler.handleSession(findAll.map(FunctionServiceImpl::mapFunctionsToJsonArray), resultHandler);
    }

    @Override
    public void findAllByAccountId(long accountId, Handler<AsyncResult<JsonArray>> resultHandler) {
        Single<List<Function>> findAll = withTransactionSingle(sessionManager -> repository
            .findAllByAccountId(sessionManager, accountId));
        RxVertxHandler.handleSession(findAll.map(FunctionServiceImpl::mapFunctionsToJsonArray), resultHandler);
    }

    private static JsonArray mapFunctionsToJsonArray(List<Function> result) {
        ArrayList<JsonObject> objects = new ArrayList<>();
        for (Function entity: result) {
            objects.add(JsonObject.mapFrom(entity));
        }
        return new JsonArray(objects);
    }

    @Override
    public void saveToAccount(long accountId, JsonObject data, Handler<AsyncResult<JsonObject>> resultHandler) {
        Function function = data.mapTo(Function.class);
        Maybe<Function> create = withTransactionMaybe(sessionManager -> sessionManager
            .find(Runtime.class, function.getRuntime().getRuntimeId())
            .switchIfEmpty(Maybe.error(new NotFoundException(Runtime.class)))
            .flatMap(runtime -> {
                RuntimeEnum selectedRuntime = RuntimeEnum.fromRuntime(runtime);
                if (!function.getIsFile() && !selectedRuntime.equals(RuntimeEnum.PYTHON38)) {
                    return Maybe.error(new BadInputException("runtime only supports zip archives"));
                }
                return repository.findOneByNameTypeRuntimeAndCreator(sessionManager, function.getName(),
                    function.getFunctionType().getArtifactTypeId(), runtime.getRuntimeId(), accountId);
            })
            .flatMap(existingFunction -> Maybe.<FunctionType>error(new AlreadyExistsException(Function.class)))
            .switchIfEmpty(sessionManager.find(FunctionType.class, function.getFunctionType().getArtifactTypeId()))
            .switchIfEmpty(Maybe.error(new NotFoundException(FunctionType.class)))
            .flatMap(functionType -> {
                function.setFunctionType(functionType);
                return sessionManager.find(Account.class, accountId);
            })
            .switchIfEmpty(Maybe.error(new NotFoundException(Account.class)))
            .flatMapSingle(account -> {
                function.setCreatedBy(account);
                return sessionManager.persist(function);
            })
            .flatMap(res -> {
                if (function.getIsFile()) {
                    Vertx vertx = Vertx.currentContext().owner();
                    return UploadFileHelper.persistUploadedFile(vertx, function.getCode())
                        .andThen(Maybe.defer(() -> Maybe.just(function)));
                }
                return Maybe.just(function);
            })
        );
        RxVertxHandler.handleSession(create.map(JsonObject::mapFrom), resultHandler);
    }

    @Override
    public void updateOwned(long id, long accountId, JsonObject fields,
            Handler<AsyncResult<Void>> resultHandler) {
        UpdateFunctionDTO updateFunction = fields.mapTo(UpdateFunctionDTO.class);
        Completable update = withTransactionCompletable(sessionManager -> repository
            .findByIdAndAccountId(sessionManager, id, accountId, false)
            .switchIfEmpty(Maybe.error(new NotFoundException(Function.class)))
            .flatMap(function -> {
                if (updateFunction.getCode() == null) {
                    return Maybe.just(function);
                }
                boolean updateIsFile = updateFunction.getIsFile();
                String message = "";
                if (function.getIsFile() != updateIsFile && updateIsFile) {
                    message = "Function can't be updated with zip packaged code";
                } else if (function.getIsFile() != updateIsFile) {
                    message = "Function can only be updated with zip packaged code";
                }
                if (!message.isBlank()) {
                    return Maybe.error(new BadInputException(message));
                }
                if (function.getIsFile()) {
                    return UploadFileHelper.updateFile(vertx, function.getCode(), updateFunction.getCode())
                        .andThen(Maybe.defer(() -> {
                            function.setCode(updateNonNullValue(function.getCode(), updateFunction.getCode()));
                            return Maybe.just(function);
                        }));
                } else {
                    function.setCode(updateNonNullValue(function.getCode(), updateFunction.getCode()));
                    return Maybe.just(function);
                }
            })
            .flatMapCompletable(function -> {
                function.setTimeoutSeconds(updateNonNullValue(function.getTimeoutSeconds(),
                        updateFunction.getTimeoutSeconds()));
                function.setMemoryMegabytes(updateNonNullValue(function.getMemoryMegabytes(),
                        updateFunction.getMemoryMegabytes()));
                function.setIsPublic(updateNonNullValue(function.getIsPublic(), updateFunction.getIsPublic()));
                return Completable.complete();
            })
        );
        RxVertxHandler.handleSession(update, resultHandler);
    }

    @Override
    public void deleteFromAccount(long accountId, long id, Handler<AsyncResult<Void>> resultHandler) {
        Completable delete = withTransactionCompletable(sessionManager -> repository
            .findByIdAndAccountId(sessionManager, id, accountId, false)
            .switchIfEmpty(Maybe.error(new NotFoundException(Function.class)))
            .flatMapCompletable(function -> {
                Completable deleteFunction = sessionManager.remove(function);
                if (function.getIsFile()) {
                    Vertx vertx = Vertx.currentContext().owner();
                    return UploadFileHelper.deleteFile(vertx, function.getCode())
                        .andThen(Completable.defer(() -> deleteFunction));
                }
                return deleteFunction;
            })
        );
        RxVertxHandler.handleSession(delete, resultHandler);
    }
}
