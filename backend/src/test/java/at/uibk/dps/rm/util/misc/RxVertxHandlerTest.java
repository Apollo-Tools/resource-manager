package at.uibk.dps.rm.util.misc;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Implements tests for the {@link RxVertxHandler} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class RxVertxHandlerTest {

    @Test
    void handleSessionMaybeSuccess(VertxTestContext testContext) {
        RxVertxHandler.handleSession(Maybe.just(1L), testContext.succeeding(result ->
            testContext.verify(() -> {
                assertThat(result).isEqualTo(1L);
                testContext.completeNow();
        })));
    }

    @Test
    void handleSessionMaybeFailure(VertxTestContext testContext) {
        RxVertxHandler.handleSession(Maybe.error(new RuntimeException()), testContext.failing(throwable ->
            testContext.verify(() -> {
                assertThat(throwable).isInstanceOf(RuntimeException.class);
                testContext.completeNow();
        })));
    }

    @Test
    void handleSessionSingleSuccess(VertxTestContext testContext) {
        RxVertxHandler.handleSession(Single.just(1L), testContext.succeeding(result ->
            testContext.verify(() -> {
                assertThat(result).isEqualTo(1L);
                testContext.completeNow();
        })));
    }

    @Test
    void handleSessionSingleFailure(VertxTestContext testContext) {
        RxVertxHandler.handleSession(Single.error(new RuntimeException()), testContext.failing(throwable ->
            testContext.verify(() -> {
                assertThat(throwable).isInstanceOf(RuntimeException.class);
                testContext.completeNow();
        })));
    }

    @Test
    void handleSessionCompletableSuccess(VertxTestContext testContext) {
        RxVertxHandler.handleSession(Completable.complete(), testContext.succeeding(result ->
            testContext.verify(testContext::completeNow)));
    }

    @Test
    void handleSessionCompletableFailure(VertxTestContext testContext) {
        RxVertxHandler.handleSession(Completable.error(new RuntimeException()), testContext.failing(throwable ->
            testContext.verify(() -> {
                assertThat(throwable).isInstanceOf(RuntimeException.class);
                testContext.completeNow();
        })));
    }

}
