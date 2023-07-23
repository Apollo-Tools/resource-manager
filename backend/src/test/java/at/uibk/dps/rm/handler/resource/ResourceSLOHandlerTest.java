package at.uibk.dps.rm.handler.resource;

import at.uibk.dps.rm.entity.dto.CreateEnsembleRequest;
import at.uibk.dps.rm.entity.dto.ensemble.GetOneEnsemble;
import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.testutil.RoutingContextMockHelper;
import at.uibk.dps.rm.testutil.objectprovider.*;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.ext.web.RoutingContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Implements tests for the {@link ResourceSLOHandler} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class ResourceSLOHandlerTest {

    private ResourceSLOHandler handler;

    @Mock
    private ResourceChecker resourceChecker;

    @Mock
    private RoutingContext rc;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        handler = new ResourceSLOHandler(resourceChecker);
    }

    @ParameterizedTest
    @CsvSource({
            "1, 2, 2, true",
            "1, 2, 3, false"
    })
    void validateNewResourceEnsembleSLOsValid(long resourceId1, long resourceId2a, long resourceId2b, boolean isValid,
            VertxTestContext testContext) {
        CreateEnsembleRequest sloRequest = TestDTOProvider.createCreateEnsembleRequest(resourceId1, resourceId2a);
        mockGetResourcesBySLOsValid(resourceId1, resourceId2b);

        RoutingContextMockHelper.mockBody(rc, JsonObject.mapFrom(sloRequest));
        handler.validateNewResourceEnsembleSLOs(rc);

        if (isValid) {
            verify(rc).next();
        } else {
            verify(rc).fail(eq(400), any(Throwable.class));
        }
        testContext.completeNow();
    }

    @Test
    void validateExistingEnsemble(VertxTestContext testContext) {
        GetOneEnsemble sloRequest = TestDTOProvider.createGetOneEnsemble();
        mockGetResourcesBySLOsValid(1L, 3L);

        handler.validateExistingEnsemble(JsonObject.mapFrom(sloRequest))
            .subscribe(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(2);
                assertThat(result.get(0).getResourceId()).isEqualTo(1L);
                assertThat(result.get(0).getIsValid()).isEqualTo(true);
                assertThat(result.get(1).getResourceId()).isEqualTo(2L);
                assertThat(result.get(1).getIsValid()).isEqualTo(false);
                testContext.completeNow();
            }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    private void mockGetResourcesBySLOsValid(long resourceId1, long resourceId2) {
        Resource resource1 = TestResourceProvider.createResource(resourceId1);
        Resource resource2 = TestResourceProvider.createResource(resourceId2);
        JsonArray resourcesJson = new JsonArray(List.of(JsonObject.mapFrom(resource1), JsonObject.mapFrom(resource2)));
        when(resourceChecker.checkFindAllBySLOs(any(JsonObject.class))).thenReturn(Single.just(resourcesJson));
    }
}
