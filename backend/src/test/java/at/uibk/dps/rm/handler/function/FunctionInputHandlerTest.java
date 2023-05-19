package at.uibk.dps.rm.handler.function;

import at.uibk.dps.rm.testutil.RoutingContextMockHelper;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.ext.web.RoutingContext;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

/**
 * Implements tests for the {@link FunctionInputHandler} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class FunctionInputHandlerTest {
    @Mock
    private RoutingContext rc;

    @ParameterizedTest
    @CsvSource({
        "foo1, true",
        "Foo1, false",
        "foo!, false",
        "foo_, false"
    })
    void validateGetResourcesBySLORequestValid(String name, boolean valid, VertxTestContext testContext) {
        JsonObject jsonObject = new JsonObject("{\"name\": \"" + name + "\"}");

        RoutingContextMockHelper.mockBody(rc, jsonObject);
        FunctionInputHandler.validateAddFunctionRequest(rc);

        if (valid) {
            verify(rc).next();
        } else {
            verify(rc).fail(eq(400), any(Throwable.class));
        }

        testContext.completeNow();
    }
}
