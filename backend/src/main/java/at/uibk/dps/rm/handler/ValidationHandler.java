package at.uibk.dps.rm.handler;

import at.uibk.dps.rm.service.rxjava3.database.DatabaseServiceInterface;
import at.uibk.dps.rm.util.misc.HttpHelper;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RoutingContext;

/**
 * Classes that inherit the ValidationHandler compose handle the request body and path parameters
 * of API requests and forward them to the service layer for further processing. The main methods
 * are create, read, update or delete.
 *
 * @author matthi-g
 */
public abstract class ValidationHandler {

    private final DatabaseServiceInterface service;

    /**
     * Create an instance from a service.
     *
     * @param service the database service
     */
    protected ValidationHandler(DatabaseServiceInterface service) {
        this.service = service;
    }

    /**
     * Delete an entity by its id.
     *
     * @param rc the RoutingContext of the request
     * @return a Completable
     */
    protected Completable deleteOne(RoutingContext rc) {
        return HttpHelper.getLongPathParam(rc, "id")
            .flatMapCompletable(service::delete);
    }

    /**
     * Delete an entity by its id from the logged-in account.
     *
     * @param rc the RoutingContext of the request
     * @return a Completable
     */
    protected Completable deleteOneFromAccount(RoutingContext rc) {
        long accountId = rc.user().principal().getLong("account_id");
        return HttpHelper.getLongPathParam(rc, "id")
            .flatMapCompletable(id -> service.deleteFromAccount(accountId, id));
    }

    /**
     * Find and return an entity by its id.
     *
     * @param rc the RoutingContext of the request
     * @return a Single that emits the found entity as JsonObject
     */
    protected Single<JsonObject> getOne(RoutingContext rc) {
        return HttpHelper.getLongPathParam(rc, "id")
            .flatMap(service::findOne);
    }

    /**
     * Find and return an entity by its id that is linked to the logged in account.
     *
     * @param rc the RoutingContext of the request
     * @return a Single that emits the found entity as JsonObject
     */
    protected Single<JsonObject> getOneFromAccount(RoutingContext rc) {
        long accountId = rc.user().principal().getLong("account_id");
        return HttpHelper.getLongPathParam(rc, "id")
            .flatMap(id -> service.findOneByIdAndAccountId(id, accountId));
    }

    /**
     * Find and return all existing entities.
     *
     * @param rc the RoutingContext of the request
     * @return a Single that emits the found entity as JsonObject
     */
    protected Single<JsonArray> getAll(RoutingContext rc) {
        return service.findAll();
    }

    /**
     * Find and return all existing entities that are linked to the logged in account.
     *
     * @param rc the RoutingContext of the request
     * @return a Single that emits the found entity as JsonObject
     */
    protected Single<JsonArray> getAllFromAccount(RoutingContext rc) {
        long accountId = rc.user().principal().getLong("account_id");
        return service.findAllByAccountId(accountId);
    }

    /**
     * Create a new entity.
     *
     * @param rc the RoutingContext of the request
     * @return a Single that emits the created entity as JsonObject
     */
    protected Single<JsonObject> postOne(RoutingContext rc) {
        JsonObject requestBody = rc.body().asJsonObject();
        return service.save(requestBody);
    }

    /**
     * Create a new entity for the logged-in user.
     *
     * @param rc the RoutingContext of the request
     * @return a Single that emits the created entity as JsonObject
     */
    protected Single<JsonObject> postOneToAccount(RoutingContext rc) {
        JsonObject requestBody = rc.body().asJsonObject();
        long accountId = rc.user().principal().getLong("account_id");
        return service.saveToAccount(accountId, requestBody);
    }

    /**
     * Create all entities.
     *
     * @param rc the RoutingContext of the request
     * @return a Completable
     */
    protected Completable postAll(RoutingContext rc) {
        JsonArray requestBody = rc.body().asJsonArray();
        return service.saveAll(requestBody);
    }

    /**
     * Update an entity identified by its id.
     *
     * @param rc the RoutingContext of the request
     * @return a Completable
     */
    protected Completable updateOne(RoutingContext rc) {
        JsonObject requestBody = rc.body().asJsonObject();
        return HttpHelper.getLongPathParam(rc, "id")
            .flatMapCompletable(id -> service.update(id, requestBody));
    }

    /**
     * Update an entity that is identified by its id and owned by the logged in account.
     *
     * @param rc the RoutingContext of the request
     * @return a Completable
     */
    protected Completable updateOneOwned(RoutingContext rc) {
        JsonObject requestBody = rc.body().asJsonObject();
        long accountId = rc.user().principal().getLong("account_id");
        return HttpHelper.getLongPathParam(rc, "id")
            .flatMapCompletable(id -> service.updateOwned(id, accountId, requestBody));
    }
}
