package at.uibk.dps.rm.util;

import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class ArrayValidatorTest {

    @Test
    void checkJsonArrayDuplicates(VertxTestContext testContext) {
        ArrayValidator<String> validator = new ArrayValidator<>();
        Collection<String> collection = List.of("1", "one", "eins", "uno");

        validator.hasDuplicates(collection)
            .blockingSubscribe(() -> {},
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
        testContext.completeNow();
    }

    @ParameterizedTest
    @CsvSource({
        "1, one, 1",
        "1, 1, 1",
        "one, 1, 1",
    })
    void checkJsonArrayDuplicates(String s1, String s2, String s3, VertxTestContext testContext) {
        ArrayValidator<String> validator = new ArrayValidator<>();
        Collection<String> collection = List.of(s1, s2, s3);

        validator.hasDuplicates(collection)
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(Throwable.class);
                    assertThat(throwable.getMessage()).isEqualTo("duplicated input");
                    testContext.completeNow();
                })
            );
    }
}
