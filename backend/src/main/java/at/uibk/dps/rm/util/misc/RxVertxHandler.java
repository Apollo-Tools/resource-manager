package at.uibk.dps.rm.util.misc;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.disposables.Disposable;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.rxjava3.CompletableHelper;
import io.vertx.rxjava3.MaybeHelper;
import io.vertx.rxjava3.SingleHelper;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A utility class that provides various methods to transform a {@link Handler} into a RxJava
 * observer.
 *
 * @author matthi-g
 */
@UtilityClass
public class RxVertxHandler {

    /**
     * Handle a Maybe operation.
     *
     * @param operation the operation
     * @param resultHandler the result handler
     * @param <E> any datatype that can be returned by the reactive session
     */
    public static <E> void handleSession(Maybe<E> operation, Handler<AsyncResult<E>> resultHandler) {
        operation.subscribe(toMaybeObserver(resultHandler));
    }

    /**
     * Handle a Single operation.
     *
     * @param operation the operation
     * @param resultHandler the result handler
     * @param <E> any datatype that can be returned by the reactive session
     */
    public static <E> void handleSession(Single<E> operation, Handler<AsyncResult<E>> resultHandler) {
        operation.subscribe(toSingleObserver(resultHandler));
    }

    /**
     * Handle a Completable operation.
     *
     * @param operation the operation
     * @param resultHandler the result handler
     */
    public static void handleSession(Completable operation, Handler<AsyncResult<Void>> resultHandler) {
        operation.subscribe(toCompletableObserver(resultHandler));
    }

    /**
     * Adapts a Vert.x {@code Handler<AsyncResult<T>>} to an RxJava3 {@link MaybeObserver}.
     * <p>
     * The returned observer can be subscribed to an {@link Maybe#subscribe(MaybeObserver)}.
     * src: {@link MaybeHelper}
     *
     * @param handler the handler to adapt
     * @return the observer
     */
    public static <T> MaybeObserver<T> toMaybeObserver(Handler<AsyncResult<T>> handler) {
        AtomicBoolean completed = new AtomicBoolean();
        return new MaybeObserver<>() {
            @Override
            public void onSubscribe(@NonNull Disposable disposable) {
            }
            @Override
            public void onComplete() {
                if (completed.compareAndSet(false, true)) {
                    handler.handle(io.vertx.core.Future.succeededFuture());
                }
            }
            @Override
            public void onSuccess(@NonNull T item) {
                if (completed.compareAndSet(false, true)) {
                    handler.handle(io.vertx.core.Future.succeededFuture(item));
                }
            }
            @Override
            public void onError(@NotNull Throwable error) {
                if (completed.compareAndSet(false, true)) {
                    Throwable cause = error.getCause() != null ? error.getCause() : error;
                    handler.handle(Future.failedFuture(cause));
                }
            }
        };
    }

    /**
     * Adapts a Vert.x {@code Handler<AsyncResult<T>>} to an RxJava3 {@link SingleObserver}.
     * <p>
     * The returned observer can be subscribed to an {@link Single#subscribe(SingleObserver)}.
     * src: {@link SingleHelper}
     *
     * @param handler the handler to adapt
     * @return the observer
     */
    public static <T> SingleObserver<T> toSingleObserver(Handler<AsyncResult<T>> handler) {
        AtomicBoolean completed = new AtomicBoolean();
        return new SingleObserver<>() {
            @Override
            public void onSubscribe(@NonNull Disposable disposable) {
            }
            @Override
            public void onSuccess(@NonNull T item) {
                if (completed.compareAndSet(false, true)) {
                    handler.handle(Future.succeededFuture(item));
                }
            }
            @Override
            public void onError(@NotNull Throwable error) {
                if (completed.compareAndSet(false, true)) {
                    Throwable cause = error.getCause() != null ? error.getCause() : error;
                    handler.handle(Future.failedFuture(cause));
                }
            }
        };
    }

    /**
     * Adapts a Vert.x {@code Handler<AsyncResult<T>>} to an RxJava3 {@link MaybeObserver}.
     * <p>
     * The returned observer can be subscribed to an {@link Maybe#subscribe(MaybeObserver)}.
     * src: {@link CompletableHelper}
     *
     * @param handler the handler to adapt
     * @return the observer
     */
    public static <T> CompletableObserver toCompletableObserver(Handler<AsyncResult<T>> handler) {
        AtomicBoolean completed = new AtomicBoolean();
        return new CompletableObserver() {
            @Override
            public void onSubscribe(@NonNull Disposable disposable) {
            }
            @Override
            public void onComplete() {
                if (completed.compareAndSet(false, true)) {
                    handler.handle(io.vertx.core.Future.succeededFuture());
                }
            }

            /**
             * CompletableObserver Notifies the CompletableObserver with that the
             * Completable has finished sending push-based notifications.
             * The Completable will not call this method if it calls onError.
             */
            public void onSuccess() {
                if (completed.compareAndSet(false, true)) {
                    handler.handle(io.vertx.core.Future.succeededFuture());
                }
            }
            @Override
            public void onError(@NotNull Throwable error) {
                if (completed.compareAndSet(false, true)) {
                    Throwable cause = error.getCause() != null ? error.getCause() : error;
                    handler.handle(Future.failedFuture(cause));
                }
            }
        };
    }
}
