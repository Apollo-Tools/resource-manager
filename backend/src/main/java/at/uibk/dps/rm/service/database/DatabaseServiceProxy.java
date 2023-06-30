package at.uibk.dps.rm.service.database;

import at.uibk.dps.rm.exception.AlreadyExistsException;
import at.uibk.dps.rm.exception.BadInputException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.exception.UnauthorizedException;
import at.uibk.dps.rm.repository.Repository;
import at.uibk.dps.rm.service.ServiceProxy;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.hibernate.reactive.stage.Stage.Session;
import org.hibernate.reactive.stage.Stage.SessionFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
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

    private final SessionFactory sessionFactory;

    private final Repository<T> repository;

    private final Class<T> entityClass;

    /**
     * Create an instance from the repository and class of the entity.
     *
     * @param repository the repository
     * @param entityClass the class of the entity
     */
    public DatabaseServiceProxy(Repository<T> repository, Class<T> entityClass, SessionFactory sessionFactory) {
        this.repository = repository;
        this.entityClass = entityClass;
        this.sessionFactory = sessionFactory;
    }

    protected <E> CompletionStage<E> withSession(Function<Session, CompletionStage<E>> function) {
        return sessionFactory.withSession(function);
    }

    protected <E> CompletionStage<E> withTransaction(Function<Session, CompletionStage<E>> function) {
        return sessionFactory.withTransaction(function);
    }

    @Override
    public String getServiceProxyAddress() {
        return entityClass.getSimpleName().toLowerCase() + super.getServiceProxyAddress();
    }

    @Override
    public Future<JsonObject> save(JsonObject data) {
        T entity = data.mapTo(entityClass);
        CompletionStage<T> create = withTransaction(session -> repository.create(session, entity));
        return Future.fromCompletionStage(create)
            .recover(this::recoverFailure)
            .map(JsonObject::mapFrom);
    }

    @Override
    public Future<Void> saveAll(JsonArray data) {
        List<T> entities = data
            .stream()
            .map(object -> ((JsonObject) object).mapTo(entityClass))
            .collect(Collectors.toList());
        CompletionStage<Void> createAll = withTransaction(session -> repository.createAll(session, entities));
        return Future.fromCompletionStage(createAll)
            .recover(this::recoverFailure);
    }

    @Override
    public Future<JsonObject> findOne(long id) {
        CompletionStage<T> findOne = withSession(session -> repository.findById(session, id));
        return Future.fromCompletionStage(findOne)
            .recover(this::recoverFailure)
            .map(JsonObject::mapFrom);
    }

    @Override
    public Future<Boolean> existsOneById(long id) {
        CompletionStage<T> findOne = withSession(session -> repository.findById(session, id));
        return Future.fromCompletionStage(findOne)
            .recover(this::recoverFailure)
            .map(Objects::nonNull);
    }

    @Override
    public Future<JsonArray> findAll() {
        CompletionStage<List<T>> findAll = withSession(repository::findAll);
        return Future
            .fromCompletionStage(findAll)
            .recover(this::recoverFailure)
            .map(result -> {
                ArrayList<JsonObject> objects = new ArrayList<>();
                for (T entity: result) {
                    objects.add(JsonObject.mapFrom(entity));
                }
                return new JsonArray(objects);
            });
    }

    @Override
    public Future<Void> update(long id, JsonObject fields) {
        CompletionStage<T> update = withTransaction(session -> repository.findById(session, id)
            .thenCompose(entity -> {
                if (entity == null) {
                    throw new NotFoundException(entityClass);
                }
                JsonObject jsonObject = JsonObject.mapFrom(entity);
                fields.stream().forEach(entry -> jsonObject.put(entry.getKey(), entry.getValue()));
                T updatedEntity = jsonObject.mapTo(entityClass);
                return session.merge(updatedEntity);
            })
        );
        return Future
            .fromCompletionStage(update)
            .recover(this::recoverFailure)
            .mapEmpty();
    }

    @Override
    public Future<Void> delete(long id) {
        CompletionStage<Void> delete = withTransaction(session -> repository.findById(session, id)
            .thenAccept(entity -> {
                if (entity == null) {
                    throw new NotFoundException(entityClass);
                }
                session.remove(entity);
            })
        );
        return Future.fromCompletionStage(delete)
            .recover(this::recoverFailure)
            .mapEmpty();
    }

    protected <E> Future<E> recoverFailure(Throwable throwable) {
        if (throwable.getCause() instanceof NotFoundException) {
            throw new NotFoundException((NotFoundException) throwable.getCause());
        } else if (throwable.getCause() instanceof UnauthorizedException) {
            throw new UnauthorizedException((UnauthorizedException) throwable.getCause());
        } else if (throwable.getCause() instanceof BadInputException) {
            throw new BadInputException((BadInputException) throwable.getCause());
        } else if (throwable.getCause() instanceof AlreadyExistsException) {
            throw new AlreadyExistsException((AlreadyExistsException) throwable.getCause());
        }
        return Future.failedFuture(throwable);
    }

    protected <E> E updateNonNullValue(E oldValue, E newValue) {
        return newValue == null ? oldValue : newValue;
    }
}
