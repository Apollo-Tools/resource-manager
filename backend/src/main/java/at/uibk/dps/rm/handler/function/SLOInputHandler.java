package at.uibk.dps.rm.handler.function;

import at.uibk.dps.rm.entity.dto.GetResourcesBySLOsRequest;
import at.uibk.dps.rm.entity.dto.slo.ExpressionType;
import at.uibk.dps.rm.entity.dto.slo.ServiceLevelObjective;
import at.uibk.dps.rm.util.ArrayValidator;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RoutingContext;

import java.util.*;

public class SLOInputHandler {

    public static void validateGetResourcesBySLOsRequest(RoutingContext rc) {
        JsonObject body = rc.body().asJsonObject();
        checkExpressionAreValid(body.getJsonArray("slo"))
            .concatWith(Completable.defer(() -> checkArrayDuplicates(body)))
            .subscribe(rc::next, throwable -> rc.fail(400, throwable))
            .dispose();
    }

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

    private static Completable checkArrayDuplicates(JsonObject body) {
        GetResourcesBySLOsRequest request = body.mapTo(GetResourcesBySLOsRequest.class);
        ArrayValidator<ServiceLevelObjective> validator = new ArrayValidator<>();
        return validator.hasDuplicates(request.getServiceLevelObjectives());
    }
}
