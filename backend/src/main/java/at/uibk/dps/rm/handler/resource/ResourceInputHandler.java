package at.uibk.dps.rm.handler.resource;

import at.uibk.dps.rm.util.validation.JsonArrayValidator;
import io.vertx.core.json.JsonArray;
import io.vertx.rxjava3.ext.web.RoutingContext;

/**
 * Used to validate the inputs of the resource endpoint and fails the
 * context if violations are found.
 *
 * @author matthi-g
 */
public class ResourceInputHandler {

    /**
     * Validate if a add metrics request contains duplicated metrics.
     *
     * @param rc the routing context
     */
    public static void validateAddMetricsRequest(RoutingContext rc) {
        JsonArray requestBody = rc.body().asJsonArray();
        // TODO: fix naming (metric_id)
        JsonArrayValidator.checkJsonArrayDuplicates(requestBody, "metricId")
            .subscribe(rc::next, throwable -> rc.fail(400, throwable))
            .dispose();
    }
}
