package at.uibk.dps.rm.handler.resource;

import at.uibk.dps.rm.testutil.RoutingContextMockHelper;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.ext.web.RoutingContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class ResourceSLOInputHandlerTest {

    @Mock
    private RoutingContext rc;

    @Test
    void validateGetResourcesBySLORequestValid(VertxTestContext testContext) {
        JsonObject jsonObject = new JsonObject("{\"slos\": " +
            "[{\"name\": \"region\", \"expression\": \"==\", \"value\": [1]}, " +
            "{\"name\": \"bandwidth\", \"expression\": \">\", \"value\": [1000]}]}");

        RoutingContextMockHelper.mockBody(rc, jsonObject);

        ResourceSLOInputHandler.validateGetResourcesBySLOsRequest(rc);

        verify(rc).next();
        testContext.completeNow();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{\"slos\": [{\"name\": \"region\", \"expression\": \"==\", \"value\": [1]}, " +
            "{\"name\": \"bandwidth\", \"expression\": \">=<\", \"value\": [1000]}]}",
            "{\"slos\": [{\"name\": \"region\", \"expression\": \"==\", \"value\": [1]}, " +
            "{\"name\": \"region\", \"expression\": \">=<\", \"value\": [\"eu-north\"]}]}"
    })
    void validateGetResourcesBySLORequestInvalid(JsonObject jsonObject, VertxTestContext testContext) {
        RoutingContextMockHelper.mockBody(rc, jsonObject);

        ResourceSLOInputHandler.validateGetResourcesBySLOsRequest(rc);

        verify(rc).fail(eq(400), any(Throwable.class));
        testContext.completeNow();
    }
}
