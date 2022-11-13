package at.uibk.dps.rm.handler.resource;

import at.uibk.dps.rm.testutil.RoutingContextMockHelper;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.ext.web.RoutingContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class ResourceInputHandlerTest {

    @Mock
    RoutingContext rc;

    @Test
    void validateAddMetricsRequestNoDuplicates(VertxTestContext testContext) {
        JsonArray jsonArray = new JsonArray("[{\"metricId\": \"1\", \"value\": 4}, " +
            "{\"metricId\": \"2\", \"value\": \"ubuntu\"}]");

        RoutingContextMockHelper.mockBody(rc, jsonArray);

        ResourceInputHandler.validateAddMetricsRequest(rc);

        verify(rc).next();
        testContext.completeNow();
    }

    @Test
    void validateAddMetricsRequestDuplicate(VertxTestContext testContext) {
        JsonArray jsonArray = new JsonArray("[{\"metricId\": \"1\", \"value\": 4}, " +
            "{\"metricId\": \"1\", \"value\": 8}]");

        RoutingContextMockHelper.mockBody(rc, jsonArray);

        ResourceInputHandler.validateAddMetricsRequest(rc);

        verify(rc).fail(eq(400), any(Throwable.class));
        testContext.completeNow();
    }

    @Test
    void validateGetResourcesBySLORequestValid(VertxTestContext testContext) {
        JsonObject jsonObject = new JsonObject("{\"slo\": " +
            "[{\"name\": \"region\", \"expression\": \"<\", \"value\": [\"eu-west\"]}, " +
            "{\"name\": \"bandwidth\", \"expression\": \">\", \"value\": [1000]}]," +
            "\"limit\": 58467637.71067335}");

        RoutingContextMockHelper.mockBody(rc, jsonObject);

        ResourceInputHandler.validateGetResourcesBySLOsRequest(rc);

        verify(rc).next();
        testContext.completeNow();
    }

    @Test
    void validateGetResourcesBySLORequestInvalidExpression(VertxTestContext testContext) {
        JsonObject jsonObject = new JsonObject("{\"slo\": " +
            "[{\"name\": \"region\", \"expression\": \"<\", \"value\": [\"eu-west\"]}, " +
            "{\"name\": \"bandwidth\", \"expression\": \">=<\", \"value\": [1000]}]," +
            "\"limit\": 58467637.71067335}");

        RoutingContextMockHelper.mockBody(rc, jsonObject);

        ResourceInputHandler.validateGetResourcesBySLOsRequest(rc);

        verify(rc).fail(eq(400), any(Throwable.class));
        testContext.completeNow();
    }

    @Test
    void validateGetResourcesBySLORequestDuplicate(VertxTestContext testContext) {
        JsonObject jsonObject = new JsonObject("{\"slo\": " +
            "[{\"name\": \"region\", \"expression\": \"<\", \"value\": [\"eu-west\"]}, " +
            "{\"name\": \"region\", \"expression\": \">=<\", \"value\": [\"eu-north\"]}]," +
            "\"limit\": 58467637.71067335}");

        RoutingContextMockHelper.mockBody(rc, jsonObject);

        ResourceInputHandler.validateGetResourcesBySLOsRequest(rc);

        verify(rc).fail(eq(400), any(Throwable.class));
        testContext.completeNow();
    }
}