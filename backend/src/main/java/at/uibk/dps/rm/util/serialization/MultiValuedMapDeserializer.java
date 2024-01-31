package at.uibk.dps.rm.util.serialization;

import at.uibk.dps.rm.entity.monitoring.kubernetes.K8sPod;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import java.io.IOException;
import java.util.*;

public class MultiValuedMapDeserializer extends StdDeserializer<MultiValuedMap<String, K8sPod>> {

    private static final long serialVersionUID = 2489015244524901705L;

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
    public MultiValuedMapDeserializer(Class<?> valueClass) {
        super(valueClass);
    }

    @Override
    public MultiValuedMap<String, K8sPod> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        MultiValuedMap<String, K8sPod> map = new ArrayListValuedHashMap<>();
        JsonNode node = p.readValueAsTree();

        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            String key = entry.getKey();
            JsonNode valueNode = entry.getValue();
            for (JsonNode element : valueNode) {
                K8sPod k8sPod;
                try (JsonParser subParser = element.traverse(p.getCodec())) {
                    k8sPod = subParser.readValueAs(K8sPod.class);
                }
                map.put(key, k8sPod);
            }
        }

        return map;
    }
}