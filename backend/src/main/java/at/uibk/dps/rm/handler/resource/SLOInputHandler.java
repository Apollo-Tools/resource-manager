package at.uibk.dps.rm.handler.resource;

import at.uibk.dps.rm.entity.dto.ListResourcesBySLOsRequest;
import at.uibk.dps.rm.entity.dto.slo.ExpressionType;
import at.uibk.dps.rm.util.validation.CollectionValidator;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RoutingContext;

import java.util.*;

/**
 * Used to validate the inputs of requests containing service level objectives and fails the
 * context if violations are found.
 *
 * @author matthi-g
 */
public class SLOInputHandler {

    /**
     * Validate the contents of a getResourcesBySLO request for its correctness.
     *
     * @param rc the routing context
     */
    public static void validateGetResourcesBySLOsRequest(RoutingContext rc) {
        JsonObject body = rc.body().asJsonObject();
        checkExpressionAreValid(body.getJsonArray("slos"))
            .concatWith(Completable.defer(() -> checkArrayDuplicates(body)))
            .subscribe(rc::next, throwable -> rc.fail(400, throwable))
            .dispose();
    }

    /**
     * Check whether the expressions of the slos are valid.
     *
     * @param slos the service level objectives
     * @return a Completable
     */
    private static Completable checkExpressionAreValid(JsonArray slos) {
        if (slos == null) {
            return Completable.complete();
        }
        return Maybe.just(slos)
            .mapOptional(items -> {
                for (int i = 0; i < items.size(); i++) {
                    if (!ExpressionType.symbolExists(items.getJsonObject(i).getString("expression"))) {
                        throw new Throwable("expression is not supported");
                    }
                }
                return Optional.empty();
            })
            .ignoreElement();
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
