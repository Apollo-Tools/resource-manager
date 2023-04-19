package at.uibk.dps.rm.handler;

import at.uibk.dps.rm.exception.*;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
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

    public ResultHandler(ValidationHandler validationHandler) {
        this.validationHandler = validationHandler;
    }

    /**
     * Handle the FIND ONE request of the validation handler.
     *
     * @param rc the RoutingContext of the request
     * @return a Disposable
     */
    public Disposable handleFindOneRequest(RoutingContext rc) {
        return validationHandler.getOne(rc)
            .subscribe(result -> getFindOneResponse(rc, result),
                throwable -> handleRequestError(rc, throwable));
    }

    /**
     * Handle a custom FIND ONE request.
     *
     * @param rc the RoutingContext of the request
     * @param handler the handler of the get one request
     * @return a Disposable
     */
    public Disposable handleFindOneRequest(RoutingContext rc, Single<JsonObject> handler) {
        return handler
            .subscribe(result -> getFindOneResponse(rc, result),
                throwable -> handleRequestError(rc, throwable));
    }

    /**
     * Handle the FIND ALL request of the validation handler.
     *
     * @param rc the RoutingContext of the request
     * @return a Disposable
     */
    public Disposable handleFindAllRequest(RoutingContext rc) {
        return validationHandler.getAll(rc)
            .subscribe(result -> getFindAllResponse(rc, result),
                throwable -> handleRequestError(rc, throwable));
    }

    /**
     * Handle a custom FIND ALL request.
     *
     * @param rc the RoutingContext of the request
     * @param handler the handler of the get one request
     * @return a Disposable
     */
    public Disposable handleFindAllRequest(RoutingContext rc, Single<JsonArray> handler) {
        return handler
            .subscribe(result -> getFindAllResponse(rc, result),
                throwable -> handleRequestError(rc, throwable));
    }

    /**
     * Handle the POST ONE request of the validation handler.
     *
     * @param rc the RoutingContext of the request
     * @return a Disposable
     */
    public Disposable handleSaveOneRequest(RoutingContext rc) {
        return validationHandler.postOne(rc)
            .subscribe(result -> getSaveResponse(rc, result),
                throwable -> handleRequestError(rc, throwable));
    }

    /**
     * Handle the POST ALL request of the validation handler.
     *
     * @param rc the RoutingContext of the request
     * @return a Disposable
     */
    public Disposable handleSaveAllRequest(RoutingContext rc) {
        return validationHandler.postAll(rc)
            .subscribe(() -> getSaveAllUpdateDeleteResponse(rc),
                throwable -> handleRequestError(rc, throwable));
    }

    /**
     * Handle the UPDATE ONE request of the validation handler.
     *
     * @param rc the RoutingContext of the request
     * @return a Disposable
     */
    public Disposable handleUpdateRequest(RoutingContext rc) {
        return validationHandler.updateOne(rc)
            .subscribe(() -> getSaveAllUpdateDeleteResponse(rc),
                throwable -> handleRequestError(rc, throwable));
    }

    /**
     * Handle the DELETE ONE request of the validation handler.
     *
     * @param rc the RoutingContext of the request
     * @return a Disposable
     */
    public Disposable handleDeleteRequest(RoutingContext rc) {
        return validationHandler.deleteOne(rc)
            .subscribe(() -> getSaveAllUpdateDeleteResponse(rc),
                throwable -> handleRequestError(rc, throwable));
    }

    /**
     * Set the statuscode and content of the response for a successful FIND ONE request.
     *
     * @param rc the RoutingContext of the request
     * @param result the content of the response
     */
    private static void getFindOneResponse(RoutingContext rc, JsonObject result) {
        rc.response().setStatusCode(200).end(result.encodePrettily());
    }

    /**
     * Set the statuscode and content of the response for a successful FIND ALL request.
     *
     * @param rc the RoutingContext of the request
     * @param result the content of the response
     */
    private static void getFindAllResponse(RoutingContext rc, JsonArray result) {
        rc.response().setStatusCode(200).end(result.encodePrettily());
    }

    /**
     * Set the statuscode and content of the response for a successful SAVE ONE request.
     *
     * @param rc the RoutingContext of the request
     * @param result the content of the response
     */
    private static void getSaveResponse(RoutingContext rc, JsonObject result) {
        rc.response().setStatusCode(201).end(result.encodePrettily());
    }

    /**
     * Set the statuscode and content of the response for a successful SAVE ALL, UPDATE ONE or
     * DELETE ONE request.
     *
     * @param rc the RoutingContext of the request
     */
    private static void getSaveAllUpdateDeleteResponse(RoutingContext rc) {
        rc.response().setStatusCode(204).end();
    }

    /**
     * Set the status code based on the type of the throwable.
     *
     * @param rc the RoutingContext of the request
     * @param throwable the caught throwable
     */
    private static void handleRequestError(RoutingContext rc, Throwable throwable) {
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
