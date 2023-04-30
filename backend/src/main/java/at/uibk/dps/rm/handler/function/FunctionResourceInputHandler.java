package at.uibk.dps.rm.handler.function;

import at.uibk.dps.rm.util.validation.JsonArrayValidator;
import io.vertx.core.json.JsonArray;
import io.vertx.rxjava3.ext.web.RoutingContext;

/**
 * Used to validate the inputs of the function resource endpoint and fails the context if
 * violations are found.
 *
 * @author matthi-g
 */
public class FunctionResourceInputHandler {

    /**
     * Check if the array of function resources in the add function resource reuqest contains
     * duplicates.
     *
     * @param rc the routing context
     */
    public static void validateAddFunctionResourceRequest(RoutingContext rc) {
        JsonArray requestBody = rc.body().asJsonArray();
        JsonArrayValidator.checkJsonArrayDuplicates(requestBody, "resource_id")
            .subscribe(rc::next, throwable -> rc.fail(400, throwable))
            .dispose();
    }
}
