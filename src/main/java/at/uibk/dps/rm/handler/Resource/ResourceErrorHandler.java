package at.uibk.dps.rm.handler.Resource;

import at.uibk.dps.rm.util.FieldCheckUtil;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RoutingContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class ResourceErrorHandler {

    public static void validatePostPatchRequest(RoutingContext rc, HttpMethod httpMethod) {
        List<Completable> completables = new ArrayList<>();
        try {
            JsonObject entity = rc.body().asJsonObject();
            long acceptedFields = entity.fieldNames()
                .stream()
                .takeWhile(field -> {
                    switch (field) {
                        case "url":
                            completables.add(checkUrl(entity.getString(field)));
                            return true;
                        case "resource_type":
                            completables.add(checkResourceType(entity.getJsonObject(field)));
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

    public static void validateAddMetricsRequest(RoutingContext rc) {
        try {
            JsonArray body = rc.body().asJsonArray();
        } catch (Exception e) {
            rc.fail(400);
            return;
        }
        rc.next();
    }

    private static Completable checkUrl(String value) {
        return Maybe.just(value.length() > 512 || value.length() <= 0 || !isValidURL(value))
            .mapOptional(result -> {
                if (result) {
                    throw new Throwable("url invalid");
                }
                return Optional.empty();
            })
            .ignoreElement();
    }

    private static boolean isValidURL(String url) {
        //Source: https://www.techiedelight.com/validate-url-java/, OWASP Validation Regex
        String urlRegex = "^((https?://)"
            + "(%[0-9A-Fa-f]{2}|[-()_.!~*';/?:@&=+$,A-Za-z0-9])+)"
            + "([).!';/?:,][[:blank:]])?$";
        Pattern urlPattern = Pattern.compile(urlRegex);
        return urlPattern.matcher(url).matches();
    }

    private static Completable checkResourceType(JsonObject value) {
        return Maybe.just(value.getLong("type_id") == null || value.fieldNames().size() != 1)
            .mapOptional(result -> {
                if (result) {
                    throw new Throwable("bad input");
                }
                return Optional.empty();
            })
            .ignoreElement();
    }
}
