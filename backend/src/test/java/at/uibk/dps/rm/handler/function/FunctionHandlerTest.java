package at.uibk.dps.rm.handler.function;

import at.uibk.dps.rm.exception.AlreadyExistsException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.testutil.RoutingContextMockHelper;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.when;

/**
 * Implements tests for the {@link FunctionHandler} class.
 *
 * @author matthi-g
 */
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
}
