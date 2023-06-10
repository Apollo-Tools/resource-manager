package at.uibk.dps.rm.handler.deployment;

import at.uibk.dps.rm.entity.dto.DeployResourcesRequest;
import at.uibk.dps.rm.entity.dto.deployment.FunctionResourceIds;
import at.uibk.dps.rm.entity.dto.deployment.ServiceResourceIds;
import at.uibk.dps.rm.testutil.RoutingContextMockHelper;
import at.uibk.dps.rm.testutil.objectprovider.TestFunctionProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestRequestProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestServiceProvider;
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

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

/**
 * Implements tests for the {@link DeploymentInputHandler} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class ReservationInputHandlerTest {

    @Mock
    RoutingContext rc;

    @Test
    void validateResourceArrayHasNoDuplicatesNoDuplicates(VertxTestContext testContext) {
        List<FunctionResourceIds> fids = TestFunctionProvider.createFunctionResourceIdsList(1L, 2L, 3L);
        List<ServiceResourceIds> sids = TestServiceProvider.createServiceResourceIdsList(4L);
        DeployResourcesRequest request = TestRequestProvider.createReserveResourcesRequest(fids, sids);
        JsonObject requestBody = JsonObject.mapFrom(request);

        RoutingContextMockHelper.mockBody(rc, requestBody);

        DeploymentInputHandler.validateResourceArrayHasNoDuplicates(rc);

        verify(rc).next();
        testContext.completeNow();
    }

    @ParameterizedTest
    @CsvSource({
        "1, 2, 3, 1, 2, 1, 2, 2",
        "1, 1, 1, 1, 2, 2, 2, 2",
        "2, 1, 2, 1, 2, 1, 2, 1",
        "1, 3, 1, 1, 2, 2, 3, 3"
    })
    void validateResourceArrayHasNoDuplicatesDuplicate(long f1, long f2, long f3, long f4, long r1, long r2, long r3,
                                                       long r4, VertxTestContext testContext) {
        FunctionResourceIds ids1 = TestFunctionProvider.createFunctionResourceIds(f1, r1);
        FunctionResourceIds ids2 = TestFunctionProvider.createFunctionResourceIds(f2, r2);
        FunctionResourceIds ids3 = TestFunctionProvider.createFunctionResourceIds(f3, r3);
        FunctionResourceIds ids4 = TestFunctionProvider.createFunctionResourceIds(f4, r4);
        List<FunctionResourceIds> fids = List.of(ids1, ids2, ids3, ids4);
        List<ServiceResourceIds> sids = new ArrayList<>();
        DeployResourcesRequest request = TestRequestProvider.createReserveResourcesRequest(fids, sids);
        JsonObject requestBody = JsonObject.mapFrom(request);

        RoutingContextMockHelper.mockBody(rc, requestBody);

        DeploymentInputHandler.validateResourceArrayHasNoDuplicates(rc);

        verify(rc).fail(eq(400), any(Throwable.class));
        testContext.completeNow();
    }
}
