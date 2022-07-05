package at.uibk.dps.rm.handler.Resource;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RoutingContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ResourceTypeErrorHandler {

    public static void validatePostPatchRequest(RoutingContext rc) {
        List<Completable> completables = new ArrayList<>();
        try {
            JsonObject entity = rc.body().asJsonObject();
            long acceptedFields = entity.fieldNames()
                .stream()
                .takeWhile(field -> {
                    if ("resource_type".equals(field)) {
                        completables.add(checkResourceType(entity.getString(field)));
                        return true;
                    }
                    return false;
                })
                .count();

            // TODO: change with more than one accepted fields
            if (acceptedFields <= 0 || acceptedFields != entity.fieldNames().size()) {
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

    public static Completable checkResourceType(String value) {
        return Maybe.just(value.length() > 8 || value.length() <= 0)
            .mapOptional(result -> {
                if (result) {
                    throw new Throwable("resource type invalid");
                }
                return Optional.empty();
            }).ignoreElement();
    }
}
