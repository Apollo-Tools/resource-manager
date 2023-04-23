package at.uibk.dps.rm.util;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.vertx.core.json.JsonArray;
import lombok.experimental.UtilityClass;

import java.util.Optional;

/**
 * A utility class that can be used to validate the items of a JsonArray.
 *
 * @author matthi-g
 */
@UtilityClass
public class JsonArrayValidator {

    /**
     * Check whether the jsonArray contains any duplicates. Duplicates are determined by comparing
     * the value of the key of each item.
     *
     * @param jsonArray the json array
     * @param key the item key for the comparison
     * @return a Completable
     */
    public static Completable checkJsonArrayDuplicates(JsonArray jsonArray, String key) {
        return Maybe.just(jsonArray)
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

    /**
     * Compare two items of the json array items at postion i and j by comparing their key.
     *
     * @param items the jsonarray that contains all items
     * @param i the position of the first item in the json array
     * @param j the position of the second item in the json array
     * @param key the item key for the comparison
     * @return true if the items a equal else false
     */
    @SuppressWarnings("PMD.ShortVariable")
    private static Boolean compareItems(JsonArray items, int i, int j, String key) {
        return items.getJsonObject(i).getValue(key)
            .equals(items.getJsonObject(j).getValue(key));
    }
}
