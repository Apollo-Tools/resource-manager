package at.uibk.dps.rm.handler.function;

import at.uibk.dps.rm.entity.model.Account;
import at.uibk.dps.rm.entity.model.Function;
import at.uibk.dps.rm.service.rxjava3.database.function.FunctionService;
import at.uibk.dps.rm.testutil.RoutingContextMockHelper;
import at.uibk.dps.rm.testutil.objectprovider.TestAccountProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestFunctionProvider;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
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
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
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
    private FunctionService functionService;

    @Mock
    private RoutingContext rc;

    @Mock
    private HttpServerRequest request;

    private long functionId, accountId;
    private Account account;
    private Function f1, f2;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        functionHandler = new FunctionHandler(functionService);
        functionId = 1L;
        accountId = 10L;
        account = TestAccountProvider.createAccount(accountId);
        f1 = TestFunctionProvider.createFunction(1L);
        f2 = TestFunctionProvider.createFunction(2L);
    }

    private static Stream<Arguments> providePostBody() {
        String name = "func";
        long runtimeId = 1L, functionTypeId = 2L;
        int timeout = 60, memory = 128;
        JsonObject requestBody = new JsonObject("{\"name\": \"" + name + "\", \"function_type\": " +
            "{\"artifact_type_id\": " + functionTypeId + "}, \"runtime\": {\"runtime_id\":" + runtimeId + "}, " +
            "\"code\": \"x = 10\", \"timeout_seconds\": " + timeout + ", \"memory_megabytes\": " + memory + "}");
        JsonObject attributeBody = new JsonObject("{\"name\": \"" + name + "\", \"runtime\": " +
            "{\"runtime_id\": " + runtimeId + "}, \"function_type\": {\"artifact_type_id\": " + functionTypeId + "}, " +
            "\"timeout_seconds\": " + timeout + ", \"memory_megabytes\": " + memory + ",\"is_public\":true," +
            "\"code\": \"testfile\",\"is_file\":true}");
        MultiMap attributes = MultiMap.caseInsensitiveMultiMap();
        attributes.add("name", name);
        attributes.add("runtime", "{\"runtime_id\": " + runtimeId + "}");
        attributes.add("function_type", "{\"artifact_type_id\": " + functionTypeId + "}");
        attributes.add("timeout_seconds", String.valueOf(timeout));
        attributes.add("memory_megabytes", String.valueOf(memory));
        attributes.add("is_public", String.valueOf(true));
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
        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        when(functionService.saveToAccount(account.getAccountId(), requestBody)).thenReturn(Single.just(requestBody));

        functionHandler.postOneToAccount(rc)
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
        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        when(rc.pathParam("id")).thenReturn(String.valueOf(functionId));
        when(functionService.updateOwned(functionId, account.getAccountId(), transferData))
            .thenReturn(Completable.complete());

        functionHandler.updateOneOwned(rc)
            .blockingSubscribe(() -> testContext.verify(() -> {}),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
        testContext.completeNow();
    }

    @ParameterizedTest
    @ValueSource(strings = {"all", "own"})
    void getAll(String type, VertxTestContext testContext) {
        JsonArray jsonResult = new JsonArray(List.of(JsonObject.mapFrom(f1), JsonObject.mapFrom(f2)));
        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        if (type.equals("all")) {
            when(functionService.findAllAccessibleFunctions(accountId)).thenReturn(Single.just(jsonResult));
        } else {
            when(functionService.findAllByAccountId(accountId)).thenReturn(Single.just(jsonResult));
        }

        Single<JsonArray> handler = type.equals("all") ? functionHandler.getAll(rc) :
            functionHandler.getAllFromAccount(rc);

        handler.blockingSubscribe(result -> testContext.verify(() -> {
                assertThat(result.getJsonObject(0).getLong("function_id")).isEqualTo(1L);
                assertThat(result.getJsonObject(1).getLong("function_id")).isEqualTo(2L);
                testContext.completeNow();
            }),
            throwable -> testContext.verify(() -> fail("method has thrown exception"))
        );
    }
}
