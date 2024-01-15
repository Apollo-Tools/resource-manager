package at.uibk.dps.rm.util.serialization;

import at.uibk.dps.rm.entity.monitoring.victoriametrics.VmResult;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.util.*;

/**
 * This class can be used to deserialize the {@link VmResult}.
 *
 * @author matthi-g
 */
public class VmResultDeserializer extends StdDeserializer<VmResult> {

    private static final long serialVersionUID = 2489015244524901705L;

    /**
     * The serialization throws an error if this constructor is not present
     */
    @SuppressWarnings("unused")
    public VmResultDeserializer() {
        this(null);
    }

    /**
     * Create an instance from the valueClass.
     *
     * @param valueClass the value class
     */
    public VmResultDeserializer(Class<?> valueClass) {
        super(valueClass);
    }

    @Override
    public VmResult deserialize(JsonParser jsonParser, DeserializationContext ctxt)
            throws IOException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        return parse(node);
    }

    /**
     * Parse a quantity value
     *
     * @param value the quantity value
     * @return the parse Quantity
     */
    protected VmResult parse(JsonNode value) {
        Map<String, String> metricMap = parseMetricMap(value);
        List<Pair<Long, Double>> valueList = new ArrayList<>();
        if (value.has("value")) {
            Iterator<JsonNode> iterator = value.get("value").elements();
            parseTimeValuePair(iterator, valueList);
        } else if (value.has("values")) {
            value.get("values").elements().forEachRemaining(entry -> {
                Iterator<JsonNode> valueIterator = entry.elements();
                parseTimeValuePair(valueIterator, valueList);
            });
        }
        return new VmResult(metricMap, valueList);
    }

    private Map<String, String> parseMetricMap(JsonNode value) {
        Iterator<Map.Entry<String, JsonNode>> fields = value.get("metric").fields();
        Map<String, String> metricMap = new HashMap<>();

        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            String entryKey = entry.getKey();
            String entryValue = entry.getValue().textValue();
            metricMap.put(entryKey, entryValue);
        }
        return metricMap;
    }

    private void parseTimeValuePair(Iterator<JsonNode> iterator, List<Pair<Long, Double>> valueList) {
        long timestamp = iterator.next().asLong();
        double metricValue = Double.parseDouble(iterator.next().textValue());
        Pair<Long, Double> valuePair = Pair.of(timestamp, metricValue);
        valueList.add(valuePair);
    }
}
