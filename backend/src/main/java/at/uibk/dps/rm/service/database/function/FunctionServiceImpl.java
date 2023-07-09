package at.uibk.dps.rm.service.database.function;

import at.uibk.dps.rm.entity.dto.resource.RuntimeEnum;
import at.uibk.dps.rm.entity.model.Function;
import at.uibk.dps.rm.entity.model.Runtime;
import at.uibk.dps.rm.exception.BadInputException;
import at.uibk.dps.rm.repository.function.FunctionRepository;
import at.uibk.dps.rm.repository.function.RuntimeRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import at.uibk.dps.rm.util.configuration.ConfigUtility;
import at.uibk.dps.rm.util.validation.ServiceResultValidator;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.core.Vertx;
import org.hibernate.reactive.stage.Stage;
import org.hibernate.reactive.util.impl.CompletionStages;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletionStage;

/**
 * This is the implementation of the #FunctionService.
 *
 * @author matthi-g
 */
public class FunctionServiceImpl extends DatabaseServiceProxy<Function> implements FunctionService {
    private final FunctionRepository repository;

    private final RuntimeRepository runtimeRepository;

    /**
     * Create an instance from the repository.
     *
     * @param repository the function repository
     */
    public FunctionServiceImpl(FunctionRepository repository, RuntimeRepository runtimeRepository,
            Stage.SessionFactory sessionFactory) {
        super(repository, Function.class, sessionFactory);
        this.repository = repository;
        this.runtimeRepository = runtimeRepository;
    }

    @Override
    public Future<JsonObject> findOne(long id) {
        CompletionStage<Function> findOne = withSession(session -> repository.findByIdAndFetch(session, id));
        return Future.fromCompletionStage(findOne)
            .map(JsonObject::mapFrom);
    }

    @Override
    public Future<JsonArray> findAll() {
        CompletionStage<List<Function>> findAll = withSession(repository::findAllAndFetch);
        return Future.fromCompletionStage(findAll)
            .map(result -> {
                ArrayList<JsonObject> objects = new ArrayList<>();
                for (Function entity: result) {
                    objects.add(JsonObject.mapFrom(entity));
                }
                return new JsonArray(objects);
            });
    }

    @Override
    public Future<JsonObject> save(JsonObject data) {
        Function function = data.mapTo(Function.class);
        CompletionStage<Function> create = withTransaction(session ->
            runtimeRepository.findById(session, function.getRuntime().getRuntimeId())
                .thenCompose(runtime -> {
                    ServiceResultValidator.checkFound(runtime, Runtime.class);
                    RuntimeEnum selectedRuntime = RuntimeEnum.fromRuntime(runtime);
                    if (!function.getIsFile() && !selectedRuntime.equals(RuntimeEnum.PYTHON38)) {
                        throw new BadInputException("runtime only supports zip archives");
                    }
                    return repository.findOneByNameAndRuntimeId(session, function.getName(), runtime.getRuntimeId());
                })
                .thenCompose(existingFunction -> {
                    ServiceResultValidator.checkExists(existingFunction, Function.class);
                    return session.persist(function);
                })
                .thenCompose(res -> {
                    if (function.getIsFile()) {
                        Vertx vertx = Vertx.currentContext().owner();
                        String fileName = function.getCode();
                        return new ConfigUtility(vertx).getConfig()
                            .flatMapCompletable(config -> {
                                Path tempPath = Path.of(config.getString("upload_temp_directory"), fileName);
                                Path destPath = Path.of(config.getString("upload_persist_directory"), fileName);
                                return vertx.fileSystem().copy(tempPath.toString(), destPath.toString());
                            })
                            .toCompletionStage(function);
                    }
                    return CompletionStages.completedFuture(function);
                })
        );
        return transactionToFuture(create)
            .map(JsonObject::mapFrom);
    }

    @Override
    public Future<Void> update(long id, JsonObject fields) {
        CompletionStage<Function> update = withTransaction(session -> repository.findByIdAndFetch(session, id)
            .thenApply(function -> {
                ServiceResultValidator.checkFound(function, Function.class);
                boolean updateIsFile = fields.getBoolean("is_file");
                String message = "";
                if (function.getIsFile() != updateIsFile && updateIsFile) {
                    message = "Function can't be updated with zip packaged code";
                } else if (function.getIsFile() != updateIsFile) {
                    message = "Function can only be updated with zip packaged code";
                }
                if (!message.isBlank()) {
                    throw new BadInputException(message);
                }
                function.setCode(fields.getString("code"));
                return function;
            })
        );
        return transactionToFuture(update).mapEmpty();
    }

    @Override
    public Future<Void> delete(long id) {
        CompletionStage<Void> delete = withTransaction(session -> repository.findById(session, id)
            .thenCompose(function -> {
                ServiceResultValidator.checkFound(function, Function.class);
                CompletionStage<Void> deleteFunction = session.remove(function);
                if (function.getIsFile()) {
                    Vertx vertx = Vertx.currentContext().owner();
                    return new ConfigUtility(vertx).getConfig().flatMapCompletable(config -> {
                        Path filePath = Path.of(config.getString("upload_persist_directory"),
                            function.getCode());
                        return vertx.fileSystem().delete(filePath.toString());
                    })
                    .toCompletionStage(null)
                    .thenCompose(res -> deleteFunction);
                }
                return deleteFunction;
            })
        );
        return transactionToFuture(delete);
    }

    @Override
    public Future<Boolean> existsAllByIds(Set<Long> functionIds) {
        CompletionStage<List<Function>> findAll = withSession(session -> repository.findAllByIds(session, functionIds));
        return Future.fromCompletionStage(findAll)
            .map(result -> result.size() == functionIds.size());
    }
}
