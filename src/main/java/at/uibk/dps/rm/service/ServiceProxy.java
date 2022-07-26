package at.uibk.dps.rm.service;

import at.uibk.dps.rm.repository.Repository;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class ServiceProxy<T> implements ServiceInterface{
    private final Repository<T> repository;

    private final Class<T> entityClass;

    public ServiceProxy(Repository<T> repository, Class<T> entityClass) {
        this.repository = repository;
        this.entityClass = entityClass;
    }

    public Future<JsonObject> save(JsonObject data) {
        T entity = data.mapTo(entityClass);
        return Future
            .fromCompletionStage(repository.create(entity))
            .map(JsonObject::mapFrom);
    }

    public Future<Void> saveAll(JsonArray data) {
        List<T> metricValues = data
            .stream()
            .map(object -> ((JsonObject) object).mapTo(entityClass))
            .collect(Collectors.toList());

        return Future
            .fromCompletionStage(repository.createAll(metricValues));
    }

    public Future<JsonObject> findOne(long id) {
        return Future
            .fromCompletionStage(repository.findById(id))
            .map(JsonObject::mapFrom);
    }

    public Future<Boolean> existsOneById(long id) {
        return Future
            .fromCompletionStage(repository.findById(id))
            .map(Objects::nonNull);
    }

    public Future<JsonArray> findAll() {
        return Future
            .fromCompletionStage(repository.findAll())
            .map(result -> {
                ArrayList<JsonObject> objects = new ArrayList<>();
                for (T entity: result) {
                    objects.add(JsonObject.mapFrom(entity));
                }
                return new JsonArray(objects);
            });
    }

    public Future<Void> update(JsonObject data) {
        T entity = data.mapTo(entityClass);
        return Future
            .fromCompletionStage(repository.update(entity))
            .mapEmpty();
    }

    public Future<Void> delete(long id) {
        return Future
            .fromCompletionStage(repository.deleteById(id))
            .mapEmpty();
    }
}
