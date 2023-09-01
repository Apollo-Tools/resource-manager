package at.uibk.dps.rm.rx.service.database;

import at.uibk.dps.rm.rx.service.ServiceInterface;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * Classes that implement this interface represent a database service. It defines the most used
 * basic database methods.
 *
 * @author matthi-g
 */
@VertxGen(concrete = false)
public interface DatabaseServiceInterface extends ServiceInterface {

    /**
     * Save a new entity.
     *
     * @param data the new entity
     * @param resultHandler receives the persisted entity if the save process was successful else
     *                      it receives an error
     */
    void save(JsonObject data, Handler<AsyncResult<JsonObject>> resultHandler);

    /**
     * Save a new entity for an account.
     *
     * @param accountId the id of the account
     * @param data the new entity
     * @param resultHandler receives the persisted entity if the save process was successful else
     *                      it receives an error
     */
    void saveToAccount(long accountId, JsonObject data, Handler<AsyncResult<JsonObject>> resultHandler);

    /**
     * Save all new entities.
     *
     * @param data the new entities
     * @param resultHandler receives an empty result if the save process was successful else it
     *                      receives an error
     */
    void saveAll(JsonArray data, Handler<AsyncResult<Void>> resultHandler);

    /**
     * Find one entity by id.
     *
     * @param id the id of the entity
     * @param resultHandler receives the entity as JsonObject if it exists else a
     *                      {@link at.uibk.dps.rm.exception.NotFoundException}
     */
    void findOne(long id, Handler<AsyncResult<JsonObject>> resultHandler);

    /**
     * Find one entity by id and account id.
     *
     * @param id the id of the entity
     * @param accountId the id of the creator's account
     * @param resultHandler receives the entity as JsonObject if it exists else a
     *                      {@link at.uibk.dps.rm.exception.NotFoundException}
     */
    void findOneByIdAndAccountId(long id, long accountId, Handler<AsyncResult<JsonObject>> resultHandler);

    /**
     * Find all entities.
     *
     * @param resultHandler receives the found entities as JsonArray
     */
    void findAll(Handler<AsyncResult<JsonArray>> resultHandler);

    /**
     * Find all entities by the accountId.
     *
     * @param accountId the id of the owner
     * @param resultHandler receives the found entities as JsonArray
     */
    void findAllByAccountId(long accountId, Handler<AsyncResult<JsonArray>> resultHandler);

    /**
     * Update an existing entity.
     *
     * @param id the id of the entity
     * @param fields the existing entity with updated values
     * @param resultHandler receives nothing if the update was successful else an error
     */
    void update(long id, JsonObject fields, Handler<AsyncResult<Void>> resultHandler);

    /**
     * Update an existing entity that is owned by the account.
     *
     * @param id the id of the entity
     * @param accountId the id of the account
     * @param fields the existing entity with updated values
     * @param resultHandler receives nothing if the update was successful else an error
     */
    void updateOwned(long id, long accountId, JsonObject fields, Handler<AsyncResult<Void>> resultHandler);

    /**
     * Delete an entity by its id.
     *
     * @param id the id of the entity
     * @param resultHandler receives nothing if the deletion was successful else an error
     */
    void delete(long id, Handler<AsyncResult<Void>> resultHandler);


    /**
     * Delete an entity from an account.
     *
     * @param accountId the id of the account
     * @param id the id of the entity
     * @param resultHandler receives nothing if the deletion was successful else an error
     */
    void deleteFromAccount(long accountId, long id, Handler<AsyncResult<Void>> resultHandler);
}
