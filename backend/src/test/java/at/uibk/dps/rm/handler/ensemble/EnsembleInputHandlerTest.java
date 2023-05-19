package at.uibk.dps.rm.handler.ensemble;

import at.uibk.dps.rm.testutil.RoutingContextMockHelper;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.ext.web.RoutingContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

/**
 * Implements tests for the {@link EnsembleInputHandler} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class EnsembleInputHandlerTest {

    @Mock
    private RoutingContext rc;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
    }

    @Test
    void validateCreateEnsembleRequestValid(VertxTestContext testContext) {
        JsonObject jsonObject = new JsonObject("{\"name\":\"ensemble\",\"slos\":[{\"name\":\"region\"," +
                "\"expression\":\"==\",\"value\":[1,3]}],\"resources\":[{\"resource_id\":1},{\"resource_id\":2}]}");

        RoutingContextMockHelper.mockBody(rc, jsonObject);

        EnsembleInputHandler.validateCreateEnsembleRequest(rc);

        verify(rc).next();
        testContext.completeNow();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{\"name\":\"ensemble\",\"slos\":[{\"name\":\"region\",\"expression\":\">=\",\"value\":[1,3]}]," +
            "\"resources\":[{\"resource_id\":1},{\"resource_id\":2}]}",
            "{\"name\":\"ensemble\",\"slos\":[{\"name\":\"region\",\"expression\":\"==\",\"value\":[1,3]}, " +
            "{\"name\":\"region\",\"expression\":\"==\",\"value\":[1,3]}]," +
            "\"resources\":[{\"resource_id\":1},{\"resource_id\":2}]}",
            "{\"name\":\"ensemble\",\"slos\":[{\"name\":\"region\",\"expression\":\">=\",\"value\":[1,3]}]," +
            "\"resources\":[{\"resource_id\":1},{\"resource_id\":1}]}",
    })
    void validateCreateEnsembleRequestInvalid(JsonObject jsonObject, VertxTestContext testContext) {
        RoutingContextMockHelper.mockBody(rc, jsonObject);

        EnsembleInputHandler.validateCreateEnsembleRequest(rc);

        verify(rc).fail(eq(400), any(Throwable.class));
        testContext.completeNow();
    }
}
