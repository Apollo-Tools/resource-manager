package at.uibk.dps.rm.handler;

import at.uibk.dps.rm.exception.AlreadyExistsException;
import at.uibk.dps.rm.exception.BadInputException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.exception.UsedByOtherEntityException;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.impl.AsyncResultSingle;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@ExtendWith(VertxExtension.class)
public class ErrorHandlerTest {

    @Test
    void handleFindOneNotNull(VertxTestContext testContext) {
        JsonObject basicJsonObject = new JsonObject("{\"id\": 10}");
        Single<JsonObject> handler = Single.just(basicJsonObject);

        ErrorHandler.handleFindOne(handler)
                .subscribe(result -> testContext.verify(() -> {
                            assertThat(result.getInteger("id")).isEqualTo(10);
                            testContext.completeNow();
                        }),
                        throwable -> testContext.verify(() -> fail("method throw exception")));
    }

    @Test
    void handleFindOneNull(VertxTestContext testContext) {
        JsonObject emptyJsonObject = null;
        // As found in "build/generated/sources/annotationProcessor/java/Main/at.uibk.dps.rm.service/rxjava3.database/metric/MetricService"
        Single<JsonObject> handler = AsyncResultSingle
                .toSingle(Future.succeededFuture(emptyJsonObject), value -> value);


        ErrorHandler.handleFindOne(handler)
                .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                        throwable -> testContext.verify(() -> {
                            assertThat(throwable).isInstanceOf(NotFoundException.class);
                            testContext.completeNow();
                        }));
    }

    @Test
    void handleFindAllNotNull(VertxTestContext testContext) {
        JsonArray basicJsonArray = new JsonArray("[10]");
        Single<JsonArray> handler = Single.just(basicJsonArray);

        ErrorHandler.handleFindAll(handler)
                .subscribe(result -> testContext.verify(() -> {
                            assertThat(result.getInteger(0)).isEqualTo(10);
                            testContext.completeNow();
                        }),
                        throwable -> testContext.verify(() -> fail("method throw exception")));
    }

    @Test
    void handleFindAllNull(VertxTestContext testContext) {
        JsonArray emptyJsonArray = null;
        // As found in "build/generated/sources/annotationProcessor/java/Main/at.uibk.dps.rm.service/rxjava3.database/metric/MetricService"
        Single<JsonArray>  handler = AsyncResultSingle
                .toSingle(Future.succeededFuture(emptyJsonArray), value -> value);

        ErrorHandler.handleFindAll(handler)
                .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                        throwable -> testContext.verify(() -> {
                            assertThat(throwable).isInstanceOf(NotFoundException.class);
                            testContext.completeNow();
                        }));
    }

    @Test
    void handleExistsOneTrue(VertxTestContext testContext) {
        Single<Boolean> handler = Single.just(true);

        ErrorHandler.handleExistsOne(handler)
                .subscribe(result -> testContext.verify(() -> {
                            assertThat(result).isTrue();
                            testContext.completeNow();
                        }),
                        throwable -> testContext.verify(() -> fail("method throw exception")));
    }

    @Test
    void handleExistsOneFalse(VertxTestContext testContext) {
        Single<Boolean> handler = Single.just(false);

        ErrorHandler.handleExistsOne(handler)
                .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                        throwable -> testContext.verify(() -> {
                            assertThat(throwable).isInstanceOf(NotFoundException.class);
                            testContext.completeNow();
                        }));
    }

    @Test
    void handleDuplicatesTrue(VertxTestContext testContext) {
        Single<Boolean> handler = Single.just(true);

        ErrorHandler.handleDuplicates(handler)
                .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                        throwable -> testContext.verify(() -> {
                            assertThat(throwable).isInstanceOf(AlreadyExistsException.class);
                            testContext.completeNow();
                        }));
    }

    @Test
    void handleDuplicatesFalse(VertxTestContext testContext) {
        Single<Boolean> handler = Single.just(false);

        ErrorHandler.handleDuplicates(handler)
                .subscribe(result -> testContext.verify(() -> {
                            assertThat(result).isFalse();
                            testContext.completeNow();
                        }),
                        throwable -> testContext.verify(() -> fail("method throw exception")));
    }

    @Test
    void handleUsedByOtherEntityTrue(VertxTestContext testContext) {
        Single<Boolean> handler = Single.just(true);

        ErrorHandler.handleUsedByOtherEntity(handler)
                .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                        throwable -> testContext.verify(() -> {
                            assertThat(throwable).isInstanceOf(UsedByOtherEntityException.class);
                            testContext.completeNow();
                        }));
    }

    @Test
    void handleUsedByOtherEntityFalse(VertxTestContext testContext) {
        Single<Boolean> handler = Single.just(false);

        ErrorHandler.handleUsedByOtherEntity(handler)
                .subscribe(result -> testContext.verify(() -> {
                            assertThat(result).isFalse();
                            testContext.completeNow();
                        }),
                        throwable -> testContext.verify(() -> fail("method throw exception")));
    }

    @Test
    void handleBadInputTrue(VertxTestContext testContext) {
        Single<Boolean> handler = Single.just(true);

        ErrorHandler.handleBadInput(handler)
                .subscribe(result -> testContext.verify(() -> {
                            assertThat(result).isTrue();
                            testContext.completeNow();
                        }),
                        throwable -> testContext.verify(() -> fail("method throw exception")));
    }

    @Test
    void handleBadInputFalse(VertxTestContext testContext) {
        Single<Boolean> handler = Single.just(false);

        ErrorHandler.handleBadInput(handler)
                .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                        throwable -> testContext.verify(() -> {
                            assertThat(throwable).isInstanceOf(BadInputException.class);
                            testContext.completeNow();
                        }));
    }
}
