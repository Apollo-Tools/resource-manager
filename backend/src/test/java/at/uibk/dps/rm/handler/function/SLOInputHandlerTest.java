package at.uibk.dps.rm.handler.function;

import at.uibk.dps.rm.testutil.RoutingContextMockHelper;
import io.vertx.core.json.JsonObject;
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
public class SLOInputHandlerTest {

    @Mock
    private RoutingContext rc;

    @Test
    void validateGetResourcesBySLORequestValid(VertxTestContext testContext) {
        JsonObject jsonObject = new JsonObject("{\"slo\": " +
            "[{\"name\": \"region\", \"expression\": \"<\", \"value\": [\"eu-west\"]}, " +
            "{\"name\": \"bandwidth\", \"expression\": \">\", \"value\": [1000]}]," +
            "\"limit\": 58467637}");

        RoutingContextMockHelper.mockBody(rc, jsonObject);

        SLOInputHandler.validateGetResourcesBySLOsRequest(rc);

        verify(rc).next();
        testContext.completeNow();
    }

    @Test
    void validateGetResourcesBySLOEmptyRequest(VertxTestContext testContext) {
        JsonObject jsonObject = new JsonObject("{}");

        RoutingContextMockHelper.mockBody(rc, jsonObject);

        SLOInputHandler.validateGetResourcesBySLOsRequest(rc);

        verify(rc).next();
        testContext.completeNow();
    }

    @Test
    void validateGetResourcesBySLORequestInvalidExpression(VertxTestContext testContext) {
        JsonObject jsonObject = new JsonObject("{\"slo\": " +
            "[{\"name\": \"region\", \"expression\": \"<\", \"value\": [\"eu-west\"]}, " +
            "{\"name\": \"bandwidth\", \"expression\": \">=<\", \"value\": [1000]}]," +
            "\"limit\": 58467637}");

        RoutingContextMockHelper.mockBody(rc, jsonObject);

        SLOInputHandler.validateGetResourcesBySLOsRequest(rc);

        verify(rc).fail(eq(400), any(Throwable.class));
        testContext.completeNow();
    }

    @Test
    void validateGetResourcesBySLORequestDuplicate(VertxTestContext testContext) {
        JsonObject jsonObject = new JsonObject("{\"slo\": " +
            "[{\"name\": \"region\", \"expression\": \"<\", \"value\": [\"eu-west\"]}, " +
            "{\"name\": \"region\", \"expression\": \">=<\", \"value\": [\"eu-north\"]}]," +
            "\"limit\": 58467637}");

        RoutingContextMockHelper.mockBody(rc, jsonObject);

        SLOInputHandler.validateGetResourcesBySLOsRequest(rc);

        verify(rc).fail(eq(400), any(Throwable.class));
        testContext.completeNow();
    }
}
