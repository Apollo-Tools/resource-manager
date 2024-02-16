package at.uibk.dps.rm.util.misc;

import io.vertx.core.json.JsonArray;
import io.vertx.rxjava3.core.MultiMap;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Implements tests for the {@link MultiMapUtility} class.
 *
 * @author matthi-g
 */
public class MultiMapUtilityTest {

    @Test
    void serializeMultiMap() {
        MultiMap multiMap = MultiMap.caseInsensitiveMultiMap();
        multiMap.add("key1", "value1");
        multiMap.add("key1", "value2");
        multiMap.add("key2", "value3");
        multiMap.add("key3", "value4");

        Map<String, JsonArray> result = MultiMapUtility.serializeMultimap(multiMap);

        assertThat(result.get("key1")).isEqualTo(new JsonArray(List.of("\"value1\"", "\"value2\"")));
        assertThat(result.get("key2")).isEqualTo(new JsonArray(List.of("\"value3\"")));
        assertThat(result.get("key3")).isEqualTo(new JsonArray(List.of("\"value4\"")));
        assertThat(result.get("key4")).isEqualTo(null);
    }

    @Test
    void deserializeMultimap() {
        Map<String, JsonArray> map = new HashMap<>();
        map.put("key1", new JsonArray(List.of("\"value1\"", "\"value2\"")));
        map.put("key2", new JsonArray(List.of("\"value3\"")));
        map.put("key3", new JsonArray(List.of("\"value4\"")));

        MultiMap result = MultiMapUtility.deserializeMultimap(map);

        assertThat(result.getAll("key1")).isEqualTo(List.of("value1", "value2"));
        assertThat(result.getAll("key2")).isEqualTo(List.of("value3"));
        assertThat(result.getAll("key3")).isEqualTo(List.of("value4"));
    }
}
