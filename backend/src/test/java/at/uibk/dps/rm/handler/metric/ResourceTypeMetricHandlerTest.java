package at.uibk.dps.rm.handler.metric;


import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.handler.resource.ResourceTypeChecker;
import at.uibk.dps.rm.testutil.TestObjectProvider;
import at.uibk.dps.rm.util.JsonMapperConfig;
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

@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class ResourceTypeMetricHandlerTest {

    private ResourceTypeMetricHandler handler;

    @Mock
    private MetricChecker metricChecker;

    @Mock
    private ResourceTypeChecker resourceTypeChecker;

    @Mock
    private RoutingContext rc;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        handler = new ResourceTypeMetricHandler(metricChecker, resourceTypeChecker);
    }

    @Test
    void getAllValid(VertxTestContext testContext) {
        long typeId = 1L;
        JsonObject rt1 = JsonObject.mapFrom(TestObjectProvider.createResourceType(typeId, "vm"));
        JsonObject m1 = JsonObject.mapFrom(TestObjectProvider.createMetric(1L, "cpu"));
        JsonObject m2 = JsonObject.mapFrom(TestObjectProvider.createMetric(2L, "memory"));
        JsonObject m3 = JsonObject.mapFrom(TestObjectProvider.createMetric(3L, "storage"));
        JsonArray required = new JsonArray(List.of(m1, m2));
        JsonArray optional = new JsonArray(List.of(m3));

        when(rc.pathParam("id")).thenReturn(String.valueOf(typeId));
        when(resourceTypeChecker.checkFindOne(typeId)).thenReturn(Single.just(rt1));
        when(metricChecker.checkFindAllByResourceTypeId(typeId, true)).thenReturn(Single.just(required));
        when(metricChecker.checkFindAllByResourceTypeId(typeId, false)).thenReturn(Single.just(optional));

        handler.getAll(rc)
            .subscribe(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(3);
                assertThat(result.getJsonObject(0).getJsonObject("metric").getLong("metric_id"))
                    .isEqualTo(1L);
                assertThat(result.getJsonObject(1).getJsonObject("metric").getLong("metric_id"))
                    .isEqualTo(2L);
                assertThat(result.getJsonObject(2).getJsonObject("metric").getLong("metric_id"))
                    .isEqualTo(3L);
                testContext.completeNow();
            }),
            throwable -> testContext.verify(() -> fail("method has thrown exception"))
        );
    }

    @Test
    void getAllOptionalNotFound(VertxTestContext testContext) {
        long typeId = 1L;
        JsonObject rt1 = JsonObject.mapFrom(TestObjectProvider.createResourceType(typeId, "vm"));
        JsonObject m1 = JsonObject.mapFrom(TestObjectProvider.createMetric(1L, "cpu"));
        JsonObject m2 = JsonObject.mapFrom(TestObjectProvider.createMetric(2L, "memory"));
        JsonArray required = new JsonArray(List.of(m1, m2));

        when(rc.pathParam("id")).thenReturn(String.valueOf(typeId));
        when(resourceTypeChecker.checkFindOne(typeId)).thenReturn(Single.just(rt1));
        when(metricChecker.checkFindAllByResourceTypeId(typeId, true)).thenReturn(Single.just(required));
        when(metricChecker.checkFindAllByResourceTypeId(typeId, false))
            .thenReturn(Single.error(NotFoundException::new));

        handler.getAll(rc)
            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
            }));
    }

    @Test
    void getAllRequiredNotFound(VertxTestContext testContext) {
        long typeId = 1L;
        JsonObject rt1 = JsonObject.mapFrom(TestObjectProvider.createResourceType(typeId, "vm"));

        when(rc.pathParam("id")).thenReturn(String.valueOf(typeId));
        when(resourceTypeChecker.checkFindOne(typeId)).thenReturn(Single.just(rt1));
        when(metricChecker.checkFindAllByResourceTypeId(typeId, true))
            .thenReturn(Single.error(NotFoundException::new));

        handler.getAll(rc)
            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
            }));
    }

    @Test
    void getAllRequiredResourceTypeNotFound(VertxTestContext testContext) {
        long typeId = 1L;

        when(rc.pathParam("id")).thenReturn(String.valueOf(typeId));
        when(resourceTypeChecker.checkFindOne(typeId)).thenReturn(Single.error(NotFoundException::new));

        handler.getAll(rc)
            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
            }));
    }
}
