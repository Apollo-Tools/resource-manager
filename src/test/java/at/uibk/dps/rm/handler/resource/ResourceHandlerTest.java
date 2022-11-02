package at.uibk.dps.rm.handler.resource;

import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.service.rxjava3.database.metric.MetricService;
import at.uibk.dps.rm.service.rxjava3.database.metric.MetricValueService;
import at.uibk.dps.rm.service.rxjava3.database.resource.ResourceService;
import at.uibk.dps.rm.service.rxjava3.database.resource.ResourceTypeService;
import at.uibk.dps.rm.testutil.RoutingContextMockHelper;
import at.uibk.dps.rm.testutil.SingleHelper;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.*;

@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class ResourceHandlerTest {

    private ResourceHandler resourceHandler;

    @Mock
    private ResourceService resourceService;

    @Mock
    private ResourceTypeService resourceTypeService;

    @Mock
    private MetricService metricService;

    @Mock
    private MetricValueService metricValueService;

    @Mock
    private RoutingContext rc;

    @BeforeEach
    void initTest() {
        resourceHandler = new ResourceHandler(resourceService, resourceTypeService, metricService, metricValueService);
    }

    @Test
    void postOneValid(VertxTestContext testContext) {
        JsonObject jsonObject = new JsonObject("{\"resource_type\": {\"type_id\": 1}, \"is_self_managed\": false}");

        RoutingContextMockHelper.mockBody(rc, jsonObject);
        when(resourceTypeService.existsOneById(1L)).thenReturn(Single.just(true));
        when(resourceService.save(jsonObject)).thenReturn(Single.just(jsonObject));

        resourceHandler.postOne(rc)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.getJsonObject("resource_type").getLong("type_id")).isEqualTo(1L);
                    assertThat(result.getBoolean("is_self_managed")).isEqualTo(false);
                    verify(resourceTypeService).existsOneById(1L);
                    verify(resourceService).save(jsonObject);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method did throw exception"))
            );
    }

    @Test
    void postOneResourceTypeNotFound(VertxTestContext testContext) {
        JsonObject jsonObject = new JsonObject("{\"resource_type\": {\"type_id\": 1}, \"is_self_managed\": false}");

        RoutingContextMockHelper.mockBody(rc, jsonObject);
        when(resourceTypeService.existsOneById(1L)).thenReturn(Single.just(false));

        resourceHandler.postOne(rc)
            .subscribe(result -> testContext.verify(() -> fail("method did throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }

    @ParameterizedTest
    @ValueSource(strings = {"{\"resource_type\": {\"type_id\": 1}, \"is_self_managed\": false}",
        "{\"resource_type\": {\"type_id\": 1}}",
        "{\"is_self_managed\": false}"})
    void updateOneValid(String jsonInput, VertxTestContext testContext) {
        long entityId = 1L;
        JsonObject jsonObject = new JsonObject(jsonInput);

        RoutingContextMockHelper.mockBody(rc, jsonObject);
        when(rc.pathParam("id")).thenReturn(String.valueOf(entityId));
        when(resourceService.findOne(entityId)).thenReturn(Single.just(jsonObject));
        if (jsonObject.containsKey("resource_type")) {
            when(resourceTypeService.existsOneById(1L)).thenReturn(Single.just(true));
        }
        when(resourceService.update(jsonObject)).thenReturn(Completable.complete());

        resourceHandler.updateOne(rc)
            .andThen(Maybe.just(testContext.verify(testContext::completeNow)))
            .blockingSubscribe();

        verify(resourceService).findOne(entityId);
        if (jsonObject.containsKey("resource_type")) {
            verify(resourceTypeService).existsOneById(1L);
        }
        verify(resourceService).update(jsonObject);
    }

    @Test
    void updateOneEntityNotFound(VertxTestContext testContext) {
        long entityId = 1L;
        Single<JsonObject> handler = new SingleHelper<JsonObject>().getEmptySingle();
        JsonObject jsonObject = new JsonObject("{\"resource_type\": {\"type_id\": 1}, \"is_self_managed\": false}");

        RoutingContextMockHelper.mockBody(rc, jsonObject);
        when(rc.pathParam("id")).thenReturn(String.valueOf(entityId));
        when(resourceService.findOne(entityId)).thenReturn(handler);

        resourceHandler.updateOne(rc)
            .andThen(Maybe.just(testContext.verify(testContext::completeNow)))
            .blockingSubscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void updateOneResourceTypeNotFound(VertxTestContext testContext) {
        long entityId = 1L;
        Single<JsonObject> handler = new SingleHelper<JsonObject>().getEmptySingle();
        JsonObject jsonObject = new JsonObject("{\"resource_type\": {\"type_id\": 1}, \"is_self_managed\": false}");

        RoutingContextMockHelper.mockBody(rc, jsonObject);
        when(rc.pathParam("id")).thenReturn(String.valueOf(entityId));
        when(resourceService.findOne(entityId)).thenReturn(Single.just(jsonObject));
        when(resourceTypeService.existsOneById(1L)).thenReturn(Single.just(false));

        resourceHandler.updateOne(rc)
            .andThen(Maybe.just(testContext.verify(testContext::completeNow)))
            .blockingSubscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }

    //TODO: getResourceBySLOs
}
