package at.uibk.dps.rm.handler;

import at.uibk.dps.rm.util.misc.HttpHelper;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RoutingContext;

/**
 * Classes that inherit the ValidationHandler compose methods of different
 * {@link EntityChecker}s to execute the different types of requests, that the API of the Resource
 * Manager provides. The major methods are executed using the entity checker which determines
 * which type of entity is getting created, read, updated or deleted.
 *
 * @author matthi-g
 */
public abstract class ValidationHandler {

    protected final EntityChecker entityChecker;

    /**
     * Create an instance from a entityChecker that determines the type of entity.
     *
     * @param entityChecker the main entityChecker
     */
    protected ValidationHandler(EntityChecker entityChecker) {
        this.entityChecker = entityChecker;
    }

    /**
     * Delete an entity by its id.
     *
     * @param rc the RoutingContext of the request
     * @return a Completable
     */
    protected Completable deleteOne(RoutingContext rc) {
        return HttpHelper.getLongPathParam(rc, "id")
            .flatMapCompletable(entityChecker::submitDelete);
    }

    /**
     * Find and return an entity by its id.
     *
     * @param rc the RoutingContext of the request
     * @return a Single that emits the found entity as JsonObject
     */
    protected Single<JsonObject> getOne(RoutingContext rc) {
        return HttpHelper.getLongPathParam(rc, "id")
            .flatMap(entityChecker::checkFindOne);
    }

    /**
     * Find and return all existing entities.
     *
     * @param rc the RoutingContext of the request
     * @return a Single that emits the found entity as JsonObject
     */
    protected Single<JsonArray> getAll(RoutingContext rc) {
        return entityChecker.checkFindAll();
    }

    /**
     * Create a new entity.
     *
     * @param rc the RoutingContext of the request
     * @return a Single that emits the created entity as JsonObject
     */
    protected Single<JsonObject> postOne(RoutingContext rc) {
        JsonObject requestBody = rc.body().asJsonObject();
        return entityChecker.submitCreate(requestBody);
    }

    /**
     * Create all entities.
     *
     * @param rc the RoutingContext of the request
     * @return a Completable
     */
    protected Completable postAll(RoutingContext rc) {
        JsonArray requestBody = rc.body().asJsonArray();
        return entityChecker.submitCreateAll(requestBody);
    }

    /**
     * Update an entitiy identified by its id.
     *
     * @param rc the RoutingContext of the request
     * @return a Completable
     */
    protected Completable updateOne(RoutingContext rc) {
        JsonObject requestBody = rc.body().asJsonObject();
        return HttpHelper.getLongPathParam(rc, "id")
            .flatMapCompletable(id -> entityChecker.submitUpdate(id, requestBody));
    }

    // TODO: move to entity checker
    /**
     * Check if the entity to delete is used.
     *
     * @param entity the entity
     * @return a Completable
     */
    protected Completable checkDeleteEntityIsUsed(JsonObject entity) {
        return Single.just(entity).ignoreElement();
    }
}
