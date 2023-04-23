package at.uibk.dps.rm.service.database;

import at.uibk.dps.rm.service.ServiceInterface;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
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
     * @return a Future that emits the persisted entity as JsonObject
     */
    Future<JsonObject> save(JsonObject data);

    /**
     * Save all new entities.
     *
     * @param data the new entities
     * @return an empty Future
     */
    Future<Void> saveAll(JsonArray data);

    /**
     * Find one entity by id.
     *
     * @param id the id of the entity
     * @return a Future that emits the found entity if it exists, else null
     */
    Future<JsonObject> findOne(long id);

    /**
     * Check if an entity exists by its id.
     *
     * @param id the id of the entity
     * @return a Future that emits true if the entity exists, else false
     */
    Future<Boolean> existsOneById(long id);

    /**
     * Find all entities.
     *
     * @return a Future that emits all entities as JsonArray
     */
    Future<JsonArray> findAll();

    /**
     * Update an existing entity.
     *
     * @param data the existing entity with updated values
     * @return an empty Future
     */
    Future<Void> update(JsonObject data);

    /**
     * Delete an entity by its id.
     *
     * @param id the id of the entity
     * @return an empty Future
     */
    Future<Void> delete(long id);
}
