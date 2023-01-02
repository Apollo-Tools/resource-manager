package at.uibk.dps.rm.entity;

import at.uibk.dps.rm.entity.dto.slo.ExpressionType;
import io.vertx.junit5.VertxExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@ExtendWith(VertxExtension.class)
public class ExpressionTypeUtilTest {

    @ParameterizedTest
    @CsvSource({
            ">, true",
            "<, true",
            "==, true",
            "<=, false",
            ">=, false"
    })
    void symbolExists(String symbol, boolean expectedResult) {
        assertThat(ExpressionType.symbolExists(symbol)).isEqualTo(expectedResult);
    }

    @ParameterizedTest
    @ValueSource(strings = {">", "<", "=="})
    void fromString(String symbol) {
        assertThat(ExpressionType.fromString(symbol).getSymbol()).isEqualTo(symbol);
    }

    @Test
    void fromStringIllegalArgumentException() {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> ExpressionType.fromString("<="))
            .withMessageContaining("unknown value:");
    }

    @ParameterizedTest
    @CsvSource({
            "5.0, -3.2, -1",
            "-5.0, 3.2, 1",
            "3.2, 3.2, 0"
    })
    void compareValuesGTNumber(Double v1, Double v2, int expectedResult) {
        assertThat(ExpressionType.compareValues(ExpressionType.GT, v1, v2))
            .isEqualTo(expectedResult);
    }

    @ParameterizedTest
    @CsvSource({
            "5.0, -3.2, 1",
            "-5.0, 3.2, -1",
            "3.2, 3.2, 0"
    })
    void compareValuesLTNumber(Double v1, Double v2, int expectedResult) {
        assertThat(ExpressionType.compareValues(ExpressionType.LT, v1, v2))
                .isEqualTo(expectedResult);
    }

    @ParameterizedTest
    @CsvSource({
            "5.0, -3.2, 1",
            "-5.0, 3.2, -1",
            "3.2, 3.2, 0"
    })
    void compareValuesEQNumber(Double v1, Double v2, int expectedResult) {
        assertThat(ExpressionType.compareValues(ExpressionType.EQ, v1, v2))
                .isEqualTo(expectedResult);
    }

    @ParameterizedTest
    @CsvSource({
            "'hello', 'hellp', -1",
            "'hello', 'hello', 0",
    })
    void compareValuesEQString(String v1, String v2, int expectedResult) {
        assertThat(ExpressionType.compareValues(ExpressionType.EQ, v1, v2))
                .isEqualTo(expectedResult);
    }

    @ParameterizedTest
    @CsvSource({
            "'hello', 'hello', -1"
    })
    void compareValuesNonEQString(String v1, String v2, int expectedResult) {
        assertThat(ExpressionType.compareValues(ExpressionType.GT, v1, v2))
                .isEqualTo(expectedResult);
    }

    @ParameterizedTest
    @CsvSource({
            "false, false, 0",
            "true, false, 1",
    })
    void compareValuesEQBoolean(Boolean v1, Boolean v2, int expectedResult) {
        assertThat(ExpressionType.compareValues(ExpressionType.EQ, v1, v2))
                .isEqualTo(expectedResult);
    }

    @ParameterizedTest
    @CsvSource({
            "false, false, -1",
    })
    void compareValuesNonEQBoolean(Boolean v1, Boolean v2, int expectedResult) {
        assertThat(ExpressionType.compareValues(ExpressionType.GT, v1, v2))
                .isEqualTo(expectedResult);
    }
}
