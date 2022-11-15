package at.uibk.dps.rm.handler.reservation;

import at.uibk.dps.rm.entity.dto.ReserveResourcesRequest;
import at.uibk.dps.rm.testutil.RoutingContextMockHelper;
import at.uibk.dps.rm.testutil.TestObjectProvider;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class ReservationInputHandlerTest {

    @Mock
    RoutingContext rc;

    @Test
    void validateResourceArrayHasNoDuplicatesNoDuplicates(VertxTestContext testContext) {
        List<Long> resources = List.of(1L, 2L, 3L, 4L);
        ReserveResourcesRequest request = TestObjectProvider.createReserveResourcesRequest(resources,
            false);
        JsonObject requestBody = JsonObject.mapFrom(request);

        RoutingContextMockHelper.mockBody(rc, requestBody);

        ReservationInputHandler.validateResourceArrayHasNoDuplicates(rc);

        verify(rc).next();
        testContext.completeNow();
    }

    @ParameterizedTest
    @CsvSource({
        "1, 2, 3, 1",
        "1, 1, 1, 1",
        "2, 1, 2, 1",
        "1, 3, 1, 1"
    })
    void validateResourceArrayHasNoDuplicatesDuplicate(long a, long b, long c, long d,
                                                       VertxTestContext testContext) {
        List<Long> resources = List.of(a, b, c, d);
        ReserveResourcesRequest request = TestObjectProvider.createReserveResourcesRequest(resources,
            false);
        JsonObject requestBody = JsonObject.mapFrom(request);

        RoutingContextMockHelper.mockBody(rc, requestBody);

        ReservationInputHandler.validateResourceArrayHasNoDuplicates(rc);

        verify(rc).fail(eq(400), any(Throwable.class));
        testContext.completeNow();
    }
}
