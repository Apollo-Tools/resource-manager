package at.uibk.dps.rm.handler;

import at.uibk.dps.rm.exception.AlreadyExistsException;
import at.uibk.dps.rm.exception.BadInputException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.exception.UsedByOtherEntityException;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RoutingContext;

public class ResultHandler {

    public static Disposable handleGetOneRequest(RoutingContext rc, Single<JsonObject> handler) {
        return handler
            .subscribe(result -> getFindOneResponse(rc, result),
                throwable -> handleRequestError(rc, throwable));
    }

    public static Disposable handleGetAllRequest(RoutingContext rc, Single<JsonArray> handler) {
        return handler
            .subscribe(result -> getFindAllResponse(rc, result),
                throwable -> handleRequestError(rc, throwable));
    }

    public static Disposable handleSaveOneRequest(RoutingContext rc, Single<JsonObject> handler) {
        return handler
            .subscribe(result -> getSaveResponse(rc, result),
                throwable -> handleRequestError(rc, throwable));
    }

    public static Disposable handleSaveAllUpdateDeleteRequest(RoutingContext rc, Completable handler) {
        return handler
            .subscribe(() -> getSaveAllUpdateDeleteResponse(rc),
                throwable -> handleRequestError(rc, throwable));
    }

    private static void getFindOneResponse(RoutingContext rc, JsonObject result) {
        rc.response().setStatusCode(200).end(result.encodePrettily());
    }

    private static void getFindAllResponse(RoutingContext rc, JsonArray result) {
        rc.response().setStatusCode(200).end(result.encodePrettily());
    }

    private static void getSaveResponse(RoutingContext rc, JsonObject result) {
        rc.response().setStatusCode(201).end(result.encodePrettily());
    }

    private static void getSaveAllUpdateDeleteResponse(RoutingContext rc) {
        rc.response().setStatusCode(204).end();
    }

    private static void handleRequestError(RoutingContext rc, Throwable throwable) {
        int statusCode = 500;
        if (throwable instanceof NotFoundException) {
            statusCode = 404;
        }  else if (throwable instanceof AlreadyExistsException ||
            throwable instanceof UsedByOtherEntityException) {
            statusCode = 409;
        } else if (throwable instanceof BadInputException) {
            statusCode = 400;
        }
        rc.fail(statusCode, throwable);
    }
}
