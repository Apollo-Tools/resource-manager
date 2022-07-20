package at.uibk.dps.rm.handler;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RoutingContext;
public class ResultHandler {

    public static void handleGetOneRequest(RoutingContext rc, JsonObject result) {
        rc.response().setStatusCode(200).end(result.encodePrettily());
    }

    public static void handleGetAllRequest(RoutingContext rc, JsonArray result) {
        rc.response().setStatusCode(200).end(result.encodePrettily());
    }

    public static void handleSaveRequest(RoutingContext rc, JsonObject result) {
        rc.response().setStatusCode(201).end(result.encodePrettily());
    }

    public static void handleSaveAllUpdateDeleteRequest(RoutingContext rc) {
        rc.response().setStatusCode(204).end();
    }
}
