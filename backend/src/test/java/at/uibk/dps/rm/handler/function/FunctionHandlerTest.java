package at.uibk.dps.rm.handler.function;

import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.testutil.RoutingContextMockHelper;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.ext.web.RoutingContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

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
    private RoutingContext rc;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        functionHandler = new FunctionHandler(functionChecker);
    }

    private static Stream<Arguments> provideRequestBody() {
        String name = "func";
        long runtimeId = 1L;
        JsonObject requestBody = new JsonObject("{\"name\": \"" + name + "\", \"runtime\": {\"runtime_id\": "
            + runtimeId + "}, \"code\": \"x = 10\"}");
        return Stream.of(Arguments.of(runtimeId, requestBody));
    }

    @ParameterizedTest
    @MethodSource("provideRequestBody")
    void postOneValid(long runtimeId, JsonObject requestBody, VertxTestContext testContext) {
        RoutingContextMockHelper.mockBody(rc, requestBody);
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

    @ParameterizedTest
    @MethodSource("provideRequestBody")
    void postOneRuntimeNotFound(long runtimeId, JsonObject requestBody, VertxTestContext testContext) {
        RoutingContextMockHelper.mockBody(rc, requestBody);

        functionHandler.postOne(rc)
            .subscribe(result -> testContext.verify(() -> fail("method has thrown exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }
}
