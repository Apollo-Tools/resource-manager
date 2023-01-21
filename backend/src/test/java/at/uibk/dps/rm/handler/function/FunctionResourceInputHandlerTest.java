package at.uibk.dps.rm.handler.function;

import at.uibk.dps.rm.testutil.RoutingContextMockHelper;
import io.vertx.core.json.JsonArray;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.ext.web.RoutingContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class FunctionResourceInputHandlerTest {
    @Mock
    private RoutingContext rc;

    @Test
    void validateAddMetricsRequestNoDuplicates(VertxTestContext testContext) {
        JsonArray jsonArray = new JsonArray("[{\"resource_id\": \"1\"}, " +
            "{\"resource_id\": \"2\"}]");

        RoutingContextMockHelper.mockBody(rc, jsonArray);

        FunctionResourceInputHandler.validateAddFunctionResourceRequest(rc);

        verify(rc).next();
        testContext.completeNow();
    }

    @Test
    void validateAddMetricsRequestDuplicate(VertxTestContext testContext) {
        JsonArray jsonArray = new JsonArray("[{\"resource_id\": \"1\"}, " +
            "{\"resource_id\": \"1\"}]");

        RoutingContextMockHelper.mockBody(rc, jsonArray);

        FunctionResourceInputHandler.validateAddFunctionResourceRequest(rc);

        verify(rc).fail(eq(400), any(Throwable.class));
        testContext.completeNow();
    }
}
