package at.uibk.dps.rm.service.database.function;

import at.uibk.dps.rm.entity.model.Function;
import at.uibk.dps.rm.repository.FunctionRepository;
import at.uibk.dps.rm.service.database.ServiceProxy;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;

public class FunctionServiceImpl extends ServiceProxy<Function> implements FunctionService {
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
            .fromCompletionStage(functionRepository.findByNameAndRuntimeId(excludeId, name, runtimeId))
            .map(result -> !result.isEmpty());
    }

    @Override
    public Future<Boolean> existsOneByNameAndRuntimeId(String name, long runtimeId) {
        return Future
            .fromCompletionStage(functionRepository.findByNameAndRuntimeId(name, runtimeId))
            .map(result -> !result.isEmpty());
    }

}
