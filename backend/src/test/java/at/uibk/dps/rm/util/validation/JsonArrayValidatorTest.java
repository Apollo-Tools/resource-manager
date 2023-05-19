package at.uibk.dps.rm.util.validation;

import io.vertx.core.json.JsonArray;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * Implements tests for the {@link JsonArrayValidator} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class JsonArrayValidatorTest {

    @Test
    void checkJsonArrayDuplicates(VertxTestContext testContext) {
        JsonArray jsonArray = new JsonArray("[{\"id\": 1}, {\"id\": 2}, {\"id\": 3}]");
        String key = "id";

        JsonArrayValidator.checkJsonArrayDuplicates(jsonArray, key)
            .blockingSubscribe(() -> {},
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
        testContext.completeNow();
    }

    @ParameterizedTest
    @ValueSource(strings = {"[{\"id\": 1}, {\"id\": 1}, {\"id\": 1}]", "[{\"id\": 1}, {\"id\": 2}, {\"id\": 1}]",
        "[{\"id\": 1}, {\"id\": 2}, {\"id\": 2}]"})
    void checkJsonArrayDuplicates(String json, VertxTestContext testContext) {
        JsonArray jsonArray = new JsonArray(json);
        String key = "id";

        JsonArrayValidator.checkJsonArrayDuplicates(jsonArray, key)
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(Throwable.class);
                    assertThat(throwable.getMessage()).isEqualTo("duplicated input");
                    testContext.completeNow();
                })
            );
    }
}
