package at.uibk.dps.rm.handler.function;

import at.uibk.dps.rm.entity.model.Function;
import at.uibk.dps.rm.exception.AlreadyExistsException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.testutil.RoutingContextMockHelper;
import at.uibk.dps.rm.testutil.objectprovider.TestFunctionProvider;
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
import static org.mockito.Mockito.when;

@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class FunctionHandlerTest {

    private FunctionHandler functionHandler;

    @Mock
    private FunctionChecker functionChecker;

    @Mock
    private RuntimeChecker runtimeChecker;

    @Mock
    private RoutingContext rc;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        functionHandler = new FunctionHandler(functionChecker, runtimeChecker);
    }

    @Test
    void postOneValid(VertxTestContext testContext) {
        String name = "func";
        long runtimeId = 1L;
        JsonObject requestBody = new JsonObject("{\"name\": \"" + name + "\", \"runtime\": {\"runtime_id\": "
            + runtimeId + "}, \"code\": \"x = 10\"}");

        RoutingContextMockHelper.mockBody(rc, requestBody);
        when(runtimeChecker.checkExistsOne(runtimeId)).thenReturn(Completable.complete());
        when(functionChecker.checkForDuplicateEntity(requestBody)).thenReturn(Completable.complete());
        when(functionChecker.submitCreate(requestBody)).thenReturn(Single.just(requestBody));

        functionHandler.postOne(rc)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.getJsonObject("runtime").getLong("runtime_id")).isEqualTo(1L);
                    assertThat(result.getString("code")).isEqualTo("x = 10");
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void postOneRuntimeNotFound(VertxTestContext testContext) {
        String name = "func";
        long runtimeId = 1L;
        JsonObject requestBody = new JsonObject("{\"name\": \"" + name + "\", \"runtime\": {\"runtime_id\": "
            + runtimeId + "}, \"code\": \"x = 10\"}");

        RoutingContextMockHelper.mockBody(rc, requestBody);
        when(runtimeChecker.checkExistsOne(runtimeId)).thenReturn(Completable.error(NotFoundException::new));
        when(functionChecker.checkForDuplicateEntity(requestBody)).thenReturn(Completable.complete());

        functionHandler.postOne(rc)
            .subscribe(result -> testContext.verify(() -> fail("method has thrown exception")),
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
        JsonObject requestBody = new JsonObject("{\"name\": \"" + name + "\", \"runtime\": {\"runtime_id\": "
            + runtimeId + "}, \"code\": \"x = 10\"}");

        RoutingContextMockHelper.mockBody(rc, requestBody);
        when(runtimeChecker.checkExistsOne(runtimeId)).thenReturn(Completable.complete());
        when(functionChecker.checkForDuplicateEntity(requestBody))
            .thenReturn(Completable.error(AlreadyExistsException::new));

        functionHandler.postOne(rc)
            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
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
        JsonObject f1 = JsonObject.mapFrom(TestFunctionProvider.createFunction(1L));
        JsonObject requestBody = new JsonObject(jsonInput);

        RoutingContextMockHelper.mockBody(rc, requestBody);
        when(rc.pathParam("id")).thenReturn(String.valueOf(entityId));
        when(functionChecker.checkFindOne(entityId)).thenReturn(Single.just(f1));
        if (requestBody.containsKey("runtime")) {
            when(runtimeChecker.checkExistsOne(1L)).thenReturn(Completable.complete());
        }
        when(functionChecker.checkUpdateNoDuplicate(requestBody, f1))
            .thenReturn(Single.just(f1));
        when(functionChecker.submitUpdate(requestBody, f1)).thenReturn(Completable.complete());

        functionHandler.updateOne(rc)
            .blockingSubscribe(() -> {},
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );

        testContext.completeNow();
    }

    @Test
    void updateOneEntityNotFound(VertxTestContext testContext) {
        long entityId = 1L;
        JsonObject jsonObject = new JsonObject("{\"runtime\": {\"runtime_id\": 1}, \"code\": \"x = 10\"}");

        RoutingContextMockHelper.mockBody(rc, jsonObject);
        when(rc.pathParam("id")).thenReturn(String.valueOf(entityId));
        when(functionChecker.checkFindOne(entityId)).thenReturn(Single.error(NotFoundException::new));

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
        JsonObject f1 = JsonObject.mapFrom(TestFunctionProvider.createFunction(1L));
        JsonObject requestBody = new JsonObject("{\"runtime\": {\"runtime_id\": 1}, \"code\": \"x = 10\"}");

        RoutingContextMockHelper.mockBody(rc, requestBody);
        when(rc.pathParam("id")).thenReturn(String.valueOf(entityId));
        when(functionChecker.checkFindOne(entityId)).thenReturn(Single.just(f1));
        when(runtimeChecker.checkExistsOne(1L)).thenReturn(Completable.error(NotFoundException::new));

        functionHandler.updateOne(rc)
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void updateOneDuplicateExists(VertxTestContext testContext) {
        long entityId = 1L;
        JsonObject f1 = JsonObject.mapFrom(TestFunctionProvider.createFunction(1L));
        JsonObject requestBody = new JsonObject("{\"runtime\": {\"runtime_id\": 1}, \"code\": \"x = 10\"}");

        RoutingContextMockHelper.mockBody(rc, requestBody);
        when(rc.pathParam("id")).thenReturn(String.valueOf(entityId));
        when(functionChecker.checkFindOne(entityId)).thenReturn(Single.just(f1));
        when(runtimeChecker.checkExistsOne(1L)).thenReturn(Completable.complete());
        when(functionChecker.checkUpdateNoDuplicate(requestBody, f1))
            .thenReturn(Single.error(AlreadyExistsException::new));

        functionHandler.updateOne(rc)
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(AlreadyExistsException.class);
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

        when(runtimeChecker.checkExistsOne(runtimeId)).thenReturn(Completable.complete());

        functionHandler.checkUpdateRuntimeExists(requestBody, entityJson)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.getLong("function_id")).isEqualTo(1L);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void checkUpdateResourceTypeNotExists(VertxTestContext testContext) {
        long runtimeId = 1L;
        Function entity = new Function();
        entity.setFunctionId(1L);
        JsonObject entityJson = JsonObject.mapFrom(entity);
        JsonObject requestBody = new JsonObject("{\"runtime\": {\"runtime_id\": " + runtimeId + "}}");

        when(runtimeChecker.checkExistsOne(runtimeId)).thenReturn(Completable.error(NotFoundException::new));

        functionHandler.checkUpdateRuntimeExists(requestBody, entityJson)
            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
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
                throwable -> testContext.verify(() -> fail("method did not throw exception"))
            );
    }
}
