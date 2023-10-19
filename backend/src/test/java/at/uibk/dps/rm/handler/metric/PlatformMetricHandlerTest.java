package at.uibk.dps.rm.handler.metric;

import at.uibk.dps.rm.service.rxjava3.database.metric.PlatformMetricService;
import at.uibk.dps.rm.testutil.objectprovider.TestMetricProvider;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.when;

/**
 * Implements tests for the {@link PlatformMetricHandler} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class PlatformMetricHandlerTest {

    private PlatformMetricHandler handler;

    @Mock
    private PlatformMetricService platformMetricService;

    @Mock
    private RoutingContext rc;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        handler = new PlatformMetricHandler(platformMetricService);
    }

    @Test
    void getAllValid(VertxTestContext testContext) {
        long platformId = 1L;
        JsonObject pm1 = JsonObject.mapFrom(TestMetricProvider.createPlatformMetric(1L, 2L));
        JsonObject pm2 = JsonObject.mapFrom(TestMetricProvider.createPlatformMetric(2L, 4L));
        JsonObject pm3 = JsonObject.mapFrom(TestMetricProvider.createPlatformMetric(3L, 6L));
        JsonArray response = new JsonArray(List.of(pm1, pm2, pm3));


        when(rc.pathParam("id")).thenReturn(String.valueOf(platformId));
        when(platformMetricService.findAllByPlatformId(platformId)).thenReturn(Single.just(response));

        handler.getAll(rc)
            .subscribe(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(3);
                assertThat(result.getJsonObject(0).getJsonObject("metric").getLong("metric_id"))
                    .isEqualTo(2L);
                assertThat(result.getJsonObject(1).getJsonObject("metric").getLong("metric_id"))
                    .isEqualTo(4L);
                assertThat(result.getJsonObject(2).getJsonObject("metric").getLong("metric_id"))
                    .isEqualTo(6);
                testContext.completeNow();
            }),
            throwable -> testContext.verify(() -> fail("method has thrown exception"))
        );
    }
}
