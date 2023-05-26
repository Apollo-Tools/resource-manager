package at.uibk.dps.rm.entity;

import at.uibk.dps.rm.entity.deployment.output.DeploymentOutput;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DeploymentOutputTest {

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
    }

    public static Stream<Arguments> provideJsonObject() {
        JsonObject jo1 = new JsonObject("{\"edge_urls\": {\"sensitive\": true, \"type\": \"string\", \"value\": " +
            "{\"value\": \"localhost\"}},\"function_urls\": {\"sensitive\": true,\"type\": \"string\", \"value\": " +
            "{}},\"vm_urls\": {\"sensitive\": true,\"type\": \"string\", \"value\":{}}}");
        JsonObject jo2 = new JsonObject("{\"edge_urls\": {\"sensitive\": true, \"type\": \"string\"}}");
        JsonObject jo3 = new JsonObject("{\"edge_urls\": {\"sensitive\": true, \"type\": \"string\", \"value\": {}, " +
            "\"invalid\": 1}}");

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
            assertThat(expected.getEdgeUrls().getValue().get("value")).isEqualTo("localhost");
        } else {
            assertThrows(IllegalArgumentException.class, () -> DeploymentOutput.fromJson(jsonObject));
        }
    }
}
