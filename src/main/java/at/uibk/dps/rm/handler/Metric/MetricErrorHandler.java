package at.uibk.dps.rm.handler.Metric;

import at.uibk.dps.rm.util.FieldCheckUtil;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RoutingContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MetricErrorHandler {
    public static void validatePostPatchRequest(RoutingContext rc, HttpMethod httpMethod) {
        List<Completable> completables = new ArrayList<>();
        try {
            JsonObject entity = rc.body().asJsonObject();
            long acceptedFields = entity.fieldNames()
                .stream()
                .takeWhile(field -> {
                    switch (field) {
                        case "metric":
                            completables.add(checkMetric(entity.getString(field)));
                            return true;
                        case "description":
                            completables.add(checkDescription(entity.getString(field)));
                            return true;
                        default:
                            return false;
                    }
                })
                .count();
            FieldCheckUtil fieldCheckUtil = new FieldCheckUtil(2, 0);
            if (fieldCheckUtil.checkAcceptedFields(httpMethod, acceptedFields, entity.fieldNames().size())) {
                rc.fail(400);
                return;
            }


        } catch (Exception e) {
            rc.fail(400);
            return;
        }

        Completable.merge(completables)
            .subscribe(rc::next, throwable -> rc.fail(400, throwable))
            .dispose();
    }

    private static Completable checkMetric(String value) {
        return Maybe.just(value.length() > 256 || value.length() <= 0)
            .mapOptional(result -> {
                if (result) {
                    throw new Throwable("metric invalid");
                }
                return Optional.empty();
            })
            .ignoreElement();
    }

    private static Completable checkDescription(String value) {
        return Maybe.just(value.length() > 512 || value.length() <= 0)
            .mapOptional(result -> {
                if (result) {
                    throw new Throwable("description invalid");
                }
                return Optional.empty();
            })
            .ignoreElement();
    }
}
