package at.uibk.dps.rm.handler.function;

import at.uibk.dps.rm.testutil.RoutingContextMockHelper;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.core.MultiMap;
import io.vertx.rxjava3.core.http.HttpServerRequest;
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

    @Mock
    private HttpServerRequest request;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        functionHandler = new FunctionHandler(functionChecker);
    }

    private static Stream<Arguments> providePostBody() {
        String name = "func";
        long runtimeId = 1L;
        JsonObject requestBody = new JsonObject("{\"name\": \"" + name + "\", \"runtime\": {\"runtime_id\": " +
           runtimeId + "}, \"code\": \"x = 10\"}");
        JsonObject attributeBody = new JsonObject("{\"name\": \"" + name + "\", \"runtime\": {\"runtime_id\": " +
           runtimeId + "}, \"code\": \"testfile\", \"is_file\":true}");
        MultiMap attributes = MultiMap.caseInsensitiveMultiMap();
        attributes.add("name", name);
        attributes.add("runtime", "{\"runtime_id\": " + runtimeId + "}");
        return Stream.of(Arguments.of(false, "application/json", requestBody, null),
            Arguments.of(true, "multipart/form-data", attributeBody, attributes));
    }

    @ParameterizedTest
    @MethodSource("providePostBody")
    void postOne(boolean isFile, String contentType, JsonObject requestBody, MultiMap attributes,
             VertxTestContext testContext) {
        String fileName = "testfile";
        MultiMap multiMap = MultiMap.caseInsensitiveMultiMap();
        multiMap.add("Content-Type", contentType);
        if (isFile) {
            RoutingContextMockHelper.mockFormAttributes(rc, request, attributes);
            RoutingContextMockHelper.mockFileUpload(rc, fileName);
        } else {
            RoutingContextMockHelper.mockBody(rc, requestBody);
        }
        RoutingContextMockHelper.mockHeaders(rc, request, multiMap);
        when(functionChecker.submitCreate(requestBody)).thenReturn(Single.just(requestBody));

        functionHandler.postOne(rc)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.getJsonObject("runtime").getLong("runtime_id")).isEqualTo(1L);
                    if (isFile) {
                        assertThat(result.getString("code")).isEqualTo(fileName);
                    } else {
                        assertThat(result.getString("code")).isEqualTo("x = 10");
                    }
                    assertThat(result.getBoolean("is_file")).isEqualTo(isFile);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    private static Stream<Arguments> provideUpdateBody() {
        JsonObject requestBody = new JsonObject("{\"code\": \"x = 10\"}");
        JsonObject attributeBody = new JsonObject("{\"code\": \"testfile\"}");
        return Stream.of(Arguments.of(false, "application/json", requestBody),
            Arguments.of(true, "multipart/form-data", attributeBody));
    }

    @ParameterizedTest
    @MethodSource("provideUpdateBody")
    void updateOne(boolean isFile, String contentType, JsonObject requestBody, VertxTestContext testContext) {
        long functionId = 1L;
        String fileName = "testfile";
        MultiMap multiMap = MultiMap.caseInsensitiveMultiMap();
        multiMap.add("Content-Type", contentType);
        if (isFile) {
            RoutingContextMockHelper.mockFileUpload(rc, fileName);
        } else {
            RoutingContextMockHelper.mockBody(rc, requestBody);
        }
        JsonObject transferData = requestBody.copy();
        transferData.put("is_file", isFile);
        RoutingContextMockHelper.mockHeaders(rc, request, multiMap);
        when(rc.pathParam("id")).thenReturn(String.valueOf(functionId));
        when(functionChecker.submitUpdate(functionId, transferData)).thenReturn(Completable.complete());

        functionHandler.updateOne(rc)
            .blockingSubscribe(() -> testContext.verify(() -> {}),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
        testContext.completeNow();
    }
}
