package at.uibk.dps.rm.handler.resource;

import at.uibk.dps.rm.entity.dto.slo.ExpressionType;
import at.uibk.dps.rm.util.JsonArrayValidator;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RoutingContext;

import java.util.Optional;

public class ResourceInputHandler {
    public static void validateAddMetricsRequest(RoutingContext rc) {
        JsonArray requestBody = rc.body().asJsonArray();
        // TODO: fix naming (metric_id)
        JsonArrayValidator.checkJsonArrayDuplicates(requestBody, "metricId")
            .subscribe(rc::next, throwable -> rc.fail(400, throwable))
            .dispose();
    }

    public static void validateGetResourcesBySLOsRequest(RoutingContext rc) {
        JsonObject requestBody = rc.body().asJsonObject();
        JsonArray serviceLevelObjectives = requestBody.getJsonArray("slo");
        JsonArrayValidator.checkJsonArrayDuplicates(serviceLevelObjectives, "name")
            .andThen(checkExpressionIsValid(serviceLevelObjectives))
            .subscribe(rc::next, throwable -> rc.fail(400, throwable))
            .dispose();
    }

    private static Completable checkExpressionIsValid(JsonArray slos) {
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
}
