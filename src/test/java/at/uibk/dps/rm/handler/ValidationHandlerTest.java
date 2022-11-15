package at.uibk.dps.rm.handler;

import at.uibk.dps.rm.entity.model.ResourceType;
import at.uibk.dps.rm.exception.AlreadyExistsException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.handler.resource.ResourceTypeChecker;
import at.uibk.dps.rm.service.rxjava3.database.resource.ResourceTypeService;
import at.uibk.dps.rm.testutil.RoutingContextMockHelper;
import at.uibk.dps.rm.testutil.SingleHelper;
import io.reactivex.rxjava3.core.Completable;
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

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.*;

@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class ValidationHandlerTest {

    static class ConcreteValidationHandler extends ValidationHandler {
        protected ConcreteValidationHandler(EntityChecker entityChecker) {
            super(entityChecker);
        }
    }

    private ConcreteValidationHandler testClass;

    @Mock
    private ResourceTypeService resourceTypeService;

    @Mock
    RoutingContext rc;

    @BeforeEach
    void initTest() {
        ResourceTypeChecker entityChecker = new ResourceTypeChecker(resourceTypeService);
        testClass = new ConcreteValidationHandler(entityChecker);
    }

    @Test
    void deleteOneExists(VertxTestContext testContext) {
        long entityId = 1L;
        ResourceType entity = new ResourceType();
        entity.setTypeId(entityId);
        entity.setResource_type("cloud");

        when(rc.pathParam("id")).thenReturn(String.valueOf(entityId));
        when(resourceTypeService.findOne(entityId)).thenReturn(Single.just(JsonObject.mapFrom(entity)));
        when(resourceTypeService.delete(entityId)).thenReturn(Completable.complete());

        testClass.deleteOne(rc)
            .blockingSubscribe(() -> {},
                throwable -> testContext.verify(() -> fail("method did throw exception"))
            );

        verify(resourceTypeService).findOne(entityId);
        verify(resourceTypeService).delete(entityId);
        testContext.completeNow();
    }

    @Test
    void deleteOneNotFound(VertxTestContext testContext) {
        long entityId = 1L;
        Single<JsonObject> handler = new SingleHelper<JsonObject>().getEmptySingle();
        when(rc.pathParam("id")).thenReturn(String.valueOf(entityId));
        when(resourceTypeService.findOne(entityId)).thenReturn(handler);

        testClass.deleteOne(rc)
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void getOneExists(VertxTestContext testContext) {
        long entityId = 1L;
        ResourceType entity = new ResourceType();
        entity.setTypeId(entityId);
        entity.setResource_type("cloud");

        when(rc.pathParam("id")).thenReturn(String.valueOf(entityId));
        when(resourceTypeService.findOne(entityId)).thenReturn(Single.just(JsonObject.mapFrom(entity)));

        testClass.getOne(rc)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.getLong("type_id")).isEqualTo(entityId);
                    assertThat(result.getString("resource_type")).isEqualTo("cloud");
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method did throw exception"))
            );
    }

    @Test
    void getOneNotFound(VertxTestContext testContext) {
        long entityId = 1L;
        Single<JsonObject> handler = new SingleHelper<JsonObject>().getEmptySingle();

        when(rc.pathParam("id")).thenReturn(String.valueOf(entityId));
        when(resourceTypeService.findOne(entityId)).thenReturn(handler);

        testClass.getOne(rc)
            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void getAll(VertxTestContext testContext) {
        ResourceType entity1 = new ResourceType();
        entity1.setTypeId(1L);
        ResourceType entity2 = new ResourceType();
        entity2.setTypeId(2L);
        List<JsonObject> entities = new ArrayList<>();
        entities.add(JsonObject.mapFrom(entity1));
        entities.add(JsonObject.mapFrom(entity2));
        JsonArray resultJson = new JsonArray(entities);

        when(resourceTypeService.findAll()).thenReturn(Single.just(resultJson));

        testClass.getAll(rc)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.size()).isEqualTo(2);
                    assertThat(result.getJsonObject(0).getLong("type_id")).isEqualTo(1L);
                    assertThat(result.getJsonObject(1).getLong("type_id")).isEqualTo(2L);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method did throw exception"))
            );
    }

    @Test
    void getAllEmpty(VertxTestContext testContext) {
        List<JsonObject> entities = new ArrayList<>();
        JsonArray resultJson = new JsonArray(entities);

        when(resourceTypeService.findAll()).thenReturn(Single.just(resultJson));

        testClass.getAll(rc)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.size()).isEqualTo(0);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method did throw exception"))
            );
    }

    @Test
    void postOne(VertxTestContext testContext) {
        ResourceType entity = new ResourceType();
        entity.setTypeId(1L);
        entity.setResource_type("cloud");
        JsonObject jsonObject = JsonObject.mapFrom(entity);

        RoutingContextMockHelper.mockBody(rc, jsonObject);
        when(resourceTypeService.existsOneByResourceType("cloud")).thenReturn(Single.just(false));
        when(resourceTypeService.save(jsonObject)).thenReturn(Single.just(jsonObject));

        testClass.postOne(rc)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.getLong("type_id")).isEqualTo(1L);
                    assertThat(result.getString("resource_type")).isEqualTo("cloud");
                    verify(resourceTypeService).save(jsonObject);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method did throw exception"))
            );
    }

    @Test
    void postOneAlreadyExists(VertxTestContext testContext) {
        ResourceType entity = new ResourceType();
        entity.setResource_type("cloud");
        JsonObject jsonObject = JsonObject.mapFrom(entity);

        RoutingContextMockHelper.mockBody(rc, jsonObject);
        when(resourceTypeService.existsOneByResourceType("cloud")).thenReturn(Single.just(true));

        testClass.postOne(rc)
            .subscribe(result -> testContext.verify(() -> fail("method did throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(AlreadyExistsException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void postAll(VertxTestContext testContext) {
        ResourceType entity1 = new ResourceType();
        ResourceType entity2 = new ResourceType();
        List<JsonObject> entities = new ArrayList<>();
        entities.add(JsonObject.mapFrom(entity1));
        entities.add(JsonObject.mapFrom(entity2));
        JsonArray resultJson = new JsonArray(entities);

        RoutingContextMockHelper.mockBody(rc, resultJson);
        when(resourceTypeService.saveAll(resultJson)).thenReturn(Completable.complete());

        testClass.postAll(rc)
            .blockingSubscribe(() -> {},
                throwable -> testContext.verify(() -> fail("method did throw exception"))
            );

        verify(resourceTypeService).saveAll(resultJson);
        testContext.completeNow();
    }

    @Test
    void updateOneExists(VertxTestContext testContext) {
        long entityId = 1L;
        ResourceType entity = new ResourceType();
        entity.setTypeId(entityId);
        entity.setResource_type("cloud");
        JsonObject jsonObject = JsonObject.mapFrom(entity);

        RoutingContextMockHelper.mockBody(rc, jsonObject);
        when(rc.pathParam("id")).thenReturn(String.valueOf(entityId));
        when(resourceTypeService.findOne(entityId)).thenReturn(Single.just(jsonObject));
        when(resourceTypeService.existsOneByResourceType("cloud")).thenReturn(Single.just(false));
        when(resourceTypeService.update(jsonObject)).thenReturn(Completable.complete());

        testClass.updateOne(rc)
            .blockingSubscribe(() -> {},
                throwable -> testContext.verify(() -> fail("method did throw exception"))
            );

        verify(resourceTypeService).findOne(entityId);
        verify(resourceTypeService).existsOneByResourceType("cloud");
        verify(resourceTypeService).update(jsonObject);
        testContext.completeNow();
    }

    @Test
    void updateOneNotFound(VertxTestContext testContext) {
        long entityId = 1L;
        Single<JsonObject> handler = new SingleHelper<JsonObject>().getEmptySingle();
        ResourceType entity = new ResourceType();
        entity.setTypeId(entityId);
        entity.setResource_type("cloud");
        JsonObject jsonObject = JsonObject.mapFrom(entity);

        RoutingContextMockHelper.mockBody(rc, jsonObject);
        when(rc.pathParam("id")).thenReturn(String.valueOf(entityId));
        when(resourceTypeService.findOne(entityId)).thenReturn(handler);

        testClass.updateOne(rc)
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void updateOneAlreadyExists(VertxTestContext testContext) {
        long entityId = 1L;
        ResourceType entity = new ResourceType();
        entity.setTypeId(entityId);
        entity.setResource_type("cloud");
        JsonObject jsonObject = JsonObject.mapFrom(entity);

        RoutingContextMockHelper.mockBody(rc, jsonObject);
        when(rc.pathParam("id")).thenReturn(String.valueOf(entityId));
        when(resourceTypeService.findOne(entityId)).thenReturn(Single.just(jsonObject));
        when(resourceTypeService.existsOneByResourceType("cloud")).thenReturn(Single.just(true));

        testClass.updateOne(rc)
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(AlreadyExistsException.class);
                    testContext.completeNow();
                })
            );
    }
}
