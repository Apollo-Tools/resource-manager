package at.uibk.dps.rm.service.database;

import at.uibk.dps.rm.exception.*;
import at.uibk.dps.rm.repository.Repository;
import at.uibk.dps.rm.service.ServiceProxy;
import at.uibk.dps.rm.service.database.util.SessionManager;
import at.uibk.dps.rm.util.misc.RxVertxHandler;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.hibernate.reactive.stage.Stage.SessionFactory;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
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

    @Override
    public String getServiceProxyAddress() {
        return entityClass.getSimpleName().toLowerCase() + super.getServiceProxyAddress();
    }

    @Override
    public void save(JsonObject data, Handler<AsyncResult<JsonObject>> resultHandler) {
        T entity = data.mapTo(entityClass);
        Single<T> save = withTransactionSingle(sessionManager -> sessionManager.persist(entity));
        RxVertxHandler.handleSession(save.map(JsonObject::mapFrom), resultHandler);
    }

    @Override
    public void saveToAccount(long accountId, JsonObject data, Handler<AsyncResult<JsonObject>> resultHandler) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void saveAll(JsonArray data, Handler<AsyncResult<Void>> resultHandler) {
        List<T> entities = data
            .stream()
            .map(object -> ((JsonObject) object).mapTo(entityClass))
            .collect(Collectors.toList());
        Completable createAll = withTransactionCompletable(sessionManager ->
            repository.createAll(sessionManager, entities));
        RxVertxHandler.handleSession(createAll, resultHandler);
    }

    @Override
    public void findOne(long id, Handler<AsyncResult<JsonObject>> resultHandler) {
        Maybe<T> findOne = withTransactionMaybe(sessionManager -> sessionManager.find(entityClass, id)
            .switchIfEmpty(Maybe.error(new NotFoundException(entityClass)))
        );
        RxVertxHandler.handleSession(findOne.map(JsonObject::mapFrom), resultHandler);
    }


    @Override
    public void findOneByIdAndAccountId(long id, long accountId, Handler<AsyncResult<JsonObject>> resultHandler) {
        Single<T> findOne = withTransactionSingle(sessionManager -> repository
            .findByIdAndAccountId(sessionManager, id, accountId)
            .switchIfEmpty(Maybe.error(new NotFoundException(entityClass)))
            .toSingle()
        );
        RxVertxHandler.handleSession(findOne.map(JsonObject::mapFrom), resultHandler);
    }

    @Override
    public void findAll(Handler<AsyncResult<JsonArray>> resultHandler) {
        Single<List<T>> findAll = withTransactionSingle(repository::findAll);
        RxVertxHandler.handleSession(findAll.map(this::mapResultListToJsonArray), resultHandler);
    }

    @Override
    public void findAllByAccountId(long accountId, Handler<AsyncResult<JsonArray>> resultHandler) {
        Single<List<T>> findAll = withTransactionSingle(sessionManager -> repository
            .findAllByAccountId(sessionManager, accountId));
        RxVertxHandler.handleSession(findAll.map(this::mapResultListToJsonArray), resultHandler);
    }

    @NotNull
    private JsonArray mapResultListToJsonArray(List<T> resultList) {
        ArrayList<JsonObject> objects = new ArrayList<>();
        for (T entity : resultList) {
            objects.add(JsonObject.mapFrom(entity));
        }
        return new JsonArray(objects);
    }

    @Override
    public void update(long id, JsonObject fields, Handler<AsyncResult<Void>> resultHandler) {
        Completable update = withTransactionCompletable(sessionManager -> sessionManager.find(entityClass, id)
            .switchIfEmpty(Maybe.error(new NotFoundException(entityClass)))
            .map(entity -> {
                JsonObject jsonObject = JsonObject.mapFrom(entity);
                fields.stream().forEach(entry -> jsonObject.put(entry.getKey(), entry.getValue()));
                T updatedEntity = jsonObject.mapTo(entityClass);
                return sessionManager.merge(updatedEntity);
            })
            .ignoreElement()
        );
        RxVertxHandler.handleSession(update, resultHandler);
    }

    @Override
    public void updateOwned(long id, long accountId, JsonObject fields, Handler<AsyncResult<Void>> resultHandler) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void delete(long id, Handler<AsyncResult<Void>> resultHandler) {
        Completable delete = withTransactionCompletable(sessionManager -> sessionManager.find(entityClass, id)
            .switchIfEmpty(Maybe.error(new NotFoundException(entityClass)))
            .flatMapCompletable(sessionManager::remove)
        );
        RxVertxHandler.handleSession(delete, resultHandler);
    }

    @Override
    public void deleteFromAccount(long accountId, long id, Handler<AsyncResult<Void>> resultHandler) {
        Completable delete = withTransactionCompletable(sessionManager -> repository
            .findByIdAndAccountId(sessionManager, id, accountId)
            .switchIfEmpty(Maybe.error(new NotFoundException(entityClass)))
            .flatMapCompletable(sessionManager::remove)
        );
        RxVertxHandler.handleSession(delete, resultHandler);
    }

    /**
     * Return the new value if it is not null, else return the old value
     *
     * @param oldValue the old value
     * @param newValue the new value
     * @return the new value if not null, else the old value
     * @param <E> any datatype
     */
    protected <E> E updateNonNullValue(E oldValue, E newValue) {
        return newValue == null ? oldValue : newValue;
    }



    /**
     * Perform work using a reactive session. The executions contained in function are
     * transactional.
     *
     * @param function the function that contains all database operations
     * @return a Single that emits an item of type E
     * @param <E> any datatype that can be returned by the reactive session
     */
    protected <E> Single<E> withTransactionSingle(Function<SessionManager, Single<E>> function) {
        CompletionStage<E> transaction = sessionFactory.withTransaction(session -> {
            SessionManager sessionManager = new SessionManager(session);
            return function.apply(sessionManager).toCompletionStage();
        });
        return Single.fromCompletionStage(transaction);
    }

    /**
     * Perform work using a reactive session. The executions contained in function are
     * transactional.
     *
     * @param function the function that contains all database operations
     * @return a Maybe that emits an item of type E
     * @param <E> any datatype that can be returned by the reactive session
     */
    protected <E> Maybe<E> withTransactionMaybe(Function<SessionManager, Maybe<E>> function) {
        CompletionStage<E> transaction = sessionFactory.withTransaction(session -> {
            SessionManager sessionManager = new SessionManager(session);
            return function.apply(sessionManager).toCompletionStage();
        });
        return Maybe.fromCompletionStage(transaction);
    }

    /**
     * Perform work using a reactive session. The executions contained in function are
     * transactional.
     *
     * @param function the function that contains all database operations
     * @return a Completable
     */
    protected Completable withTransactionCompletable(Function<SessionManager, Completable> function) {
        CompletionStage<Void> transaction = sessionFactory.withTransaction(session -> {
            SessionManager sessionManager = new SessionManager(session);
            return function.apply(sessionManager).toCompletionStage(null);
        });
        return Completable.fromCompletionStage(transaction);
    }
}
