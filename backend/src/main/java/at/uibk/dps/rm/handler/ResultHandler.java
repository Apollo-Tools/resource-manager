package at.uibk.dps.rm.handler;

import at.uibk.dps.rm.exception.*;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RoutingContext;

/**
 * This class is used to handle the different types of requests, that the API can receive.
 * - FIND ONE
 * - FIND ALL
 * - POST ONE
 * - POST ALL
 * - UPDATE ONE
 * - DELETE ONE
 *
 * @author matthi-g
 */
public class ResultHandler {

    private final ValidationHandler validationHandler;

    /**
     * Create an instance from a validation handler.
     *
     * @param validationHandler the validation handler
     */
    public ResultHandler(final ValidationHandler validationHandler) {
        this.validationHandler = validationHandler;
    }

    /**
     * Handle the FIND ONE request of the validation handler.
     *
     * @param rc the RoutingContext of the request
     */
    public void handleFindOneRequest(final RoutingContext rc) {
        validationHandler.getOne(rc)
            .subscribe(result -> getFindOneResponse(rc, result), throwable -> handleRequestError(rc, throwable));
    }

    /**
     * Handle a custom FIND ONE request.
     *
     * @param rc the RoutingContext of the request
     * @param handler the handler of the get one request
     */
    public void handleFindOneRequest(final RoutingContext rc, final Single<JsonObject> handler) {
        handler.subscribe(result -> getFindOneResponse(rc, result), throwable -> handleRequestError(rc, throwable));
    }

    /**
     * Handle the FIND ALL request of the validation handler.
     *
     * @param rc the RoutingContext of the request
     */
    public void handleFindAllRequest(final RoutingContext rc) {
        validationHandler.getAll(rc)
            .subscribe(result -> getFindAllResponse(rc, result),
                throwable -> handleRequestError(rc, throwable));
    }

    /**
     * Handle a custom FIND ALL request.
     *
     * @param rc the RoutingContext of the request
     * @param handler the handler of the get one request
     */
    public void handleFindAllRequest(final RoutingContext rc, final Single<JsonArray> handler) {
        handler.subscribe(result -> getFindAllResponse(rc, result), throwable -> handleRequestError(rc, throwable));
    }

    /**
     * Handle the POST ONE request of the validation handler.
     *
     * @param rc the RoutingContext of the request
     */
    public void handleSaveOneRequest(final RoutingContext rc) {
        validationHandler.postOne(rc)
            .subscribe(result -> getSaveResponse(rc, result), throwable -> handleRequestError(rc, throwable));
    }

    /**
     * Handle the POST ALL request of the validation handler.
     *
     * @param rc the RoutingContext of the request
     */
    public void handleSaveAllRequest(final RoutingContext rc) {
        validationHandler.postAll(rc)
            .subscribe(() -> getSaveAllUpdateDeleteResponse(rc), throwable -> handleRequestError(rc, throwable));
    }

    /**
     * Handle the UPDATE ONE request of the validation handler.
     *
     * @param rc the RoutingContext of the request
     */
    public void handleUpdateRequest(final RoutingContext rc) {
        validationHandler.updateOne(rc)
            .subscribe(() -> getSaveAllUpdateDeleteResponse(rc), throwable -> handleRequestError(rc, throwable));
    }

    /**
     * Handle the UPDATE ONE request of the validation handler.
     *
     * @param rc the RoutingContext of the request
     */
    public void handleUpdateRequest(final RoutingContext rc, final Completable handler) {
        handler.subscribe(() -> getSaveAllUpdateDeleteResponse(rc), throwable -> handleRequestError(rc, throwable));
    }

    /**
     * Handle the DELETE ONE request of the validation handler.
     *
     * @param rc the RoutingContext of the request
     */
    public void handleDeleteRequest(final RoutingContext rc) {
        validationHandler.deleteOne(rc)
            .subscribe(() -> getSaveAllUpdateDeleteResponse(rc), throwable -> handleRequestError(rc, throwable));
    }

    /**
     * Set the status code and content of the response for a successful FIND ONE request.
     *
     * @param rc the RoutingContext of the request
     * @param result the content of the response
     */
    public static void getFindOneResponse(final RoutingContext rc, final JsonObject result) {
        rc.response().setStatusCode(200).end(result.encodePrettily());
    }

    /**
     * Set the status code and content of the response for a successful FIND ALL request.
     *
     * @param rc the RoutingContext of the request
     * @param result the content of the response
     */
    public static void getFindAllResponse(final RoutingContext rc, final JsonArray result) {
        rc.response().setStatusCode(200).end(result.encodePrettily());
    }

    /**
     * Set the status code and content of the response for a successful SAVE ONE request.
     *
     * @param rc the RoutingContext of the request
     * @param result the content of the response
     */
    public static void getSaveResponse(final RoutingContext rc, final JsonObject result) {
        rc.response().setStatusCode(201).end(result.encodePrettily());
    }

    /**
     * Set the status code and content of the response for a successful SAVE ALL, UPDATE ONE or
     * DELETE ONE request.
     *
     * @param rc the RoutingContext of the request
     */
    public static void getSaveAllUpdateDeleteResponse(final RoutingContext rc) {
        rc.response().setStatusCode(204).end();
    }

    /**
     * Set the status code based on the type of the throwable.
     *
     * @param rc the RoutingContext of the request
     * @param throwable the caught throwable
     */
    public static void handleRequestError(final RoutingContext rc, final Throwable throwable) {
        int statusCode = 500;
        if (throwable instanceof NotFoundException) {
            statusCode = 404;
        }  else if (throwable instanceof AlreadyExistsException ||
            throwable instanceof UsedByOtherEntityException) {
            statusCode = 409;
        } else if (throwable instanceof BadInputException) {
            statusCode = 400;
        } else if (throwable instanceof UnauthorizedException) {
            statusCode = 401;
        }
        rc.fail(statusCode, throwable);
    }
}
