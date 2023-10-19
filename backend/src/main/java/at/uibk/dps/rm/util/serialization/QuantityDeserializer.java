package at.uibk.dps.rm.util.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.custom.QuantityFormatException;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * This class can be used to deserialize the {@link Quantity}.
 *
 * @author matthi-g
 */
public class QuantityDeserializer extends StdDeserializer<Quantity> {

    private static final long serialVersionUID = 2489015244524901705L;

    /**
     * The serialization throws an error if this constructor is not present
     */
    @SuppressWarnings("unused")
    public QuantityDeserializer() {
        this(null);
    }

    /**
     * Create an instance from the valueClass.
     *
     * @param valueClass the value class
     */
    public QuantityDeserializer(Class<?> valueClass) {
        super(valueClass);
    }

    @Override
    public Quantity deserialize(JsonParser jsonParser, DeserializationContext ctxt)
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
    protected Quantity parse(JsonNode value) {
        if (value == null || value.isEmpty()) {
            throw new QuantityFormatException("");
        }
        BigDecimal numericValue = new BigDecimal(value.get("number").asText());
        Quantity.Format format = Quantity.Format.valueOf(value.get("format").asText());
        return new Quantity(numericValue, format);
    }
}
