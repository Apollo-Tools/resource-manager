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
     * Find all entities, that exist.
     *
     * @return a Single that emits the list of found entities as JsonArray if found, else a
     * NotFoundException gets thrown
     */
    public Single<JsonArray> checkFindAll() {
        return ErrorHandler.handleFindAll(service.findAll());
    }

    /**
     * Check if an entity exists by its id.
     *
     * @param id the id of the entity
     * @return a Completable if it exists, else a NotFoundException gets thrown
     */
    public Completable checkExistsOne(long id) {
        Single<Boolean> existsOneById = service.existsOneById(id);
        return ErrorHandler.handleExistsOne(existsOneById).ignoreElement();
    }

    /**
     * Check if the entity violates uniqueness constraints.
     *
     * @param entity the entity to create
     * @return a Completable if it does not violate uniqueness, else an AlreadyExistsException
     * gets thrown.
     */
    public Completable checkForDuplicateEntity(JsonObject entity) {
        return Single.just(entity).ignoreElement();
    }

    /**
     * Check if an update of an entity violates uniqueness constraints.
     *
     * @param updateEntity the updated entity
     * @param entity the most recent state of the entity
     * @return a Single that emits the most recent entity
     */
    public Single<JsonObject> checkUpdateNoDuplicate(JsonObject updateEntity, JsonObject entity) {
        return Single.just(entity);
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
     * Submit the creation of multiple new entities.
     *
     * @param entities the new entities
     * @return a Completable
     */
    public Completable submitCreateAll(JsonArray entities) {
        return service.saveAll(entities);
    }

    /**
     * Submit the update of an entity.
     *
     * @param updateEntity the updated entity
     * @param entity the most recent state of the entity to update
     * @return a Completable
     */
    public Completable submitUpdate(JsonObject updateEntity, final JsonObject entity) {
        for (String field : updateEntity.fieldNames()) {
            entity.put(field, updateEntity.getValue(field));
        }
        return submitUpdate(entity);
    }

    /**
     * Submit the update of an entity.
     *
     * @param entity the updated entity
     * @return a Completable
     */
    public Completable submitUpdate(JsonObject entity) {
        return service.update(entity);
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
}
