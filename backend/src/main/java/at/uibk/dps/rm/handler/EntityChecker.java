package at.uibk.dps.rm.handler;

import at.uibk.dps.rm.service.rxjava3.database.DatabaseServiceInterface;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * Classes that inherit the EntityChecker implement methods to perform CRUD operations on a
 * specific database entity. The service indirectly determines the entity type.
 *
 * @author matthi-g
 */
@Deprecated
public abstract class EntityChecker {

    private final DatabaseServiceInterface service;

    /**
     * Create an instance from a service interface.
     *
     * @param service the service to use
     */
    public EntityChecker(DatabaseServiceInterface service) {
        this.service = service;
    }

    /**
     * Find the entity by its id and return it, if found.
     *
     * @param id the id of the entity
     * @return a Single that emits the entity as JsonObject if found, else a NotFoundException
     * gets thrown
     */
    public Single<JsonObject> checkFindOne(long id) {
        Single<JsonObject> findOneById = service.findOne(id);
        return ErrorHandler.handleFindOne(findOneById);
    }

    /**
     * Find the entity by its id and account id and return it, if found.
     *
     * @param id the id of the entity
     * @param accountId  the id of the account
     * @return a Single that emits the found entity as JsonObject if found, else a NotFoundException
     * gets thrown
     */
    public Single<JsonObject> checkFindOne(long id, long accountId) {
        Single<JsonObject> findOneById = service.findOneByIdAndAccountId(id, accountId);
        return ErrorHandler.handleFindOne(findOneById);
    }

    /**
     * Find all entities, that exist.
     *
     * @return a Single that emits the list of found entities as JsonArray
     */
    public Single<JsonArray> checkFindAll() {
        return ErrorHandler.handleFindAll(service.findAll());
    }

    /**
     * Find all entities by the accountId.
     *
     * @return a Single that emits the list of found entities as JsonArray
     */
    public Single<JsonArray> checkFindAll(long accountId) {
        return ErrorHandler.handleFindAll(service.findAllByAccountId(accountId));
    }

    /**
     * Submit the creation of a new entity.
     *
     * @param entity the new entity
     * @return a Single that emits the persisted entity
     */
    public Single<JsonObject> submitCreate(JsonObject entity) {
        return service.save(entity);
    }

    /**
     * Submit the creation of a new entity.
     *
     * @param accountId the id of the creator
     * @param entity the new entity
     * @return a Single that emits the persisted entity
     */
    public Single<JsonObject> submitCreate(long accountId, JsonObject entity) {
        return service.saveToAccount(accountId, entity);
    }

    /**
     * Submit the creation of multiple new entities.
     *
     * @param entities the new entities
     * @return a Completable
     */
    public Completable submitCreateAll(JsonArray entities) {
        return service.saveAll(entities);
    }

    /**
     * Submit the update of all fields.
     *
     * @param fields the updated fields
     * @return a Completable
     */
    public Completable submitUpdate(long id, JsonObject fields) {
        return service.update(id, fields);
    }

    /**
     * Submit the update of all fields for an owned entity.
     *
     * @param id the id of the entity
     * @param accountId the id of the owner
     * @param fields the updated fields
     * @return a Completable
     */
    public Completable submitUpdate(long id, long accountId, JsonObject fields) {
        return service.updateOwned(id, accountId, fields);
    }

    /**
     * Submit the deletion of an entity by its id.
     *
     * @param id the id of the entity
     * @return a Completable
     */
    public Completable submitDelete(long id) {
        return service.delete(id);
    }

    /**
     * Submit the deletion of an entity by its id and owner.
     *
     * @param accountId the id of the owner
     * @param id the id of the entity
     * @return a Completable
     */
    public Completable submitDelete(long accountId, long id) {
        return service.deleteFromAccount(accountId, id);
    }
}
