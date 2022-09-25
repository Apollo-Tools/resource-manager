package at.uibk.dps.rm.handler;

import at.uibk.dps.rm.exception.AlreadyExistsException;
import at.uibk.dps.rm.exception.BadInputException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.exception.UsedByOtherEntityException;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.rxjava3.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.ext.web.RoutingContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class ResultHandlerTest {

    @Mock
    RoutingContext rc;

    @Mock
    HttpServerResponse response;

    @Test
    void handleGetOneRequestValid(VertxTestContext testContext) {
        setupMockResponse();
        JsonObject basicJsonObject = new JsonObject("{\"id\": 10}");
        Single<JsonObject> handler = Single.just(basicJsonObject);

        ResultHandler.handleGetOneRequest(rc, handler);

        Mockito.verify(response, times(1)).setStatusCode(200);
        testContext.completeNow();
    }

    @Test
    void handleGetOneRequestInvalid(VertxTestContext testContext) {
        Throwable throwable = new Throwable();
        Single<JsonObject> handler = Single.just(1)
                .map(res -> {
                    throw throwable;
                });

        ResultHandler.handleGetOneRequest(rc, handler);

        Mockito.verify(rc, times(1)).fail(500, throwable);
        testContext.completeNow();
    }

    @Test
    void handleGetAllRequestValid(VertxTestContext testContext) {
        setupMockResponse();
        JsonArray basicJsonArray = new JsonArray("[10]");
        Single<JsonArray> handler = Single.just(basicJsonArray);

        ResultHandler.handleGetAllRequest(rc, handler);

        Mockito.verify(response, times(1)).setStatusCode(200);
        testContext.completeNow();
    }

    @Test
    void handleGetAllRequestInvalid(VertxTestContext testContext) {
        Throwable throwable = new Throwable();
        Single<JsonArray> handler = Single.just(1)
                .map(res -> {
                    throw throwable;
                });

        ResultHandler.handleGetAllRequest(rc, handler);

        Mockito.verify(rc, times(1)).fail(500, throwable);
        testContext.completeNow();
    }

    @Test
    void handleSaveOneRequestValid(VertxTestContext testContext) {
        setupMockResponse();
        JsonObject basicJsonObject = new JsonObject("{\"id\": 10}");
        Single<JsonObject> handler = Single.just(basicJsonObject);

        ResultHandler.handleSaveOneRequest(rc, handler);

        Mockito.verify(response, times(1)).setStatusCode(201);
        testContext.completeNow();
    }

    @Test
    void handleSaveOneRequestInvalid(VertxTestContext testContext) {
        Throwable throwable = new Throwable();
        Single<JsonObject> handler = Single.just(1)
                .map(res -> {
                    throw throwable;
                });

        ResultHandler.handleSaveOneRequest(rc, handler);

        Mockito.verify(rc, times(1)).fail(500, throwable);
        testContext.completeNow();
    }

    @Test
    void handleSaveAllUpdateDeleteRequestValid(VertxTestContext testContext) {
        setupMockResponse();
        Completable handler = Completable.fromMaybe(Maybe.empty());

        ResultHandler.handleSaveAllUpdateDeleteRequest(rc, handler);

        Mockito.verify(response, times(1)).setStatusCode(204);
        testContext.completeNow();
    }

    @Test
    void handleSaveAllUpdateDeleteRequestInvalid(VertxTestContext testContext) {
        Throwable throwable = new Throwable();
        Completable handler = Completable.fromSingle(Single.just(1)
                .map(res -> {
                    throw throwable;
                }));

        ResultHandler.handleSaveAllUpdateDeleteRequest(rc, handler);

        Mockito.verify(rc, times(1)).fail(500, throwable);
        testContext.completeNow();
    }

    @Test
    void handleRequestNotFound(VertxTestContext testContext) {
        Throwable throwable = new NotFoundException();
        Single<JsonObject> handler = Single.just(1)
                .map(res -> {
                    throw throwable;
                });

        ResultHandler.handleGetOneRequest(rc, handler);

        Mockito.verify(rc, times(1)).fail(404, throwable);
        testContext.completeNow();
    }

    @Test
    void handleRequestAlreadyExists(VertxTestContext testContext) {
        Throwable throwable = new AlreadyExistsException();
        Single<JsonObject> handler = Single.just(1)
                .map(res -> {
                    throw throwable;
                });

        ResultHandler.handleGetOneRequest(rc, handler);

        Mockito.verify(rc, times(1)).fail(409, throwable);
        testContext.completeNow();
    }

    @Test
    void handleRequestUsedByOtherEntity(VertxTestContext testContext) {
        Throwable throwable = new UsedByOtherEntityException();
        Single<JsonObject> handler = Single.just(1)
                .map(res -> {
                    throw throwable;
                });

        ResultHandler.handleGetOneRequest(rc, handler);

        Mockito.verify(rc, times(1)).fail(409, throwable);
        testContext.completeNow();
    }

    @Test
    void handleRequestBadInput(VertxTestContext testContext) {
        Throwable throwable = new BadInputException();
        Single<JsonObject> handler = Single.just(1)
                .map(res -> {
                    throw throwable;
                });

        ResultHandler.handleGetOneRequest(rc, handler);

        Mockito.verify(rc, times(1)).fail(400, throwable);
        testContext.completeNow();
    }

    private void setupMockResponse() {
        when(rc.response()).thenReturn(response);
        when(response.setStatusCode(anyInt())).thenReturn(response);
        when(response.end(anyString())).thenReturn(Completable.fromMaybe(Maybe.empty()));
    }
}
