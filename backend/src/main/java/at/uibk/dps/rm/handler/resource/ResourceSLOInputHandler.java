package at.uibk.dps.rm.handler.resource;

import at.uibk.dps.rm.entity.dto.ListResourcesBySLOsRequest;
import at.uibk.dps.rm.util.validation.CollectionValidator;
import at.uibk.dps.rm.util.validation.ExpressionValidator;
import io.reactivex.rxjava3.core.Completable;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RoutingContext;
import lombok.experimental.UtilityClass;

/**
 * Used to validate the inputs of requests containing service level objectives and fails the
 * context if violations are found.
 *
 * @author matthi-g
 */
@UtilityClass
public class ResourceSLOInputHandler {

    /**
     * Validate the contents of a getResourcesBySLO request for its correctness.
     *
     * @param rc the routing context
     */
    public static void validateGetResourcesBySLOsRequest(RoutingContext rc) {
        JsonObject body = rc.body().asJsonObject();
        ExpressionValidator.checkExpressionAreValid(body.getJsonArray("slos"))
            .concatWith(Completable.defer(() -> checkArrayDuplicates(body)))
            .subscribe(rc::next, throwable -> rc.fail(400, throwable))
            .dispose();
    }

    /**
     * Check the service level objectives for duplicates.
     *
     * @param body the request body that contains the service level objectives
     * @return a Completable
     */
    private static Completable checkArrayDuplicates(JsonObject body) {
        ListResourcesBySLOsRequest request = body.mapTo(ListResourcesBySLOsRequest.class);
        return CollectionValidator.hasDuplicates(request.getServiceLevelObjectives());
    }
}
