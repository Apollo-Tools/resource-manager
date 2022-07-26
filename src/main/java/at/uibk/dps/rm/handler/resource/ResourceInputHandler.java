package at.uibk.dps.rm.handler.resource;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.vertx.core.json.JsonArray;
import io.vertx.rxjava3.ext.web.RoutingContext;

import java.util.Optional;

public class ResourceInputHandler {
    public static void validateAddMetricsRequest(RoutingContext rc) {
        JsonArray requestBody = rc.body().asJsonArray();
        checkMetricsArrayDuplicates(requestBody)
            .subscribe(rc::next, throwable -> rc.fail(400, throwable))
            .dispose();
    }

    private static Completable checkMetricsArrayDuplicates(JsonArray requestBody) {
        return Maybe.just(requestBody)
            .mapOptional(items -> {
                for (int i = 0; i < items.size(); i++) {
                    for (int j = i + 1; j < items.size(); j++) {
                        if (compareMetricObjects(items, i, j)) {
                            throw new Throwable("duplicated input");
                        }
                    }
                }
                return Optional.empty();
            })
            .ignoreElement();
    }

    private static Boolean compareMetricObjects(JsonArray items, int i, int j) {
        return items.getJsonObject(i).getLong("metricId")
            .equals(items.getJsonObject(j).getLong("metricId"));
    }
}
