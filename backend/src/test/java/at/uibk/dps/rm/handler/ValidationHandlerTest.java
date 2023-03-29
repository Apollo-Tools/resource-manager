package at.uibk.dps.rm.handler;

import at.uibk.dps.rm.entity.model.ResourceType;
import at.uibk.dps.rm.exception.AlreadyExistsException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.handler.resource.ResourceTypeChecker;
import at.uibk.dps.rm.testutil.RoutingContextMockHelper;
import at.uibk.dps.rm.testutil.TestObjectProvider;
import at.uibk.dps.rm.util.JsonMapperConfig;
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
import static org.mockito.Mockito.when;

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
    private ResourceTypeChecker resourceTypeChecker;

    @Mock
    RoutingContext rc;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        testClass = new ConcreteValidationHandler(resourceTypeChecker);
    }

    @Test
    void deleteOneExists(VertxTestContext testContext) {
        long entityId = 1L;
        ResourceType entity = new ResourceType();
        entity.setTypeId(entityId);
        entity.setResourceType("cloud");

        when(rc.pathParam("id")).thenReturn(String.valueOf(entityId));
        when(resourceTypeChecker.checkFindOne(entityId)).thenReturn(Single.just(JsonObject.mapFrom(entity)));
        when(resourceTypeChecker.submitDelete(entityId)).thenReturn(Completable.complete());

        testClass.deleteOne(rc)
            .blockingSubscribe(() -> {},
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
        testContext.completeNow();
    }

    @Test
    void deleteOneNotFound(VertxTestContext testContext) {
        long entityId = 1L;

        when(rc.pathParam("id")).thenReturn(String.valueOf(entityId));
        when(resourceTypeChecker.checkFindOne(entityId)).thenReturn(Single.error(NotFoundException::new));

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
        entity.setResourceType("cloud");

        when(rc.pathParam("id")).thenReturn(String.valueOf(entityId));
        when(resourceTypeChecker.checkFindOne(entityId)).thenReturn(Single.just(JsonObject.mapFrom(entity)));

        testClass.getOne(rc)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.getLong("type_id")).isEqualTo(entityId);
                    assertThat(result.getString("resource_type")).isEqualTo("cloud");
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void getOneNotFound(VertxTestContext testContext) {
        long entityId = 1L;

        when(rc.pathParam("id")).thenReturn(String.valueOf(entityId));
        when(resourceTypeChecker.checkFindOne(entityId)).thenReturn(Single.error(NotFoundException::new));

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
        ResourceType entity1 = TestObjectProvider.createResourceType(1L, "faas");
        ResourceType entity2 = TestObjectProvider.createResourceType(2L, "vm");
        List<JsonObject> entities = new ArrayList<>();
        entities.add(JsonObject.mapFrom(entity1));
        entities.add(JsonObject.mapFrom(entity2));
        JsonArray resultJson = new JsonArray(entities);

        when(resourceTypeChecker.checkFindAll()).thenReturn(Single.just(resultJson));

        testClass.getAll(rc)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.size()).isEqualTo(2);
                    assertThat(result.getJsonObject(0).getLong("type_id")).isEqualTo(1L);
                    assertThat(result.getJsonObject(1).getLong("type_id")).isEqualTo(2L);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void getAllEmpty(VertxTestContext testContext) {
        List<JsonObject> entities = new ArrayList<>();
        JsonArray resultJson = new JsonArray(entities);

        when(resourceTypeChecker.checkFindAll()).thenReturn(Single.just(resultJson));

        testClass.getAll(rc)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.size()).isEqualTo(0);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void postOne(VertxTestContext testContext) {
        JsonObject requestBody = JsonObject.mapFrom(TestObjectProvider.createResourceType(1L, "vm"));

        RoutingContextMockHelper.mockBody(rc, requestBody);
        when(resourceTypeChecker.checkForDuplicateEntity(requestBody)).thenReturn(Completable.complete());
        when(resourceTypeChecker.submitCreate(requestBody)).thenReturn(Single.just(requestBody));

        testClass.postOne(rc)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.getLong("type_id")).isEqualTo(1L);
                    assertThat(result.getString("resource_type")).isEqualTo("vm");
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void postOneAlreadyExists(VertxTestContext testContext) {
        JsonObject requestBody = JsonObject.mapFrom(TestObjectProvider.createResourceType(1L, "vm"));

        RoutingContextMockHelper.mockBody(rc, requestBody);
        when(resourceTypeChecker.checkForDuplicateEntity(requestBody))
            .thenReturn(Completable.error(AlreadyExistsException::new));

        testClass.postOne(rc)
            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
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
        when(resourceTypeChecker.submitCreateAll(resultJson)).thenReturn(Completable.complete());

        testClass.postAll(rc)
            .blockingSubscribe(() -> {},
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
        testContext.completeNow();
    }

    @Test
    void updateOneExists(VertxTestContext testContext) {
        long entityId = 1L;
        JsonObject requestBody = JsonObject.mapFrom(TestObjectProvider.createResourceType(1L, "vm"));
        JsonObject result = JsonObject.mapFrom(TestObjectProvider.createResourceType(1L, "edge"));

        RoutingContextMockHelper.mockBody(rc, requestBody);
        when(rc.pathParam("id")).thenReturn(String.valueOf(entityId));
        when(resourceTypeChecker.checkFindOne(entityId)).thenReturn(Single.just(result));
        when(resourceTypeChecker.checkUpdateNoDuplicate(requestBody, result)).thenReturn(Single.just(result));
        when(resourceTypeChecker.submitUpdate(requestBody, result)).thenReturn(Completable.complete());

        testClass.updateOne(rc)
            .blockingSubscribe(() -> {},
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
        testContext.completeNow();
    }

    @Test
    void updateOneNotFound(VertxTestContext testContext) {
        long entityId = 1L;
        JsonObject requestBody = JsonObject.mapFrom(TestObjectProvider.createResourceType(1L, "vm"));


        RoutingContextMockHelper.mockBody(rc, requestBody);
        when(rc.pathParam("id")).thenReturn(String.valueOf(entityId));
        when(resourceTypeChecker.checkFindOne(entityId)).thenReturn(Single.error(NotFoundException::new));

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
        JsonObject requestBody = JsonObject.mapFrom(TestObjectProvider.createResourceType(1L, "vm"));
        JsonObject result = JsonObject.mapFrom(TestObjectProvider.createResourceType(1L, "edge"));

        RoutingContextMockHelper.mockBody(rc, requestBody);
        when(rc.pathParam("id")).thenReturn(String.valueOf(entityId));
        when(resourceTypeChecker.checkFindOne(entityId)).thenReturn(Single.just(result));
        when(resourceTypeChecker.checkUpdateNoDuplicate(requestBody, result))
            .thenReturn(Single.error(AlreadyExistsException::new));

        testClass.updateOne(rc)
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(AlreadyExistsException.class);
                    testContext.completeNow();
                })
            );
    }
}
