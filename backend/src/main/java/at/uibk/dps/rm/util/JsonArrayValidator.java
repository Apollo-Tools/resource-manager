package at.uibk.dps.rm.util;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.vertx.core.json.JsonArray;

import java.util.Optional;

public class JsonArrayValidator {

    public static Completable checkJsonArrayDuplicates(JsonArray slos, String key) {
        return Maybe.just(slos)
            .mapOptional(items -> {
                for (int i = 0; i < items.size() - 1; i++) {
                    for (int j = i + 1; j < items.size(); j++) {
                        if (compareItems(items, i, j, key)) {
                            throw new Throwable("duplicated input");
                        }
                    }
                }
                return Optional.empty();
            })
            .ignoreElement();
    }

    private static Boolean compareItems(JsonArray items, int i, int j, String key) {
        return items.getJsonObject(i).getValue(key)
            .equals(items.getJsonObject(j).getValue(key));
    }
}
