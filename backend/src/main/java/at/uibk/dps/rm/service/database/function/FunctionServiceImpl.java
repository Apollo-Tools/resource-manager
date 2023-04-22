package at.uibk.dps.rm.service.database.function;

import at.uibk.dps.rm.entity.model.Function;
import at.uibk.dps.rm.repository.function.FunctionRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.Objects;

public class FunctionServiceImpl extends DatabaseServiceProxy<Function> implements FunctionService {
    private final FunctionRepository functionRepository;

    public FunctionServiceImpl(FunctionRepository repository) {
        super(repository, Function.class);
        functionRepository = repository;
    }

    @Override
    public Future<JsonObject> findOne(long id) {
        return Future
            .fromCompletionStage(functionRepository.findByIdAndFetch(id))
            .map(JsonObject::mapFrom);
    }

    @Override
    public Future<JsonArray> findAll() {
        return Future
            .fromCompletionStage(functionRepository.findAllAndFetch())
            .map(result -> {
                ArrayList<JsonObject> objects = new ArrayList<>();
                for (Function entity: result) {
                    objects.add(JsonObject.mapFrom(entity));
                }
                return new JsonArray(objects);
            });
    }

    @Override
    public Future<Boolean> existsOneByNameAndRuntimeIdExcludeEntity(long excludeId, String name, long runtimeId) {
        return Future
            .fromCompletionStage(functionRepository.findOneByNameAndRuntimeId(excludeId, name, runtimeId))
            .map(Objects::nonNull);
    }

    @Override
    public Future<Boolean> existsOneByNameAndRuntimeId(String name, long runtimeId) {
        return Future
            .fromCompletionStage(functionRepository.findOneByNameAndRuntimeId(name, runtimeId))
            .map(Objects::nonNull);
    }

}
