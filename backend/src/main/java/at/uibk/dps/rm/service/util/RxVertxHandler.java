package at.uibk.dps.rm.service.util;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.rxjava3.CompletableHelper;
import io.vertx.rxjava3.MaybeHelper;
import io.vertx.rxjava3.SingleHelper;
import lombok.experimental.UtilityClass;

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
        operation.doOnError(throwable -> resultHandler.handle(Future.failedFuture(throwable.getCause())))
            .subscribe(MaybeHelper.toObserver(resultHandler));
    }

    /**
     * Handle a Single operation.
     *
     * @param operation the operation
     * @param resultHandler the result handler
     * @param <E> any datatype that can be returned by the reactive session
     */
    public static <E> void handleSession(Single<E> operation, Handler<AsyncResult<E>> resultHandler) {
        operation.doOnError(throwable -> resultHandler.handle(Future.failedFuture(throwable.getCause())))
            .subscribe(SingleHelper.toObserver(resultHandler));
    }

    /**
     * Handle a Completable operation.
     *
     * @param operation the operation
     * @param resultHandler the result handler
     */
    public static void handleSession(Completable operation, Handler<AsyncResult<Void>> resultHandler) {
        operation.doOnError(throwable -> resultHandler.handle(Future.failedFuture(throwable.getCause())))
            .subscribe(CompletableHelper.toObserver(resultHandler));
    }
}
