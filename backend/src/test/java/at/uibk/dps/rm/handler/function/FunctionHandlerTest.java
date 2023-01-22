package at.uibk.dps.rm.handler.function;

import at.uibk.dps.rm.entity.model.Function;
import at.uibk.dps.rm.exception.AlreadyExistsException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.service.rxjava3.database.function.FunctionService;
import at.uibk.dps.rm.service.rxjava3.database.function.RuntimeService;
import at.uibk.dps.rm.testutil.RoutingContextMockHelper;
import at.uibk.dps.rm.testutil.SingleHelper;
import at.uibk.dps.rm.util.JsonMapperConfig;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class FunctionHandlerTest {

    private FunctionHandler functionHandler;

    @Mock
    private FunctionService functionService;

    @Mock
    private RuntimeService runtimeService;

    @Mock
    private RoutingContext rc;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        functionHandler = new FunctionHandler(functionService, runtimeService);
    }

    @Test
    void postOneValid(VertxTestContext testContext) {
        String name = "func";
        long runtimeId = 1L;
        JsonObject jsonObject = new JsonObject("{\"name\": \"" + name + "\", \"runtime\": {\"runtime_id\": "
            + runtimeId + "}, \"code\": \"x = 10\"}");

        RoutingContextMockHelper.mockBody(rc, jsonObject);
        when(runtimeService.existsOneById(1L)).thenReturn(Single.just(true));
        when(functionService.existsOneByNameAndRuntimeId(name, runtimeId)).thenReturn(Single.just(false));
        when(functionService.save(jsonObject)).thenReturn(Single.just(jsonObject));

        functionHandler.postOne(rc)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.getJsonObject("runtime").getLong("runtime_id")).isEqualTo(1L);
                    assertThat(result.getString("code")).isEqualTo("x = 10");
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method did throw exception"))
            );
    }

    @Test
    void postOneRuntimeNotFound(VertxTestContext testContext) {
        String name = "func";
        long runtimeId = 1L;
        JsonObject jsonObject = new JsonObject("{\"name\": \"" + name + "\", \"runtime\": {\"runtime_id\": "
            + runtimeId + "}, \"code\": \"x = 10\"}");

        RoutingContextMockHelper.mockBody(rc, jsonObject);
        when(runtimeService.existsOneById(1L)).thenReturn(Single.just(false));
        when(functionService.existsOneByNameAndRuntimeId(name, runtimeId)).thenReturn(Single.just(false));

        functionHandler.postOne(rc)
            .subscribe(result -> testContext.verify(() -> fail("method did throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void postOneAlreadyExists(VertxTestContext testContext) {
        String name = "func";
        long runtimeId = 1L;
        JsonObject jsonObject = new JsonObject("{\"name\": \"" + name + "\", \"runtime\": {\"runtime_id\": "
            + runtimeId + "}, \"code\": \"x = 10\"}");

        RoutingContextMockHelper.mockBody(rc, jsonObject);
        when(runtimeService.existsOneById(1L)).thenReturn(Single.just(true));
        when(functionService.existsOneByNameAndRuntimeId(name, runtimeId)).thenReturn(Single.just(true));

        functionHandler.postOne(rc)
            .subscribe(result -> testContext.verify(() -> fail("method did throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(AlreadyExistsException.class);
                    testContext.completeNow();
                })
            );
    }

    @ParameterizedTest
    @ValueSource(strings = {"{\"runtime\": {\"runtime_id\": 1}, \"code\": \"x = 10\"}",
        "{\"runtime\": {\"runtime_id\": 1}}",
        "{\"code\": \"x = 10\"}"})
    void updateOneValid(String jsonInput, VertxTestContext testContext) {
        long entityId = 1L;
        JsonObject jsonObject = new JsonObject(jsonInput);

        RoutingContextMockHelper.mockBody(rc, jsonObject);
        when(rc.pathParam("id")).thenReturn(String.valueOf(entityId));
        when(functionService.findOne(entityId)).thenReturn(Single.just(jsonObject));
        if (jsonObject.containsKey("runtime")) {
            when(runtimeService.existsOneById(1L)).thenReturn(Single.just(true));
        }
        when(functionService.update(jsonObject)).thenReturn(Completable.complete());

        functionHandler.updateOne(rc)
            .blockingSubscribe(() -> {},
                throwable -> testContext.verify(() -> fail("method did throw exception"))
            );

        verify(functionService).findOne(entityId);
        if (jsonObject.containsKey("runtime")) {
            verify(runtimeService).existsOneById(1L);
        }
        verify(functionService).update(jsonObject);
        testContext.completeNow();
    }

    @Test
    void updateOneEntityNotFound(VertxTestContext testContext) {
        long entityId = 1L;
        Single<JsonObject> handler = new SingleHelper<JsonObject>().getEmptySingle();
        JsonObject jsonObject = new JsonObject("{\"runtime\": {\"runtime_id\": 1}, \"code\": \"x = 10\"}");

        RoutingContextMockHelper.mockBody(rc, jsonObject);
        when(rc.pathParam("id")).thenReturn(String.valueOf(entityId));
        when(functionService.findOne(entityId)).thenReturn(handler);

        functionHandler.updateOne(rc)
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void updateOneRuntimeNotFound(VertxTestContext testContext) {
        long entityId = 1L;
        JsonObject jsonObject = new JsonObject("{\"runtime\": {\"runtime_id\": 1}, \"code\": \"x = 10\"}");

        RoutingContextMockHelper.mockBody(rc, jsonObject);
        when(rc.pathParam("id")).thenReturn(String.valueOf(entityId));
        when(functionService.findOne(entityId)).thenReturn(Single.just(jsonObject));
        when(runtimeService.existsOneById(1L)).thenReturn(Single.just(false));

        functionHandler.updateOne(rc)
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void checkUpdateRuntimeExists(VertxTestContext testContext) {
        long runtimeId = 1L;
        Function entity = new Function();
        entity.setFunctionId(1L);
        JsonObject entityJson = JsonObject.mapFrom(entity);
        JsonObject requestBody = new JsonObject("{\"runtime\": {\"runtime_id\": " + runtimeId + "}}");

        when(runtimeService.existsOneById(runtimeId)).thenReturn(Single.just(true));

        functionHandler.checkUpdateRuntimeExists(requestBody, entityJson)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.getLong("function_id")).isEqualTo(1L);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method did throw exception"))
            );
    }

    @Test
    void checkUpdateResourceTypeNotExists(VertxTestContext testContext) {
        long runtimeId = 1L;
        Function entity = new Function();
        entity.setFunctionId(1L);
        JsonObject entityJson = JsonObject.mapFrom(entity);
        JsonObject requestBody = new JsonObject("{\"runtime\": {\"runtime_id\": " + runtimeId + "}}");

        when(runtimeService.existsOneById(runtimeId)).thenReturn(Single.just(false));

        functionHandler.checkUpdateRuntimeExists(requestBody, entityJson)
            .subscribe(result -> testContext.verify(() -> fail("method did throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void checkUpdateResourceTypeNotInRequestBody(VertxTestContext testContext) {
        Function entity = new Function();
        entity.setFunctionId(1L);
        JsonObject entityJson = JsonObject.mapFrom(entity);
        JsonObject requestBody = new JsonObject("{\"code\": \"x = 10\"}");

        functionHandler.checkUpdateRuntimeExists(requestBody, entityJson)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.getLong("function_id")).isEqualTo(1L);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method did throw exception"))
            );
    }
}
