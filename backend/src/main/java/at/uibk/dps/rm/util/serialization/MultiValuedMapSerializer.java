package at.uibk.dps.rm.util.serialization;

import at.uibk.dps.rm.entity.monitoring.kubernetes.K8sPod;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.apache.commons.collections4.MultiValuedMap;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

public class MultiValuedMapSerializer extends StdSerializer<MultiValuedMap<String, K8sPod>> {

    private static final long serialVersionUID = 190033535439232237L;

    /**
     * The serialization throws an error if this constructor is not present
     */
    @SuppressWarnings("unused")
    public MultiValuedMapSerializer() {
        super(MultiValuedMap.class, true);
    }

    /**
     * Create an instance from the multiValuedMapClass.
     *
     * @param multiValuedMapClass the class of the SLOValue
     */
    public MultiValuedMapSerializer(Class<MultiValuedMap<String, K8sPod>> multiValuedMapClass) {
        super(multiValuedMapClass);
    }


    @Override
    public void serialize(MultiValuedMap<String, K8sPod> value, JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
        gen.writeStartObject();

        for (Map.Entry<String, Collection<K8sPod>> entry : value.asMap().entrySet()) {
            gen.writeFieldName(entry.getKey());
            gen.writeObject(entry.getValue());
        }

        gen.writeEndObject();
    }
}