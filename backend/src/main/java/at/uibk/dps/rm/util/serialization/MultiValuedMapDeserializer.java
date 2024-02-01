package at.uibk.dps.rm.util.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import java.io.IOException;
import java.util.*;

public class MultiValuedMapDeserializer<V> extends StdDeserializer<MultiValuedMap<String, V>> {

    private static final long serialVersionUID = 2489015244524901705L;

    private final Class<V> valueClass; // Add this field

    /**
     * The serialization throws an error if this constructor is not present
     */
    @SuppressWarnings("unused")
    public MultiValuedMapDeserializer() {
        this(null);
    }

    /**
     * Create an instance from the valueClass.
     *
     * @param valueClass the value class
     */
    public MultiValuedMapDeserializer(Class<V> valueClass) {
        super(valueClass);
        this.valueClass = valueClass;
    }

    @Override
    public MultiValuedMap<String, V> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        MultiValuedMap<String, V> map = new ArrayListValuedHashMap<>();
        JsonNode node = p.readValueAsTree();

        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            String key = entry.getKey();
            JsonNode valueNode = entry.getValue();
            for (JsonNode element : valueNode) {
                if (!element.isNull()) {
                    V value;
                    try (JsonParser subParser = element.traverse(p.getCodec())) {
                        value = subParser.readValueAs(valueClass);
                    }
                    map.put(key, value);
                }
            }
        }
        return map;
    }
}
