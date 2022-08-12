package at.uibk.dps.rm.util;

import at.uibk.dps.rm.dto.slo.*;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class ServiceLevelObjectiveDeserializer extends StdDeserializer<ServiceLevelObjective> {

    // Throws an error if this constructor is not present
    public ServiceLevelObjectiveDeserializer() {
        this(null);
    }

    public ServiceLevelObjectiveDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public ServiceLevelObjective deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        JsonNode node = jp.getCodec().readTree(jp);
        String name =  node.get("name").asText();
        ExpressionType expressionType = ExpressionType.fromString(node.get("expression").asText());
        List<JsonNode> values = StreamSupport.stream(node.get("value").spliterator(), false)
                .collect(Collectors.toList());
        PropertyType propertyType = PropertyType.valueOf(node.get("type").asText().toUpperCase());
        List<SLOValue> sloValues = new ArrayList<>();
        for (JsonNode value : values) {
            sloValues.add(deserializeSLOValue(value));
        }

        return new ServiceLevelObjective(name, expressionType, sloValues, propertyType);
    }

    private SLOValue deserializeSLOValue(JsonNode value) {
        SLOValue sloValue = new SLOValue();
        if (value.isNumber()) {
            sloValue.setSloValueType(SLOValueType.NUMBER);
            sloValue.setNumberValue(value.numberValue());
        } else if (value.isTextual()) {
            sloValue.setSloValueType(SLOValueType.STRING);
            sloValue.setStringValue(value.textValue());
        } else {
            sloValue.setSloValueType(SLOValueType.BOOLEAN);
            sloValue.setBooleanValue(value.booleanValue());
        }
        return sloValue;
    }
}
