package at.uibk.dps.rm.util.serialization;

import at.uibk.dps.rm.util.misc.MetricPair;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

/**
 * This class can be used to serialize MetricPair objects.
 *
 * @author matthi-g
 */
public class MetricPairSerializer extends StdSerializer<MetricPair> {

    private static final long serialVersionUID = 190033535439232237L;

    /**
     * The serialization throws an error if this constructor is not present
     */
    @SuppressWarnings("unused")
    public MetricPairSerializer() {
        this(null);
    }

    /**
     * Create an instance from the pairValueClass.
     *
     * @param pairValueClass the class of the SLOValue
     */
    public MetricPairSerializer(Class<MetricPair> pairValueClass) {
        super(pairValueClass);
    }

    @Override
    public void serialize(MetricPair value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartArray();
        gen.writeNumber(value.getKey());
        gen.writeString(value.getValue().toString());
        gen.writeEndArray();
    }
}
