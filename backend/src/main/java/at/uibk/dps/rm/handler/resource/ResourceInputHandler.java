package at.uibk.dps.rm.handler.resource;

import at.uibk.dps.rm.util.JsonArrayValidator;
import io.vertx.core.json.JsonArray;
import io.vertx.rxjava3.ext.web.RoutingContext;

public class ResourceInputHandler {
    public static void validateAddMetricsRequest(RoutingContext rc) {
        JsonArray requestBody = rc.body().asJsonArray();
        // TODO: fix naming (metric_id)
        JsonArrayValidator.checkJsonArrayDuplicates(requestBody, "metricId")
            .subscribe(rc::next, throwable -> rc.fail(400, throwable))
            .dispose();
    }
}
