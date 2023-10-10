package at.uibk.dps.rm.util.serialization;

import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.custom.QuantityFormatException;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Implements tests for the {@link QuantityDeserializer} class.
 *
 * @author matthi-g
 */
public class QuantityDeserializerTest {

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
    }

    @Test
    void deserialize() {
        JsonObject quantity = JsonObject.mapFrom(new Quantity(BigDecimal.valueOf(1.0), Quantity.Format.BINARY_SI));

        Quantity result = quantity.mapTo(Quantity.class);

        assertThat(result.getNumber().compareTo(BigDecimal.valueOf(1.0))).isEqualTo(0);
        assertThat(result.getFormat()).isEqualTo(Quantity.Format.BINARY_SI);
    }

    @Test
    void deserializeEmptyObject() {
        JsonObject quantity = new JsonObject("{}");

        assertThrows(QuantityFormatException.class, () -> quantity.mapTo(Quantity.class));
    }

    @Test
    void parseNullNode() {
        assertThrows(QuantityFormatException.class, () -> new QuantityDeserializer().parse(null));
    }

}
