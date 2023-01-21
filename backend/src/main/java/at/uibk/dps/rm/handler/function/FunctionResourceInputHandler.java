package at.uibk.dps.rm.handler.function;

import at.uibk.dps.rm.util.JsonArrayValidator;
import io.vertx.core.json.JsonArray;
import io.vertx.rxjava3.ext.web.RoutingContext;

public class FunctionResourceInputHandler {

    public static void validateAddFunctionResourceRequest(RoutingContext rc) {
        JsonArray requestBody = rc.body().asJsonArray();
        JsonArrayValidator.checkJsonArrayDuplicates(requestBody, "resource_id")
            .subscribe(rc::next, throwable -> rc.fail(400, throwable))
            .dispose();
    }
}
