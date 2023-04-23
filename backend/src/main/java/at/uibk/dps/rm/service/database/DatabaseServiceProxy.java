package at.uibk.dps.rm.service.database;

import at.uibk.dps.rm.repository.Repository;
import at.uibk.dps.rm.service.ServiceProxy;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * This class is an abstract implementation of the {@link DatabaseServiceInterface}. For the
 * implementation of the methods a {@link Repository} is used that has to be provided to the
 * constructor. The repository determines which type of entity is getting modified by an extension
 * of this class.
 *
 * @param <T> the type of entity
 */
public abstract class DatabaseServiceProxy<T> extends ServiceProxy implements DatabaseServiceInterface {
    private final Repository<T> repository;

    private final Class<T> entityClass;

    /**
     * Create an instance from the repository and class of the entity.
     *
     * @param repository the repository
     * @param entityClass the class of the entity
     */
    public DatabaseServiceProxy(Repository<T> repository, Class<T> entityClass) {
        this.repository = repository;
        this.entityClass = entityClass;
    }

    @Override
    public String getServiceProxyAddress() {
        return entityClass.getSimpleName().toLowerCase() + super.getServiceProxyAddress();
    }

    @Override
    public Future<JsonObject> save(JsonObject data) {
        T entity = data.mapTo(entityClass);
        return Future
            .fromCompletionStage(repository.create(entity))
            .map(JsonObject::mapFrom);
    }

    @Override
    public Future<Void> saveAll(JsonArray data) {
        List<T> entities = data
            .stream()
            .map(object -> ((JsonObject) object).mapTo(entityClass))
            .collect(Collectors.toList());

        return Future
            .fromCompletionStage(repository.createAll(entities));
    }

    @Override
    public Future<JsonObject> findOne(long id) {
        return Future
            .fromCompletionStage(repository.findById(id))
            .map(JsonObject::mapFrom);
    }

    @Override
    public Future<Boolean> existsOneById(long id) {
        return Future
            .fromCompletionStage(repository.findById(id))
            .map(Objects::nonNull);
    }

    @Override
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

    @Override
    public Future<Void> update(JsonObject data) {
        T entity = data.mapTo(entityClass);
        return Future
            .fromCompletionStage(repository.update(entity))
            .mapEmpty();
    }

    @Override
    public Future<Void> delete(long id) {
        return Future
            .fromCompletionStage(repository.deleteById(id))
            .mapEmpty();
    }
}
