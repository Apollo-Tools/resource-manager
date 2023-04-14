package at.uibk.dps.rm.handler.function;

import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RoutingContext;

public class FunctionInputHandler {
    public static void validateAddFunctionRequest(RoutingContext rc) {
        JsonObject requestBody = rc.body().asJsonObject();
        String functionName = requestBody.getString("name");
        if (functionName.matches("^[a-z0-9]+$")) {
            rc.next();
        } else {
            rc.fail(400, new Throwable("invalid function name"));
        }
    }
}
