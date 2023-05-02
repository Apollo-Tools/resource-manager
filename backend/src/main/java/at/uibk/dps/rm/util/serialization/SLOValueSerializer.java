package at.uibk.dps.rm.util.serialization;

import at.uibk.dps.rm.entity.dto.slo.SLOValue;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class SLOValueSerializer extends StdSerializer<SLOValue> {

    private static final long serialVersionUID = 190033535439232237L;

    @SuppressWarnings("unused")
    public SLOValueSerializer() {
        this(null);
    }

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
