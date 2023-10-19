package at.uibk.dps.rm.util.serialization;

import at.uibk.dps.rm.entity.dto.slo.SLOValue;
import at.uibk.dps.rm.testutil.objectprovider.TestDTOProvider;
import io.vertx.core.json.JsonObject;
import lombok.Data;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Implements tests for the {@link SLOValueSerializer} class.
 *
 * @author matthi-g
 */
public class SLOValueSerializerTest {

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
    }

    /**
     * Implements a wrapper class for a list of slo values.
     */
    @Data
    private static class SLOValueWrapper {
        private final List<SLOValue> sloValue;
    }

    private static Stream<Arguments> provideSLOValue() {
        return Stream.of(
                Arguments.of(TestDTOProvider.createSLOValue(1.0), "[1.0]"),
                Arguments.of(TestDTOProvider.createSLOValue("one"), "[\"one\"]"),
                Arguments.of(TestDTOProvider.createSLOValue(false), "[false]")
        );
    }
    @ParameterizedTest
    @MethodSource("provideSLOValue")
    void serialize(SLOValue sloValue, String expectedValue) {
        SLOValueWrapper wrapper = new SLOValueWrapper(List.of(sloValue));
        JsonObject result = JsonObject.mapFrom(wrapper);

        assertThat(result.getValue("slo_value").toString()).isEqualTo(expectedValue);
    }
}
