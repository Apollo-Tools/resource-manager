package at.uibk.dps.rm.entity;

import at.uibk.dps.rm.entity.deployment.output.DeploymentOutput;
import at.uibk.dps.rm.entity.deployment.output.TFOutputValue;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Implements tests for the {@link DeploymentOutput} class.
 *
 * @author matthi-g
 */
public class DeploymentOutputTest {

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
    }

    public static Stream<Arguments> provideJsonObject() {
        JsonObject jo1 = new JsonObject("{\"resource_output\": {\"sensitive\": true,\"type\": \"object\", \"value\": " +
            "{\"r1_test_python38_1\": {\"base_url\": \"http://192.168.0.1\", \"full_url\": \"http://192.168.0" +
            ".1:8080/function/r1_test_python38_1\", \"metrics_port\": 9100, \"openfaas_port\": 8080, \"path\": " +
            "\"/function/r1_test_python38_23\"}}}}");
        JsonObject jo2 = new JsonObject("{\"resource_output\": {\"sensitive\": true, \"type\": \"object\"}}");
        JsonObject jo3 = new JsonObject("{\"resource_output\": {\"sensitive\": true, \"type\": \"string\", " +
            "\"value\": {}, \"invalid\": 1}}");

        return Stream.of(
            Arguments.of(jo1, true),
            Arguments.of(jo2, false),
            Arguments.of(jo3, false)
        );
    }

    @ParameterizedTest
    @MethodSource("provideJsonObject")
    void fromJsonValid(JsonObject jsonObject, boolean isValid) {
        if (isValid) {
            DeploymentOutput expected = DeploymentOutput.fromJson(jsonObject);
            TFOutputValue tfOutputValue = expected.getResourceOutput().getValue().get("r1_test_python38_1");
            assertThat(tfOutputValue.getBaseUrl()).isEqualTo("http://192.168.0.1");
            assertThat(tfOutputValue.getFullUrl()).isEqualTo("http://192.168.0.1:8080/function/" +
                "r1_test_python38_1");
            assertThat(tfOutputValue.getMetricsPort()).isEqualTo(9100);
            assertThat(tfOutputValue.getOpenfaasPort()).isEqualTo(8080);
            assertThat(tfOutputValue.getPath()).isEqualTo("/function/r1_test_python38_23");
        } else {
            assertThrows(IllegalArgumentException.class, () -> DeploymentOutput.fromJson(jsonObject));
        }
    }
}
