package at.uibk.dps.rm.service.database.function;

import at.uibk.dps.rm.entity.model.Function;
import at.uibk.dps.rm.exception.BadInputException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.repository.function.FunctionRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.hibernate.reactive.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletionStage;

/**
 * This is the implementation of the #FunctionService.
 *
 * @author matthi-g
 */
public class FunctionServiceImpl extends DatabaseServiceProxy<Function> implements FunctionService {
    private final FunctionRepository repository;

    /**
     * Create an instance from the repository.
     *
     * @param repository the function repository
     */
    public FunctionServiceImpl(FunctionRepository repository, Stage.SessionFactory sessionFactory) {
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
    public Future<Void> update(long id, JsonObject data) {
        CompletionStage<Function> update = withTransaction(session -> repository.findByIdAndFetch(session, id)
            .thenApply(function -> {
                if (function == null) {
                    throw new NotFoundException(Function.class);
                }
                boolean updateIsFile = data.getBoolean("is_file");
                String message = "";
                if (function.getIsFile() != updateIsFile && updateIsFile) {
                    message = "Function can't be updated with zip packaged code";
                } else if (function.getIsFile() != updateIsFile) {
                    message = "Function can only be updated with zip packaged code";
                }
                if (!message.isBlank()) {
                    throw new BadInputException(message);
                }
                function.setCode(data.getString("code"));
                return function;
            })
        );
        return Future
            .fromCompletionStage(update)
            .recover(this::recoverFailure)
            .mapEmpty();
    }

    @Override
    public Future<Boolean> existsOneByNameAndRuntimeId(String name, long runtimeId) {
        CompletionStage<Function> findOne = withSession(session ->
            repository.findOneByNameAndRuntimeId(session, name, runtimeId));
        return Future.fromCompletionStage(findOne)
            .map(Objects::nonNull);
    }

    @Override
    public Future<Boolean> existsAllByIds(Set<Long> functionIds) {
        CompletionStage<List<Function>> findAll = withSession(session -> repository.findAllByIds(session, functionIds));
        return Future.fromCompletionStage(findAll)
            .map(result -> result.size() == functionIds.size());
    }
}
