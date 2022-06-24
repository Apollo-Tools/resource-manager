package at.uibk.dps.rm.handler;

import io.vertx.core.AsyncResult;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RoutingContext;

public class ResultHandler {

    public static void handleGetOneRequest(RoutingContext rc, AsyncResult<JsonObject> result) {
        if (result.succeeded()) {
            if (result.result() != null) {
                rc.response()
                    .setStatusCode(200)
                    .end(result.result().encodePrettily());
            } else {
                rc.fail(404, new Throwable("not found"));
            }
        } else {
            rc.fail(500, result.cause());
        }
    }
}
