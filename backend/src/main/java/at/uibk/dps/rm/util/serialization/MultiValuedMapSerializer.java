package at.uibk.dps.rm.util.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.apache.commons.collections4.MultiValuedMap;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

/**
 * This class can be used to serialize MultiValuedMap objects where the key is a string.
 *
 * @author matthi-g
 */
public class MultiValuedMapSerializer<V> extends StdSerializer<MultiValuedMap<String, V>> {

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
     * @param multiValuedMapClass the class of the multivalued map
     */
    public MultiValuedMapSerializer(Class<MultiValuedMap<String, V>> multiValuedMapClass) {
        super(multiValuedMapClass);
    }


    @Override
    public void serialize(MultiValuedMap<String, V> value, JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
        gen.writeStartObject();

        for (Map.Entry<String, Collection<V>> entry : value.asMap().entrySet()) {
            gen.writeFieldName(entry.getKey());
            gen.writeObject(entry.getValue());
        }

        gen.writeEndObject();
    }
}
