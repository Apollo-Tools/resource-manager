package at.uibk.dps.rm.service.database;

import at.uibk.dps.rm.exception.*;
import at.uibk.dps.rm.repository.Repository;
import at.uibk.dps.rm.service.ServiceProxy;
import at.uibk.dps.rm.service.database.util.SessionManagerProvider;
import at.uibk.dps.rm.util.misc.RxVertxHandler;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
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

    protected final SessionManagerProvider smProvider;

    private final Repository<T> repository;

    private final Class<T> entityClass;

    /**
     * Create an instance from the repository and class of the entity.
     *
     * @param repository the repository
     * @param entityClass the class of the entity
     */
    public DatabaseServiceProxy(Repository<T> repository, Class<T> entityClass, SessionManagerProvider smpProvider) {
        this.repository = repository;
        this.entityClass = entityClass;
        this.smProvider = smpProvider;
    }

    @Override
    public String getServiceProxyAddress() {
        return entityClass.getSimpleName().toLowerCase() + super.getServiceProxyAddress();
    }

    @Override
    public void save(JsonObject data, Handler<AsyncResult<JsonObject>> resultHandler) {
        T entity = data.mapTo(entityClass);
        Single<T> save = smProvider.withTransactionSingle(sm -> sm.persist(entity));
        RxVertxHandler.handleSession(save.map(JsonObject::mapFrom), resultHandler);
    }

    @Override
    public void saveToAccount(long accountId, JsonObject data, Handler<AsyncResult<JsonObject>> resultHandler) {
        resultHandler.handle(Future.failedFuture(new UnsupportedOperationException()));
    }

    @Override
    public void saveAll(JsonArray data, Handler<AsyncResult<Void>> resultHandler) {
        List<T> entities = data
            .stream()
            .map(object -> ((JsonObject) object).mapTo(entityClass))
            .collect(Collectors.toList());
        Completable createAll = smProvider.withTransactionCompletable(sm -> sm.persist(entities.toArray()));
        RxVertxHandler.handleSession(createAll, resultHandler);
    }

    @Override
    public void findOne(long id, Handler<AsyncResult<JsonObject>> resultHandler) {
        Maybe<T> findOne = smProvider.withTransactionMaybe( sm -> sm
            .find(entityClass, id)
            .switchIfEmpty(Maybe.error(new NotFoundException(entityClass)))
        );
        RxVertxHandler.handleSession(findOne.map(JsonObject::mapFrom), resultHandler);
    }


    @Override
    public void findOneByIdAndAccountId(long id, long accountId, Handler<AsyncResult<JsonObject>> resultHandler) {
        Maybe<T> findOne = smProvider.withTransactionMaybe(sm -> repository.findByIdAndAccountId(sm, id, accountId)
            .switchIfEmpty(Maybe.error(new NotFoundException(entityClass)))
        );
        RxVertxHandler.handleSession(findOne.map(JsonObject::mapFrom), resultHandler);
    }

    @Override
    public void findAll(Handler<AsyncResult<JsonArray>> resultHandler) {
        Single<List<T>> findAll = smProvider.withTransactionSingle(repository::findAll);
        RxVertxHandler.handleSession(findAll.map(this::mapResultListToJsonArray), resultHandler);
    }

    @Override
    public void findAllByAccountId(long accountId, Handler<AsyncResult<JsonArray>> resultHandler) {
        Single<List<T>> findAll = smProvider.withTransactionSingle(sm -> repository
            .findAllByAccountId(sm, accountId));
        RxVertxHandler.handleSession(findAll.map(this::mapResultListToJsonArray), resultHandler);
    }

    /**
     * Map a list of found entities to a JsonArray
     *
     * @param resultList the result list
     * @return the mapped JsonArray
     */
    @NotNull
    protected <E> JsonArray mapResultListToJsonArray(List<E> resultList) {
        ArrayList<JsonObject> objects = new ArrayList<>();
        for (E entity : resultList) {
            objects.add(JsonObject.mapFrom(entity));
        }
        return new JsonArray(objects);
    }

    @Override
    public void update(long id, JsonObject fields, Handler<AsyncResult<Void>> resultHandler) {
        Completable update = smProvider.withTransactionCompletable(sm -> sm.find(entityClass, id)
            .switchIfEmpty(Single.error(new NotFoundException(entityClass)))
            .flatMap(entity -> {
                JsonObject jsonObject = JsonObject.mapFrom(entity);
                fields.stream().forEach(entry -> jsonObject.put(entry.getKey(), entry.getValue()));
                T updatedEntity = jsonObject.mapTo(entityClass);
                return sm.merge(updatedEntity);
            })
            .ignoreElement()
        );
        RxVertxHandler.handleSession(update, resultHandler);
    }

    @Override
    public void updateOwned(long id, long accountId, JsonObject fields, Handler<AsyncResult<Void>> resultHandler) {
        resultHandler.handle(Future.failedFuture(new UnsupportedOperationException()));
    }

    @Override
    public void delete(long id, Handler<AsyncResult<Void>> resultHandler) {
        Completable delete = smProvider.withTransactionCompletable(sm -> sm
            .find(entityClass, id)
            .switchIfEmpty(Maybe.error(new NotFoundException(entityClass)))
            .flatMapCompletable(sm::remove)
        );
        RxVertxHandler.handleSession(delete, resultHandler);
    }

    @Override
    public void deleteFromAccount(long accountId, long id, Handler<AsyncResult<Void>> resultHandler) {
        Completable delete = smProvider.withTransactionCompletable(sm -> repository
            .findByIdAndAccountId(sm, id, accountId)
            .switchIfEmpty(Maybe.error(new NotFoundException(entityClass)))
            .flatMapCompletable(sm::remove)
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
}
