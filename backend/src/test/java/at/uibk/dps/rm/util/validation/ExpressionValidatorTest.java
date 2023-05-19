package at.uibk.dps.rm.util.validation;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * Implements tests for the {@link ExpressionValidator} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
public class ExpressionValidatorTest {

    @ParameterizedTest
    @CsvSource({
        "==, <, >, true",
        "==, <=, >=, false"
    })
    void checkExpressionsAreValid(String s1, String s2, String s3, boolean isValid, VertxTestContext testContext) {
        JsonObject slo1 = new JsonObject("{\"name\": \"region\", \"expression\": \"" + s1 +"\", \"value\": [1]}");
        JsonObject slo2 = new JsonObject("{\"name\": \"region\", \"expression\": \"" + s2 +"\", \"value\": [1]}");
        JsonObject slo3 = new JsonObject("{\"name\": \"region\", \"expression\": \"" + s3 +"\", \"value\": [1]}");
        JsonArray slos = new JsonArray(List.of(slo1, slo2, slo3));

        ExpressionValidator.checkExpressionAreValid(slos)
            .blockingSubscribe(() -> testContext.verify(() -> {
                    if (!isValid) {
                        fail("method did not throw exception");
                    }
            }),
            throwable -> testContext.verify(() -> {
                if (isValid) {
                    fail("method has thrown exception");
                } else {
                    assertThat(throwable).isInstanceOf(Throwable.class);
                    assertThat(throwable.getMessage()).isEqualTo("expression is not supported");
                }
                testContext.completeNow();
            }));
        testContext.completeNow();
    }

    @Test
    void checkExpressionsAreValidNull(VertxTestContext testContext) {
        ExpressionValidator.checkExpressionAreValid(null)
            .blockingSubscribe(() -> {},
            throwable -> testContext.verify(() -> fail("method has thrown exception")));
        testContext.completeNow();
    }

}
