package at.uibk.dps.rm.handler.resource;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.ext.web.RequestBody;
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
        JsonArray data = new JsonArray("[{\"metricId\": \"1\", \"value\": 4}, " +
            "{\"metricId\": \"2\", \"value\": \"ubuntu\"}]");

        RequestBody mockBody = mock(RequestBody.class);
        when(rc.body()).thenReturn(mockBody);
        when(mockBody.asJsonArray()).thenReturn(data);

        ResourceInputHandler.validateAddMetricsRequest(rc);

        verify(rc).next();
        testContext.completeNow();
    }

    @Test
    void validateAddMetricsRequestDuplicate(VertxTestContext testContext) {
        JsonArray data = new JsonArray("[{\"metricId\": \"1\", \"value\": 4}, " +
            "{\"metricId\": \"1\", \"value\": 8}]");

        RequestBody mockBody = mock(RequestBody.class);
        when(rc.body()).thenReturn(mockBody);
        when(mockBody.asJsonArray()).thenReturn(data);

        ResourceInputHandler.validateAddMetricsRequest(rc);

        verify(rc).fail(eq(400), any(Throwable.class));
        testContext.completeNow();
    }

    @Test
    void validateGetResourcesBySLORequestValid(VertxTestContext testContext) {
        JsonObject data = new JsonObject("{\"slo\": " +
            "[{\"name\": \"region\", \"expression\": \"<\", \"value\": [\"eu-west\"]}, " +
            "{\"name\": \"bandwidth\", \"expression\": \">\", \"value\": [1000]}]," +
            "\"limit\": 58467637.71067335}");

        RequestBody mockBody = mock(RequestBody.class);
        when(rc.body()).thenReturn(mockBody);
        when(mockBody.asJsonObject()).thenReturn(data);

        ResourceInputHandler.validateGetResourcesBySLOsRequest(rc);

        verify(rc).next();
        testContext.completeNow();
    }

    @Test
    void validateGetResourcesBySLORequestInvalidExpression(VertxTestContext testContext) {
        JsonObject data = new JsonObject("{\"slo\": " +
            "[{\"name\": \"region\", \"expression\": \"<\", \"value\": [\"eu-west\"]}, " +
            "{\"name\": \"bandwidth\", \"expression\": \">=<\", \"value\": [1000]}]," +
            "\"limit\": 58467637.71067335}");

        RequestBody mockBody = mock(RequestBody.class);
        when(rc.body()).thenReturn(mockBody);
        when(mockBody.asJsonObject()).thenReturn(data);

        ResourceInputHandler.validateGetResourcesBySLOsRequest(rc);

        verify(rc).fail(eq(400), any(Throwable.class));
        testContext.completeNow();
    }

    @Test
    void validateGetResourcesBySLORequestDuplicate(VertxTestContext testContext) {
        JsonObject data = new JsonObject("{\"slo\": " +
            "[{\"name\": \"region\", \"expression\": \"<\", \"value\": [\"eu-west\"]}, " +
            "{\"name\": \"region\", \"expression\": \">=<\", \"value\": [\"eu-north\"]}]," +
            "\"limit\": 58467637.71067335}");

        RequestBody mockBody = mock(RequestBody.class);
        when(rc.body()).thenReturn(mockBody);
        when(mockBody.asJsonObject()).thenReturn(data);

        ResourceInputHandler.validateGetResourcesBySLOsRequest(rc);

        verify(rc).fail(eq(400), any(Throwable.class));
        testContext.completeNow();
    }
}
