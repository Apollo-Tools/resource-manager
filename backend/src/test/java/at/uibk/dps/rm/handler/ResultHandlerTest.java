package at.uibk.dps.rm.handler;

import at.uibk.dps.rm.exception.*;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.rxjava3.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.ext.web.RoutingContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class ResultHandlerTest {

    private ResultHandler resultHandler;

    @Mock
    private ValidationHandler validationHandler;

    @Mock
    private RoutingContext rc;

    @Mock
    private HttpServerResponse response;

    @BeforeEach
    void initTest() {
        resultHandler = new ResultHandler(validationHandler);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void handleFindOneRequestValid(boolean useCustomHandler, VertxTestContext testContext) {
        setupMockResponse(true);
        JsonObject basicJsonObject = new JsonObject("{\"id\": 10}");
        Single<JsonObject> handler = Single.just(basicJsonObject);

        if (useCustomHandler) {
            resultHandler.handleFindOneRequest(rc, handler);
        } else {
            when(validationHandler.getOne(rc)).thenReturn(handler);
            resultHandler.handleFindOneRequest(rc);
        }

        Mockito.verify(response).setStatusCode(200);
        testContext.completeNow();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void handleFindOneRequestInvalid(boolean useCustomHandler, VertxTestContext testContext) {
        Throwable throwable = new Throwable();
        Single<JsonObject> handler = Single.error(throwable);

        if (useCustomHandler) {
            resultHandler.handleFindOneRequest(rc, handler);
        } else {
            when(validationHandler.getOne(rc)).thenReturn(handler);
            resultHandler.handleFindOneRequest(rc);
        }

        Mockito.verify(rc).fail(500, throwable);
        testContext.completeNow();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void handleFindAllRequestValid(boolean useCustomHandler, VertxTestContext testContext) {
        setupMockResponse(true);
        JsonArray basicJsonArray = new JsonArray("[10]");
        Single<JsonArray> handler = Single.just(basicJsonArray);

        if (useCustomHandler) {
            resultHandler.handleFindAllRequest(rc, handler);
        } else {
            when(validationHandler.getAll(rc)).thenReturn(handler);
            resultHandler.handleFindAllRequest(rc);
        }

        Mockito.verify(response).setStatusCode(200);
        testContext.completeNow();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void handleFindAllRequestInvalid(boolean useCustomHandler, VertxTestContext testContext) {
        Throwable throwable = new Throwable();
        Single<JsonArray> handler = Single.error(throwable);

        if (useCustomHandler) {
            resultHandler.handleFindAllRequest(rc, handler);
        } else {
            when(validationHandler.getAll(rc)).thenReturn(handler);
            resultHandler.handleFindAllRequest(rc);
        }

        Mockito.verify(rc).fail(500, throwable);
        testContext.completeNow();
    }

    @Test
    void handlePostOneRequestValid(VertxTestContext testContext) {
        setupMockResponse(true);
        JsonObject basicJsonObject = new JsonObject("{\"id\": 10}");
        Single<JsonObject> handler = Single.just(basicJsonObject);

        when(validationHandler.postOne(rc)).thenReturn(handler);

        resultHandler.handleSaveOneRequest(rc);

        Mockito.verify(response).setStatusCode(201);
        testContext.completeNow();
    }

    @Test
    void handlePostOneRequestInvalid(VertxTestContext testContext) {
        Throwable throwable = new Throwable();
        Single<JsonObject> handler = Single.error(throwable);

        when(validationHandler.postOne(rc)).thenReturn(handler);

        resultHandler.handleSaveOneRequest(rc);

        Mockito.verify(rc).fail(500, throwable);
        testContext.completeNow();
    }

    @ParameterizedTest
    @CsvSource({"saveAll, 204", "saveAll, 500", "update, 204", "update, 500", "delete, 204", "delete, 500"})
    void handlePostAllUpdateDeleteRequest(String method, int statusCode, VertxTestContext testContext) {
        Completable handler;
        Throwable throwable = new Throwable();
        if (statusCode == 204) {
            setupMockResponse(false);
            handler = Completable.fromMaybe(Maybe.empty());
        } else {
            handler = Completable.fromSingle(Single.error(throwable));
        }

        switch(method) {
            case "saveAll":
                when(validationHandler.postAll(rc)).thenReturn(handler);
                resultHandler.handleSaveAllRequest(rc);
                break;
            case "update":
                when(validationHandler.updateOne(rc)).thenReturn(handler);
                resultHandler.handleUpdateRequest(rc);
                break;
            case "delete":
                when(validationHandler.deleteOne(rc)).thenReturn(handler);
                resultHandler.handleDeleteRequest(rc);
                break;
        }

        if (statusCode == 204) {
            Mockito.verify(response).setStatusCode(204);
        } else {
            Mockito.verify(rc).fail(500, throwable);
        }
        testContext.completeNow();
    }

    @Test
    void handleRequestNotFound(VertxTestContext testContext) {
        Throwable throwable = new NotFoundException();
        Single<JsonObject> handler = Single.error(throwable);

        resultHandler.handleFindOneRequest(rc, handler);

        Mockito.verify(rc).fail(404, throwable);
        testContext.completeNow();
    }

    @Test
    void handleRequestAlreadyExists(VertxTestContext testContext) {
        Throwable throwable = new AlreadyExistsException();
        Single<JsonObject> handler = Single.error(throwable);

        resultHandler.handleFindOneRequest(rc, handler);

        Mockito.verify(rc).fail(409, throwable);
        testContext.completeNow();
    }

    @Test
    void handleRequestUsedByOtherEntity(VertxTestContext testContext) {
        Throwable throwable = new UsedByOtherEntityException();
        Single<JsonObject> handler = Single.error(throwable);

        resultHandler.handleFindOneRequest(rc, handler);

        Mockito.verify(rc).fail(409, throwable);
        testContext.completeNow();
    }

    @Test
    void handleRequestBadInput(VertxTestContext testContext) {
        Throwable throwable = new BadInputException();
        Single<JsonObject> handler = Single.error(throwable);

        resultHandler.handleFindOneRequest(rc, handler);

        Mockito.verify(rc).fail(400, throwable);
        testContext.completeNow();
    }

    @Test
    void handleUnauthorized(VertxTestContext testContext) {
        Throwable throwable = new UnauthorizedException();
        Single<JsonObject> handler = Single.error(throwable);

        resultHandler.handleFindOneRequest(rc, handler);

        Mockito.verify(rc).fail(401, throwable);
        testContext.completeNow();
    }

    private void setupMockResponse(boolean responseHasContent) {
        when(rc.response()).thenReturn(response);
        when(response.setStatusCode(anyInt())).thenReturn(response);
        if (responseHasContent) {
            when(response.end(anyString())).thenReturn(Completable.fromMaybe(Maybe.empty()));
        } else {
            // for getSaveAllUpdateDeleteResponse (no content)
            when(response.end()).thenReturn(Completable.fromMaybe(Maybe.empty()));
        }
    }
}
