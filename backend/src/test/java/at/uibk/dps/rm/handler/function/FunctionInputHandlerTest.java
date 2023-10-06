package at.uibk.dps.rm.handler.function;

import at.uibk.dps.rm.exception.BadInputException;
import at.uibk.dps.rm.exception.WrongFileTypeException;
import at.uibk.dps.rm.testutil.RoutingContextMockHelper;
import at.uibk.dps.rm.testutil.mockprovider.Mockprovider;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.core.Context;
import io.vertx.rxjava3.core.MultiMap;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.core.file.FileSystem;
import io.vertx.rxjava3.core.http.HttpServerRequest;
import io.vertx.rxjava3.ext.web.RoutingContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.AdditionalMatchers.or;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Implements tests for the {@link FunctionInputHandler} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class FunctionInputHandlerTest {

    @Mock
    private RoutingContext rc;

    @Mock
    private HttpServerRequest request;

    @Mock
    private Context context;

    @Mock
    private Vertx vertx;

    @Mock
    private FileSystem fileSystem;

    @ParameterizedTest
    @CsvSource({
        "foo1, true",
        "Foo1, false",
        "foo!, false",
        "foo_, false"
    })
    void validateAddFunctionRequestNonFile(String name, boolean valid, VertxTestContext testContext) {
        JsonObject jsonObject = new JsonObject("{\"name\": \"" + name + "\"}");

        MultiMap multiMap = MultiMap.caseInsensitiveMultiMap();
        multiMap.add("Content-Type", "application/json");
        RoutingContextMockHelper.mockHeaders(rc, request, multiMap);
        RoutingContextMockHelper.mockBody(rc, jsonObject);
        FunctionInputHandler.validateAddFunctionRequest(rc);

        if (valid) {
            verify(rc).next();
        } else {
            verify(rc).fail(eq(400), any(Throwable.class));
        }
        testContext.completeNow();
    }

    @ParameterizedTest
    @CsvSource({
        "foo1, foo1.zip, true",
        "Foo1, foo1.zip, false",
        "foo1, foo1, false",
        "foo_, foo1, false"
    })
    void validateAddFunctionRequestFile(String name, String fileName, boolean valid, VertxTestContext testContext) {
        MultiMap attributes = MultiMap.caseInsensitiveMultiMap();
        attributes.add("name", name);

        MultiMap multiMap = MultiMap.caseInsensitiveMultiMap();
        multiMap.add("Content-Type", "multipart/form-data");
        RoutingContextMockHelper.mockHeaders(rc, request, multiMap);
        RoutingContextMockHelper.mockFormAttributes(rc, request, attributes);
        RoutingContextMockHelper.mockFileUpload(rc, fileName);

        try (MockedStatic<Vertx> mockedVertx = Mockprovider.mockVertx()) {
            if (!valid) {
                when(context.owner()).thenReturn(vertx);
                when(vertx.fileSystem()).thenReturn(fileSystem);
                when(fileSystem.deleteBlocking(fileName)).thenReturn(fileSystem);
                mockedVertx.when(Vertx::currentContext).thenReturn(context);
            }
            FunctionInputHandler.validateAddFunctionRequest(rc);
        }

        if (valid) {
            verify(rc).next();
        } else {
            verify(rc).fail(eq(400), or(any(BadInputException.class), any(WrongFileTypeException.class)));
        }
        testContext.completeNow();
    }

    @Test
    void validateUpdateFunctionRequestNonFile(VertxTestContext testContext) {
        MultiMap multiMap = MultiMap.caseInsensitiveMultiMap();
        multiMap.add("Content-Type", "application/json");
        RoutingContextMockHelper.mockHeaders(rc, request, multiMap);
        FunctionInputHandler.validateUpdateFunctionRequest(rc);

        verify(rc).next();
        testContext.completeNow();
    }

    @ParameterizedTest
    @CsvSource({
        "foo1.zip, true",
        "foo1, false"
    })
    void validateUpdateFunctionRequestFile(String fileName, boolean valid, VertxTestContext testContext) {
        MultiMap multiMap = MultiMap.caseInsensitiveMultiMap();
        multiMap.add("Content-Type", "multipart/form-data");
        RoutingContextMockHelper.mockHeaders(rc, request, multiMap);
        RoutingContextMockHelper.mockFileUpload(rc, fileName);

        try (MockedStatic<Vertx> mockedVertx = Mockprovider.mockVertx()) {
            if (!valid) {
                when(context.owner()).thenReturn(vertx);
                when(vertx.fileSystem()).thenReturn(fileSystem);
                when(fileSystem.deleteBlocking(fileName)).thenReturn(fileSystem);
                mockedVertx.when(Vertx::currentContext).thenReturn(context);
            }
            FunctionInputHandler.validateUpdateFunctionRequest(rc);
        }

        if (valid) {
            verify(rc).next();
        } else {
            verify(rc).fail(eq(400), any(WrongFileTypeException.class));
        }
        testContext.completeNow();
    }

}
