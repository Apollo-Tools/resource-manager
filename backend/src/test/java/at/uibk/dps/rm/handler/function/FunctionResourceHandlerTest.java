package at.uibk.dps.rm.handler.function;

import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.exception.AlreadyExistsException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.handler.resource.ResourceChecker;
import at.uibk.dps.rm.testutil.RoutingContextMockHelper;
import at.uibk.dps.rm.testutil.objectprovider.TestFunctionProvider;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Implements tests for the {@link FunctionResourceHandler} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class FunctionResourceHandlerTest {

    private FunctionResourceHandler functionResourceHandler;

    @Mock
    private FunctionResourceChecker functionResourceChecker;

    @Mock
    private FunctionChecker functionChecker;

    @Mock
    private ResourceChecker resourceChecker;

    @Mock
    private RoutingContext rc;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        functionResourceHandler = new FunctionResourceHandler(functionResourceChecker, functionChecker,
            resourceChecker);
    }

    @Test
    void getAllValid(VertxTestContext testContext) {
        long functionId = 1L;
        Function function = TestFunctionProvider.createFunction(functionId, "func","true");
        FunctionResource fr1 = TestFunctionProvider.createFunctionResource(1L, function, true);
        FunctionResource fr2 = TestFunctionProvider.createFunctionResource(2L, function, true);
        FunctionResource fr3 = TestFunctionProvider.createFunctionResource(3L, function, false);
        JsonArray functionResources = new JsonArray(List.of(JsonObject.mapFrom(fr1),
            JsonObject.mapFrom(fr2), JsonObject.mapFrom(fr3)));

        when(rc.pathParam("id")).thenReturn(String.valueOf(functionId));
        when(functionChecker.checkExistsOne(functionId)).thenReturn(Completable.complete());
        when(resourceChecker.checkFindAllByFunction(functionId)).thenReturn(Single.just(functionResources));

        functionResourceHandler.getAll(rc)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.size()).isEqualTo(3);
                    assertThat(result.getJsonObject(0).getLong("function_resource_id")).isEqualTo(1L);
                    assertThat(result.getJsonObject(1).getLong("function_resource_id")).isEqualTo(2L);
                    assertThat(result.getJsonObject(2).getLong("function_resource_id")).isEqualTo(3L);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void getAllFunctionNotFound(VertxTestContext testContext) {
        long functionId = 1L;

        when(rc.pathParam("id")).thenReturn(String.valueOf(functionId));
        when(functionChecker.checkExistsOne(functionId)).thenReturn(Completable.error(NotFoundException::new));

        functionResourceHandler.getAll(rc)
            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void getAllFunctionNoResources(VertxTestContext testContext) {
        long functionId = 1L;
        JsonArray functionResources = new JsonArray();

        when(rc.pathParam("id")).thenReturn(String.valueOf(functionId));
        when(functionChecker.checkExistsOne(functionId)).thenReturn(Completable.complete());
        when(resourceChecker.checkFindAllByFunction(functionId)).thenReturn(Single.just(functionResources));

        functionResourceHandler.getAll(rc)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.size()).isEqualTo(0);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void getAllFunctionResourcesNotFound(VertxTestContext testContext) {
        long functionId = 1L;

        when(rc.pathParam("id")).thenReturn(String.valueOf(functionId));
        when(functionChecker.checkExistsOne(functionId)).thenReturn(Completable.complete());
        when(resourceChecker.checkFindAllByFunction(functionId)).thenReturn(Single.error(NotFoundException::new));

        functionResourceHandler.getAll(rc)
            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void postAllValid(VertxTestContext testContext) {
        long functionId = 1L;
        JsonObject value1 = new JsonObject("{\"resource_id\": 1}");
        JsonObject value2 = new JsonObject("{\"resource_id\": 2}");
        JsonObject value3 = new JsonObject("{\"resource_id\": 3}");
        JsonArray requestBody = new JsonArray(List.of(value1, value2, value3));

        RoutingContextMockHelper.mockBody(rc, requestBody);
        when(rc.pathParam("id")).thenReturn(String.valueOf(functionId));
        when(rc.pathParam("id")).thenReturn(String.valueOf(functionId));
        when(functionChecker.checkExistsOne(functionId)).thenReturn(Completable.complete());
        when(resourceChecker.checkExistsOne(anyLong())).thenReturn(Completable.complete());
        when(functionResourceChecker.checkForDuplicateByFunctionAndResource(eq(functionId), anyLong()))
            .thenReturn(Completable.complete());

        when(functionResourceChecker.submitCreateAll(any(JsonArray.class))).thenReturn(Completable.complete());

        functionResourceHandler.postAll(rc)
            .blockingSubscribe(() -> {},
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
        testContext.completeNow();
    }

    @Test
    void postAllFunctionNotFound(VertxTestContext testContext) {
        long functionId = 1L;
        JsonObject value = new JsonObject("{\"resource_id\": 1}");
        JsonArray requestBody = new JsonArray(List.of(value));


        RoutingContextMockHelper.mockBody(rc, requestBody);
        when(rc.pathParam("id")).thenReturn(String.valueOf(functionId));
        when(functionChecker.checkExistsOne(functionId)).thenReturn(Completable.error(NotFoundException::new));

        functionResourceHandler.postAll(rc)
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void postAllResourceNotFound(VertxTestContext testContext) {
        long functionId = 1L;
        JsonObject value = new JsonObject("{\"resource_id\": 1}");
        JsonArray requestBody = new JsonArray(List.of(value));

        RoutingContextMockHelper.mockBody(rc, requestBody);
        when(rc.pathParam("id")).thenReturn(String.valueOf(functionId));
        when(functionChecker.checkExistsOne(functionId)).thenReturn(Completable.complete());
        when(resourceChecker.checkExistsOne(anyLong())).thenReturn(Completable.error(NotFoundException::new));
        when(functionResourceChecker.checkForDuplicateByFunctionAndResource(eq(functionId), anyLong()))
            .thenReturn(Completable.complete());

        functionResourceHandler.postAll(rc)
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void postAllAlreadyExists(VertxTestContext testContext) {
        long functionId = 1L;
        JsonObject value = new JsonObject("{\"resource_id\": 1}");
        JsonArray requestBody = new JsonArray(List.of(value));

        RoutingContextMockHelper.mockBody(rc, requestBody);
        when(rc.pathParam("id")).thenReturn(String.valueOf(functionId));
        when(functionChecker.checkExistsOne(functionId)).thenReturn(Completable.complete());
        when(resourceChecker.checkExistsOne(anyLong())).thenReturn(Completable.complete());
        when(functionResourceChecker.checkForDuplicateByFunctionAndResource(eq(functionId), anyLong()))
            .thenReturn(Completable.error(AlreadyExistsException::new));


        functionResourceHandler.postAll(rc)
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(AlreadyExistsException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void deleteOneValid(VertxTestContext testContext) {
        long functionId = 1L;
        long resourceId = 1L;

        when(rc.pathParam("functionId")).thenReturn(String.valueOf(functionId));
        when(rc.pathParam("resourceId")).thenReturn(String.valueOf(resourceId));
        when(resourceChecker.checkExistsOne(resourceId)).thenReturn(Completable.complete());
        when(functionChecker.checkExistsOne(functionId)).thenReturn(Completable.complete());
        when(functionResourceChecker.checkExistsByFunctionAndResource(functionId, resourceId))
            .thenReturn(Completable.complete());
        when(functionResourceChecker.submitDeleteFunctionResource(functionId, resourceId))
            .thenReturn(Completable.complete());

        functionResourceHandler.deleteOne(rc)
            .blockingSubscribe(() -> {},
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
        testContext.completeNow();
    }

    @Test
    void deleteOneResourceNotFound(VertxTestContext testContext) {
        long functionId = 1L;
        long resourceId = 1L;

        when(rc.pathParam("functionId")).thenReturn(String.valueOf(functionId));
        when(rc.pathParam("resourceId")).thenReturn(String.valueOf(resourceId));
        when(resourceChecker.checkExistsOne(resourceId)).thenReturn(Completable.error(NotFoundException::new));
        when(functionChecker.checkExistsOne(functionId)).thenReturn(Completable.complete());
        when(functionResourceChecker.checkExistsByFunctionAndResource(functionId, resourceId))
            .thenReturn(Completable.complete());

        functionResourceHandler.deleteOne(rc)
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void deleteOneFunctionNotFound(VertxTestContext testContext) {
        long functionId = 1L;
        long resourceId = 1L;

        when(rc.pathParam("functionId")).thenReturn(String.valueOf(functionId));
        when(rc.pathParam("resourceId")).thenReturn(String.valueOf(resourceId));
        when(resourceChecker.checkExistsOne(resourceId)).thenReturn(Completable.complete());
        when(functionChecker.checkExistsOne(functionId)).thenReturn(Completable.error(NotFoundException::new));
        when(functionResourceChecker.checkExistsByFunctionAndResource(functionId, resourceId))
            .thenReturn(Completable.complete());

        functionResourceHandler.deleteOne(rc)
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void deleteOneFunctionResourceNotFound(VertxTestContext testContext) {
        long functionId = 1L;
        long resourceId = 1L;

        when(rc.pathParam("functionId")).thenReturn(String.valueOf(functionId));
        when(rc.pathParam("resourceId")).thenReturn(String.valueOf(resourceId));
        when(resourceChecker.checkExistsOne(resourceId)).thenReturn(Completable.complete());
        when(functionChecker.checkExistsOne(functionId)).thenReturn(Completable.complete());
        when(functionResourceChecker.checkExistsByFunctionAndResource(functionId, resourceId))
            .thenReturn(Completable.error(NotFoundException::new));

        functionResourceHandler.deleteOne(rc)
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void checkAddResourcesExistValid(VertxTestContext testContext) {
        long functionId = 1L;
        JsonObject value1 = new JsonObject("{\"resource_id\": 1}");
        JsonObject value2 = new JsonObject("{\"resource_id\": 2}");
        JsonObject value3 = new JsonObject("{\"resource_id\": 3}");
        JsonArray requestBody = new JsonArray(List.of(value1, value2, value3));

        when(resourceChecker.checkExistsOne(anyLong())).thenReturn(Completable.complete());
        when(functionResourceChecker.checkForDuplicateByFunctionAndResource(eq(functionId), anyLong()))
            .thenReturn(Completable.complete());

        functionResourceHandler.checkAddResourcesExist(requestBody, functionId)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.size()).isEqualTo(3);
                    assertThat(result.get(0).getResource().getResourceId()).isEqualTo(1L);
                    assertThat(result.get(1).getResource().getResourceId()).isEqualTo(2L);
                    assertThat(result.get(2).getResource().getResourceId()).isEqualTo(3L);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void checkAddResourcesExistResourceNotExists(VertxTestContext testContext) {
        long functionId = 1L;
        JsonObject value = new JsonObject("{\"resource_id\": 1}");
        JsonArray requestBody = new JsonArray(List.of(value));

        when(resourceChecker.checkExistsOne(anyLong())).thenReturn(Completable.error(NotFoundException::new));
        when(functionResourceChecker.checkForDuplicateByFunctionAndResource(eq(functionId), anyLong()))
            .thenReturn(Completable.complete());

        functionResourceHandler.checkAddResourcesExist(requestBody, functionId)
            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void checkAddResourcesExistDuplicated(VertxTestContext testContext) {
        long functionId = 1L;
        JsonObject value = new JsonObject("{\"resource_id\": 1}");
        JsonArray requestBody = new JsonArray(List.of(value));

        when(resourceChecker.checkExistsOne(anyLong())).thenReturn(Completable.complete());
        when(functionResourceChecker.checkForDuplicateByFunctionAndResource(eq(functionId), anyLong()))
            .thenReturn(Completable.error(AlreadyExistsException::new));

        functionResourceHandler.checkAddResourcesExist(requestBody, functionId)
            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(AlreadyExistsException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void checkDeleteFunctionResourceExistsAllExist(VertxTestContext testContext) {
        long functionId = 1L;
        long resourceId = 1L;

        when(resourceChecker.checkExistsOne(resourceId)).thenReturn(Completable.complete());
        when(functionChecker.checkExistsOne(functionId)).thenReturn(Completable.complete());
        when(functionResourceChecker.checkExistsByFunctionAndResource(functionId, resourceId))
            .thenReturn(Completable.complete());

        functionResourceHandler.checkFunctionResourceExists(functionId, resourceId)
            .blockingSubscribe(() -> {},
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
        testContext.completeNow();
    }

    @Test
    void checkDeleteFunctionResourceExistsResourceNotExists(VertxTestContext testContext) {
        long functionId = 1L;
        long resourceId = 1L;

        when(resourceChecker.checkExistsOne(resourceId)).thenReturn(Completable.error(NotFoundException::new));
        when(functionChecker.checkExistsOne(functionId)).thenReturn(Completable.complete());
        when(functionResourceChecker.checkExistsByFunctionAndResource(functionId, resourceId))
            .thenReturn(Completable.complete());

        functionResourceHandler.checkFunctionResourceExists(functionId, resourceId)
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void checkDeleteFunctionResourceExistsFunctionNotExists(VertxTestContext testContext) {
        long functionId = 1L;
        long resourceId = 1L;

        when(resourceChecker.checkExistsOne(resourceId)).thenReturn(Completable.complete());
        when(functionChecker.checkExistsOne(functionId)).thenReturn(Completable.error(NotFoundException::new));
        when(functionResourceChecker.checkExistsByFunctionAndResource(functionId, resourceId))
            .thenReturn(Completable.complete());

        functionResourceHandler.checkFunctionResourceExists(functionId, resourceId)
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void checkDeleteFunctionResourceExistsFunctionResourceNotExists(VertxTestContext testContext) {
        long functionId = 1L;
        long resourceId = 1L;

        when(resourceChecker.checkExistsOne(resourceId)).thenReturn(Completable.complete());
        when(functionChecker.checkExistsOne(functionId)).thenReturn(Completable.complete());
        when(functionResourceChecker.checkExistsByFunctionAndResource(functionId, resourceId))
            .thenReturn(Completable.error(NotFoundException::new));

        functionResourceHandler.checkFunctionResourceExists(functionId, resourceId)
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }

}
