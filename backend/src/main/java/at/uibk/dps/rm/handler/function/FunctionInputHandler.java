package at.uibk.dps.rm.handler.function;

import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RoutingContext;
import lombok.experimental.UtilityClass;

/**
 * Used to validate the inputs of the function endpoint and fails the context if violations are found.
 *
 * @author matthi-g
 */
@UtilityClass
public class FunctionInputHandler {

    /**
     * Validate the name of a function that should be created.
     *
     * @param rc the routing context
     */
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
