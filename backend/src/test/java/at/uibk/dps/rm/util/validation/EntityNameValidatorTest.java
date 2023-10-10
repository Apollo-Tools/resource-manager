package at.uibk.dps.rm.util.validation;

import at.uibk.dps.rm.exception.BadInputException;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

/**
 * Implements tests for the {@link EntityNameValidator} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
public class EntityNameValidatorTest {

    @ParameterizedTest
    @CsvSource({
        "foo1, true",
        "Foo1, false",
        "foo!, false",
        "foo_, false"
    })
    void checkName(String name, boolean valid, VertxTestContext testContext) {
        EntityNameValidator.checkName(name, Long.class)
            .subscribe(() -> testContext.verify(() -> {
                if (valid) {
                    testContext.completeNow();
                } else {
                    fail("method did not throw exception");
                }
            }), throwable -> {
                if (valid) {
                    fail("method has thrown exception");
                } else {
                    assertThat(throwable).isInstanceOf(BadInputException.class);
                    assertThat(throwable.getMessage()).isEqualTo("invalid Long name");
                    testContext.completeNow();
                }
            });
    }

}
