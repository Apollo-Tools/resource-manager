package at.uibk.dps.rm.handler.resource;

import at.uibk.dps.rm.entity.dto.ensemble.GetOneEnsemble;
import at.uibk.dps.rm.entity.model.MetricValue;
import at.uibk.dps.rm.entity.model.Resource;
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
            "1, 3, true",
            "1, 2, false"
    })
    void validateNewResourceEnsembleSLOsValid(long resourceId1, long resourceId2, boolean isValid,
            VertxTestContext testContext) {
        mockGetResourcesBySLOsValid();

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
        mockGetResourcesBySLOsValid();
        GetOneEnsemble sloRequest = TestDTOProvider.createGetOneEnsemble();

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

    private void mockGetResourcesBySLOsValid() {
        Resource resource1 = TestResourceProvider.createResource(1L);
        MetricValue mv1 = TestMetricProvider.createMetricValue(1L, 1L, "latency", 25.0);
        MetricValue mv2 = TestMetricProvider.createMetricValue(2L, 2L, "availability", 0.995);
        Resource resource2 = TestResourceProvider.createResource(2L);
        MetricValue mv3 = TestMetricProvider.createMetricValue(3L, 2L, "availability", 0.8);
        Resource resource3 = TestResourceProvider.createResource(3L);
        MetricValue mv4 = TestMetricProvider.createMetricValue(4L, 3L, "bandwidth", 1000);
        MetricValue mv5 = TestMetricProvider.createMetricValue(5L, 2L, "availability", 0.999);
        JsonArray resourcesJson = new JsonArray(List.of(JsonObject.mapFrom(resource1), JsonObject.mapFrom(resource2),
                JsonObject.mapFrom(resource3)));
        resourcesJson.getJsonObject(0).put("metric_values", new JsonArray(List.of(JsonObject.mapFrom(mv1),
            JsonObject.mapFrom(mv2))));
        resourcesJson.getJsonObject(1).put("metric_values", new JsonArray(List.of(JsonObject.mapFrom(mv3))));
        resourcesJson.getJsonObject(2).put("metric_values", new JsonArray(List.of(JsonObject.mapFrom(mv4),
            JsonObject.mapFrom(mv5))));
        when(resourceChecker.checkFindAllBySLOs(new JsonObject())).thenReturn(Single.just(resourcesJson));
    }
}
