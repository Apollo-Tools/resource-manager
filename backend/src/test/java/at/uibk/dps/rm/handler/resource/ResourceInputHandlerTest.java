package at.uibk.dps.rm.handler.resource;

import at.uibk.dps.rm.testutil.RoutingContextMockHelper;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.ext.web.RoutingContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

/**
 * Implements tests for the {@link ResourceInputHandler} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class ResourceInputHandlerTest {

    @Mock
    RoutingContext rc;

    @ParameterizedTest
    @CsvSource({
        "resource1, true",
        "Resource1, false",
        "resource!, false",
        "resource_, false"
    })
    void validateAddResourceRequest(String name, boolean valid, VertxTestContext testContext) {
        JsonObject jsonObject = new JsonObject("{\"name\": \"" + name + "\"}");

        RoutingContextMockHelper.mockBody(rc, jsonObject);
        ResourceInputHandler.validateAddResourceRequest(rc);

        if (valid) {
            verify(rc).next();
        } else {
            verify(rc).fail(eq(400), any(Throwable.class));
        }
        testContext.completeNow();
    }

    @Test
    void validateAddMetricsRequestNoDuplicates(VertxTestContext testContext) {
        JsonArray jsonArray = new JsonArray("[{\"metric_id\": \"1\", \"value\": 4}, " +
            "{\"metric_id\": \"2\", \"value\": \"ubuntu\"}]");

        RoutingContextMockHelper.mockBody(rc, jsonArray);

        ResourceInputHandler.validateAddMetricsRequest(rc);

        verify(rc).next();
        testContext.completeNow();
    }

    @Test
    void validateAddMetricsRequestDuplicate(VertxTestContext testContext) {
        JsonArray jsonArray = new JsonArray("[{\"metric_id\": \"1\", \"value\": 4}, " +
            "{\"metric_id\": \"1\", \"value\": 8}]");

        RoutingContextMockHelper.mockBody(rc, jsonArray);

        ResourceInputHandler.validateAddMetricsRequest(rc);

        verify(rc).fail(eq(400), any(Throwable.class));
        testContext.completeNow();
    }
}
