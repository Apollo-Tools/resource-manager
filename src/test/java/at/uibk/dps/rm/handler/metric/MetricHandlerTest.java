package at.uibk.dps.rm.handler.metric;

import at.uibk.dps.rm.exception.AlreadyExistsException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.service.rxjava3.database.metric.MetricService;
import at.uibk.dps.rm.service.rxjava3.database.metric.MetricTypeService;
import at.uibk.dps.rm.testutil.RoutingContextMockHelper;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.ext.web.RoutingContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class MetricHandlerTest {

    private MetricHandler metricHandler;

    @Mock
    private MetricService metricService;

    @Mock
    private MetricTypeService metricTypeService;

    @Mock
    private RoutingContext rc;

    @BeforeEach
    void initTest() {
        metricHandler = new MetricHandler(metricService, metricTypeService);
    }

    @Test
    void postOneValid(VertxTestContext testContext) {
        JsonObject jsonObject = new JsonObject("{\"metric\": \"availability\", " +
            "\"description\": \"the availability of a resource\", \"is_monitored\": true, " +
            "\"metric_type\": {\"metric_type_id\": 1}}");

        RoutingContextMockHelper.mockBody(rc, jsonObject);
        when(metricService.existsOneByMetric("availability")).thenReturn(Single.just(false));
        when(metricTypeService.existsOneById(1L)).thenReturn(Single.just(true));
        when(metricService.save(jsonObject)).thenReturn(Single.just(jsonObject));

        metricHandler.postOne(rc)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.getString("metric")).isEqualTo("availability");
                    assertThat(result.getBoolean("is_monitored")).isEqualTo(true);
                    verify(metricService).existsOneByMetric("availability");
                    verify(metricTypeService).existsOneById(1L);
                    verify(metricService).save(jsonObject);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method did throw exception"))
            );
    }

    @Test
    void postOneDuplicated(VertxTestContext testContext) {
        JsonObject jsonObject = new JsonObject("{\"metric\": \"availability\", " +
            "\"description\": \"the availability of a resource\", \"is_monitored\": true, " +
            "\"metric_type\": {\"metric_type_id\": 1}}");

        RoutingContextMockHelper.mockBody(rc, jsonObject);
        when(metricService.existsOneByMetric("availability")).thenReturn(Single.just(true));
        when(metricTypeService.existsOneById(1L)).thenReturn(Single.just(true));

        metricHandler.postOne(rc)
            .subscribe(result -> testContext.verify(() -> fail("method did throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(AlreadyExistsException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void postOneMetricTypeNotFound(VertxTestContext testContext) {
        JsonObject jsonObject = new JsonObject("{\"metric\": \"availability\", " +
            "\"description\": \"the availability of a resource\", \"is_monitored\": true, " +
            "\"metric_type\": {\"metric_type_id\": 1}}");

        RoutingContextMockHelper.mockBody(rc, jsonObject);
        when(metricService.existsOneByMetric("availability")).thenReturn(Single.just(false));
        when(metricTypeService.existsOneById(1L)).thenReturn(Single.just(false));

        metricHandler.postOne(rc)
            .subscribe(result -> testContext.verify(() -> fail("method did throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }
}
