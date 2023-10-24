package at.uibk.dps.rm.util.misc;

import at.uibk.dps.rm.entity.misc.RetryCount;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.exception.SerializationException;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.pgclient.PgException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

    private static Stream<Arguments> provideCheckForRetry() {
        PgException serializationError = Mockito.mock(PgException.class);
        when(serializationError.getSqlState()).thenReturn("40001");
        PgException dbError = Mockito.mock(PgException.class);
        when(dbError.getSqlState()).thenReturn("40000");
        return Stream.of(
            Arguments.of(serializationError, 3, 4, SerializationException.class, true),
            Arguments.of(serializationError, 4, 4, SerializationException.class, false),
            Arguments.of(dbError, 0, 4, PgException.class, false),
            Arguments.of(new NotFoundException(), 3, 4, NotFoundException.class, false),
            Arguments.of(new NotFoundException(), 4, 4, NotFoundException.class, false)
        );
    }

    @ParameterizedTest
    @MethodSource("provideCheckForRetry")
    void checkForRetry(Throwable error, int retryCount,  int maxRetries, Class<? extends Throwable> expectedException,
            boolean expectedRetry, VertxTestContext testContext) throws Throwable {
        int retryDelay = 100;
        RetryCount count = mock(RetryCount.class);
        Flowable<Throwable> errors = Flowable.just(error);

        if (expectedRetry || expectedException.equals(SerializationException.class)) {
            when(count.increment()).thenReturn(retryCount);
        }

        RxVertxHandler.checkForRetry(count, maxRetries, retryDelay)
            .apply(errors)
            .subscribe(result -> testContext.verify(() -> {
                if (expectedRetry) {
                    assertThat(result).isEqualTo(0L);
                    testContext.completeNow();
                } else {
                    testContext.failNow("method did not throw exception");
                }
            }), throwable -> testContext.verify(() -> {
                if (expectedRetry) {
                    testContext.failNow("method has thrown exception");
                } else {
                    assertThat(throwable).isInstanceOf(expectedException);
                    testContext.completeNow();
                }
            }));
    }

}
