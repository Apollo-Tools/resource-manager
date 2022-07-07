package at.uibk.dps.rm.handler;

import io.vertx.core.AsyncResult;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RoutingContext;

public class ErrorHandler {

    public static void handleFindOne(RoutingContext rc, AsyncResult<JsonObject> result) {
        if (!rc.failed()) {
            if (result.failed()) {
                rc.fail(500, new Throwable(result.cause()));
            }
            else if (result.result() == null) {
                rc.fail(404, new Throwable("not found"));
            }
        }
    }

    public static void handleExistsOne(RoutingContext rc, AsyncResult<Boolean> result) {
        if (!rc.failed()) {
            if (result.failed() && !rc.failed()) {
                rc.fail(500, new Throwable(result.cause()));
            }
            else if (!result.result() && !rc.failed()) {
                rc.fail(404, new Throwable("not found"));
            }
        }
    }

    public static void handleDuplicates(RoutingContext rc, AsyncResult<Boolean> result) {
        if (!rc.failed()) {
            if (result.failed()){
                rc.fail(500, result.cause());
            }
            else if (result.result()) {
                rc.fail(409, new Throwable("already exists"));
            }
        }
    }
}
