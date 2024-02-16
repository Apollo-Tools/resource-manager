package at.uibk.dps.rm.util.misc;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.rxjava3.core.MultiMap;
import lombok.experimental.UtilityClass;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A utility class for the {@link MultiMap} class.
 *
 * @author matthi-g
 */
@UtilityClass
public class MultiMapUtility {

    /**
     * Transform a {@link MultiMap} to be transportable on the
     * {@link io.vertx.rxjava3.core.eventbus.EventBus}.
     *
     * @param multiMap the multimap
     * @return the transformed map
     */
    public static Map<String, JsonArray> serializeMultimap(MultiMap multiMap) {
        Map<String, JsonArray> serializedMap = new HashMap<>();
        multiMap.forEach((key, value) -> {
            JsonArray values = serializedMap.get(key);
            if (values == null) {
                values = new JsonArray();
                values.add(Json.encode(value));
                serializedMap.put(key, values);
            } else {
                values.add(Json.encode(value));
            }
        });
        return serializedMap;
    }

    /**
     * Create a multimap from a map that has been sent over the
     * @link io.vertx.rxjava3.core.eventbus.EventBus}.
     *
     * @param serializedMap the map
     * @return the composed multimap
     */
    public static MultiMap deserializeMultimap(Map<String, JsonArray> serializedMap) {
        MultiMap multiMap = MultiMap.caseInsensitiveMultiMap();
        serializedMap.forEach((key, values) -> multiMap.add(key,
            (List<String>) values.stream().map(value -> (String) Json.decodeValue((String) value))
                .collect(Collectors.toList())));
        return multiMap;
    }
}
