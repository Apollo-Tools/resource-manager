package at.uibk.dps.rm.handler.resource;

import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.entity.model.ResourceType;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.testutil.RoutingContextMockHelper;
import at.uibk.dps.rm.testutil.objectprovider.TestResourceProvider;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.reactivex.rxjava3.core.Completable;
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
import static org.mockito.Mockito.when;

@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class ResourceHandlerTest {

    private ResourceHandler resourceHandler;

    @Mock
    private ResourceChecker resourceChecker;

    @Mock
    private ResourceTypeChecker resourceTypeChecker;

    @Mock
    private RoutingContext rc;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        resourceHandler = new ResourceHandler(resourceChecker, resourceTypeChecker);
    }

    @Test
    void postOneValid(VertxTestContext testContext) {
        ResourceType rt = TestResourceProvider.createResourceType(1L, "vm");
        Resource resource = TestResourceProvider.createResource(1L, rt);
        JsonObject requestBody = JsonObject.mapFrom(resource);
        requestBody.remove("resource_id");

        RoutingContextMockHelper.mockBody(rc, requestBody);
        when(resourceTypeChecker.checkExistsOne(1L)).thenReturn(Completable.complete());
        when(resourceChecker.submitCreate(requestBody)).thenReturn(Single.just(JsonObject.mapFrom(resource)));

        resourceHandler.postOne(rc)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.getJsonObject("resource_type").getLong("type_id")).isEqualTo(1L);
                    assertThat(result.getBoolean("is_self_managed")).isEqualTo(false);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void postOneResourceTypeNotFound(VertxTestContext testContext) {
        JsonObject jsonObject = new JsonObject("{\"resource_type\": {\"type_id\": 1}, \"is_self_managed\": false}");

        RoutingContextMockHelper.mockBody(rc, jsonObject);
        when(resourceTypeChecker.checkExistsOne(1L)).thenReturn(Completable.error(NotFoundException::new));

        resourceHandler.postOne(rc)
            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
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
        JsonObject r1 = JsonObject.mapFrom(TestResourceProvider.createResource(1L));
        JsonObject requestBody = new JsonObject(jsonInput);

        RoutingContextMockHelper.mockBody(rc, requestBody);
        when(rc.pathParam("id")).thenReturn(String.valueOf(entityId));
        when(resourceChecker.checkFindOne(entityId)).thenReturn(Single.just(r1));
        if (requestBody.containsKey("resource_type")) {
            when(resourceTypeChecker.checkExistsOne(1L)).thenReturn(Completable.complete());
        }
        when(resourceChecker.submitUpdate(requestBody, r1)).thenReturn(Completable.complete());

        resourceHandler.updateOne(rc)
            .blockingSubscribe(() -> {},
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
        testContext.completeNow();
    }

    @Test
    void updateOneEntityNotFound(VertxTestContext testContext) {
        long entityId = 1L;
        JsonObject requestBody = new JsonObject("{\"resource_type\": {\"type_id\": 1}, \"is_self_managed\": false}");

        RoutingContextMockHelper.mockBody(rc, requestBody);
        when(rc.pathParam("id")).thenReturn(String.valueOf(entityId));
        when(resourceChecker.checkFindOne(entityId)).thenReturn(Single.error(NotFoundException::new));

        resourceHandler.updateOne(rc)
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void updateOneResourceTypeNotFound(VertxTestContext testContext) {
        long entityId = 1L;
        JsonObject r1 = JsonObject.mapFrom(TestResourceProvider.createResource(1L));
        JsonObject requestBody = new JsonObject("{\"resource_type\": {\"type_id\": 1}, \"is_self_managed\": false}");

        RoutingContextMockHelper.mockBody(rc, requestBody);
        when(rc.pathParam("id")).thenReturn(String.valueOf(entityId));
        when(resourceChecker.checkFindOne(entityId)).thenReturn(Single.just(r1));
        when(resourceTypeChecker.checkExistsOne(1L)).thenReturn(Completable.error(NotFoundException::new));

        resourceHandler.updateOne(rc)
           .blockingSubscribe(() -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void checkUpdateResourceTypeExists(VertxTestContext testContext) {
        long typeId = 1L;
        ResourceType rt = TestResourceProvider.createResourceType(typeId, "vm");
        Resource r1 = TestResourceProvider.createResource(1L, rt);
        JsonObject r1Json = JsonObject.mapFrom(r1);
        JsonObject requestBody = new JsonObject("{\"resource_type\": {\"type_id\": " + typeId + "}}");

        when(resourceTypeChecker.checkExistsOne(typeId)).thenReturn(Completable.complete());

        resourceHandler.checkUpdateResourceTypeExists(requestBody, r1Json)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.getLong("resource_id")).isEqualTo(1L);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void checkUpdateResourceTypeNotExists(VertxTestContext testContext) {
        long typeId = 1L;
        ResourceType rt = TestResourceProvider.createResourceType(typeId, "vm");
        Resource r1 = TestResourceProvider.createResource(1L, rt);
        JsonObject r1Json = JsonObject.mapFrom(r1);
        JsonObject requestBody = new JsonObject("{\"resource_type\": {\"type_id\": " + typeId + "}}");

        when(resourceTypeChecker.checkExistsOne(typeId)).thenReturn(Completable.error(NotFoundException::new));

        resourceHandler.checkUpdateResourceTypeExists(requestBody, r1Json)
            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void checkUpdateResourceTypeNotInRequestBody(VertxTestContext testContext) {
        long typeId = 1L;
        ResourceType rt = TestResourceProvider.createResourceType(typeId, "vm");
        Resource r1 = TestResourceProvider.createResource(1L, rt);
        JsonObject r1Json = JsonObject.mapFrom(r1);
        JsonObject requestBody = new JsonObject("{\"is_managed\": true}");

        resourceHandler.checkUpdateResourceTypeExists(requestBody, r1Json)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.getLong("resource_id")).isEqualTo(1L);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }
}
