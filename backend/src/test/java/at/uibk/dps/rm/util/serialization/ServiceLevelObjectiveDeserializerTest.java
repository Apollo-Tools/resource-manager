package at.uibk.dps.rm.util.serialization;

import at.uibk.dps.rm.entity.dto.slo.ExpressionType;
import at.uibk.dps.rm.entity.dto.slo.ServiceLevelObjective;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Implements tests for the {@link ServiceLevelObjectiveDeserializer} class.
 *
 * @author matthi-g
 */
public class ServiceLevelObjectiveDeserializerTest {

    @Test
    void deserializeNumberSingle() {
        JsonObject json = new JsonObject("{\"name\": \"availability\", \"expression\": \">\", \"value\": [0.99]}");

        ServiceLevelObjective result = json.mapTo(ServiceLevelObjective.class);

        assertThat(result.getName()).isEqualTo("availability");
        assertThat(result.getExpression()).isEqualTo(ExpressionType.GT);
        assertThat(result.getValue().size()).isEqualTo(1);
        assertThat(result.getValue().get(0).getValueNumber()).isEqualTo(0.99);
    }

    @Test
    void deserializeNumberMultiple() {
        JsonObject json = new JsonObject("{\"name\": \"availability\", \"expression\": \">\", \"value\": [1, 2, 3]}");

        ServiceLevelObjective result = json.mapTo(ServiceLevelObjective.class);

        assertThat(result.getValue().size()).isEqualTo(3);
        assertThat(result.getValue().get(0).getValueNumber()).isEqualTo(1.0);
        assertThat(result.getValue().get(1).getValueNumber()).isEqualTo(2.0);
        assertThat(result.getValue().get(2).getValueNumber()).isEqualTo(3.0);
    }

    @Test
    void deserializeStringSingle() {
        JsonObject json = new JsonObject("{\"name\": \"region\", \"expression\": \"==\", \"value\": [\"eu-west\"]}");

        ServiceLevelObjective result = json.mapTo(ServiceLevelObjective.class);

        assertThat(result.getName()).isEqualTo("region");
        assertThat(result.getExpression()).isEqualTo(ExpressionType.EQ);
        assertThat(result.getValue().size()).isEqualTo(1);
        assertThat(result.getValue().get(0).getValueString()).isEqualTo("eu-west");
    }

    @Test
    void deserializeStringMultiple() {
        JsonObject json = new JsonObject("{\"name\": \"availability\", \"expression\": \"==\", " +
            "\"value\": [\"1\", \"2\", \"3\"]}");

        ServiceLevelObjective result = json.mapTo(ServiceLevelObjective.class);

        assertThat(result.getValue().size()).isEqualTo(3);
        assertThat(result.getValue().get(0).getValueString()).isEqualTo("1");
        assertThat(result.getValue().get(1).getValueString()).isEqualTo("2");
        assertThat(result.getValue().get(2).getValueString()).isEqualTo("3");
    }

    @Test
    void deserializeBooleanSingle() {
        JsonObject json = new JsonObject("{\"name\": \"online\", \"expression\": \"<\", \"value\": [false]}");

        ServiceLevelObjective result = json.mapTo(ServiceLevelObjective.class);

        assertThat(result.getName()).isEqualTo("online");
        assertThat(result.getExpression()).isEqualTo(ExpressionType.LT);
        assertThat(result.getValue().size()).isEqualTo(1);
        assertThat(result.getValue().get(0).getValueBool()).isEqualTo(false);
    }

    @Test
    void deserializeBooleanMultiple() {
        JsonObject json = new JsonObject("{\"name\": \"online\", \"expression\": \"<\", \"value\": [true, false, true]}");

        ServiceLevelObjective result = json.mapTo(ServiceLevelObjective.class);

        assertThat(result.getValue().size()).isEqualTo(3);
        assertThat(result.getValue().get(0).getValueBool()).isEqualTo(true);
        assertThat(result.getValue().get(1).getValueBool()).isEqualTo(false);
        assertThat(result.getValue().get(2).getValueBool()).isEqualTo(true);
    }

    @Test
    void deserializeContainerNumber() {
        JsonObject json = new JsonObject("{\"name\": \"availability\", \"expression\": \">\", " +
            "\"value\": [{\"slo_value_type\": \"number\", \"value_number\": 1}," +
            "{\"slo_value_type\": \"number\", \"value_number\": 2}," +
            "{\"slo_value_type\": \"number\", \"value_number\": 3}]}");

        ServiceLevelObjective result = json.mapTo(ServiceLevelObjective.class);

        assertThat(result.getValue().size()).isEqualTo(3);
        assertThat(result.getValue().get(0).getValueNumber()).isEqualTo(1.0);
        assertThat(result.getValue().get(1).getValueNumber()).isEqualTo(2.0);
        assertThat(result.getValue().get(2).getValueNumber()).isEqualTo(3.0);
    }

    @Test
    void deserializeContainerString() {
        JsonObject json = new JsonObject("{\"name\": \"availability\", \"expression\": \">\", " +
            "\"value\": [{\"slo_value_type\": \"string\", \"value_string\": \"1\"}," +
            "{\"slo_value_type\": \"string\", \"value_string\": \"2\"}," +
            "{\"slo_value_type\": \"string\", \"value_string\": \"3\"}]}");

        ServiceLevelObjective result = json.mapTo(ServiceLevelObjective.class);

        assertThat(result.getValue().size()).isEqualTo(3);
        assertThat(result.getValue().get(0).getValueString()).isEqualTo("1");
        assertThat(result.getValue().get(1).getValueString()).isEqualTo("2");
        assertThat(result.getValue().get(2).getValueString()).isEqualTo("3");
    }

    @Test
    void deserializeContainerBoolean() {
        JsonObject json = new JsonObject("{\"name\": \"availability\", \"expression\": \">\", " +
            "\"value\": [{\"slo_value_type\": \"boolean\", \"value_bool\": true}," +
            "{\"slo_value_type\": \"boolean\", \"value_bool\": false}," +
            "{\"slo_value_type\": \"boolean\", \"value_bool\": true}]}");

        ServiceLevelObjective result = json.mapTo(ServiceLevelObjective.class);

        assertThat(result.getValue().size()).isEqualTo(3);
        assertThat(result.getValue().get(0).getValueBool()).isEqualTo(true);
        assertThat(result.getValue().get(1).getValueBool()).isEqualTo(false);
        assertThat(result.getValue().get(2).getValueBool()).isEqualTo(true);
    }

    @Test
    void deserializeNonSupportedValue() {
        JsonObject json = new JsonObject("{\"name\": \"availability\", \"expression\": \">\", \"value\": [[]]}");

        assertThrows(IllegalArgumentException.class, () -> json.mapTo(ServiceLevelObjective.class));
    }
}


