package at.uibk.dps.rm.util.serialization;

import at.uibk.dps.rm.entity.dto.slo.SLOValue;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

/**
 * This class can be used to serialize SLOValue instances.
 *
 * @author matthi-g
 */
public class SLOValueSerializer extends StdSerializer<SLOValue> {

    private static final long serialVersionUID = 190033535439232237L;

    /**
     * The serialization throws an error if this constructor is not present
     */
    @SuppressWarnings("unused")
    public SLOValueSerializer() {
        this(null);
    }

    /**
     * Create an instance from the sloValueClass.
     *
     * @param sloValueClass the class of the SLOValue
     */
    public SLOValueSerializer(Class<SLOValue> sloValueClass) {
        super(sloValueClass);
    }

    @Override
    public void serialize(SLOValue value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        switch (value.getSloValueType()) {
            case NUMBER:
                gen.writeNumber(value.getValueNumber().doubleValue());
                break;
            case STRING:
                gen.writeString(value.getValueString());
                break;
            case BOOLEAN:
                gen.writeBoolean(value.getValueBool());
                break;
        }
    }
}
