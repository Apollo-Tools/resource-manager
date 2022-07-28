package at.uibk.dps.rm.handler.resource;

import at.uibk.dps.rm.slo.EvaluationType;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RoutingContext;

import java.util.Optional;

public class ResourceInputHandler {
    public static void validateAddMetricsRequest(RoutingContext rc) {
        JsonArray requestBody = rc.body().asJsonArray();
        checkJsonArrayDuplicates(requestBody, "metricId")
            .subscribe(rc::next, throwable -> rc.fail(400, throwable))
            .dispose();
    }

    public static void validateGetResourcesBySLOsRequest(RoutingContext rc) {
        JsonObject requestBody = rc.body().asJsonObject();
        JsonArray serviceLevelObjectives = requestBody.getJsonArray("slo");
        JsonArray sortSlos = requestBody.getJsonArray("sort");
        checkJsonArrayDuplicates(serviceLevelObjectives, "metric")
            .andThen(checkEvaluationTypeIsValid(serviceLevelObjectives))
            .andThen(checkJsonArrayDuplicates(sortSlos))
            .andThen(checkSortingArrayContainsPresentMetrics(serviceLevelObjectives, sortSlos))
            .subscribe(rc::next, throwable -> rc.fail(400, throwable))
            .dispose();
    }

    private static Completable checkJsonArrayDuplicates(JsonArray slos, String param) {
        return Maybe.just(slos)
            .mapOptional(items -> {
                for (int i = 0; i < items.size() - 1; i++) {
                    for (int j = i + 1; j < items.size(); j++) {
                        if (compareItems(items, i, j, param)) {
                            throw new Throwable("duplicated input");
                        }
                    }
                }
                return Optional.empty();
            })
            .ignoreElement();
    }

    private static Completable checkJsonArrayDuplicates(JsonArray slos) {
        return Maybe.just(slos)
            .mapOptional(items -> {
                for (int i = 0; i < items.size() - 1; i++) {
                    for (int j = i + 1; j < items.size(); j++) {
                        if (compareItems(items, i, j)) {
                            throw new Throwable("duplicated input");
                        }
                    }
                }
                return Optional.empty();
            })
            .ignoreElement();
    }

    private static Boolean compareItems(JsonArray items, int i, int j, String param) {
        return items.getJsonObject(i).getValue(param)
            .equals(items.getJsonObject(j).getValue(param));
    }

    private static Boolean compareItems(JsonArray items, int i, int j) {
        return items.getValue(i)
            .equals(items.getValue(j));
    }

    private static Completable checkEvaluationTypeIsValid(JsonArray slos) {
        return Maybe.just(slos)
            .mapOptional(items -> {
                for (int i = 0; i < items.size(); i++) {
                    if (!EvaluationType.symbolExists(items.getJsonObject(i).getString("evaluationType"))) {
                        throw new Throwable("evaluationType is not supported");
                    }
                }
                return Optional.empty();
            })
            .ignoreElement();
    }

    private static Completable checkSortingArrayContainsPresentMetrics(JsonArray slos, JsonArray sortArray) {
        return Maybe.just(sortArray)
            .mapOptional(items -> {
                for (int i = 0; i < items.size(); i++) {
                    boolean notFound = true;
                    for (int j = 0; j < slos.size(); j++) {
                        if (slos.getJsonObject(j).getValue("metric").equals(items.getValue(i))) {
                            notFound = false;
                            break;
                        }
                    }
                    if (notFound) {
                        throw new Throwable("sorting metric is not present");
                    }
                }
                return Optional.empty();
            })
            .ignoreElement();
    }
}
