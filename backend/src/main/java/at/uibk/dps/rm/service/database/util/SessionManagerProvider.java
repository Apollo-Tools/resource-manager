package at.uibk.dps.rm.service.database.util;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import org.hibernate.reactive.stage.Stage.SessionFactory;

import java.util.concurrent.CompletionStage;
import java.util.function.Function;

/**
 * This class provides methods to perform work using a reactive session inside a database
 * transaction.
 *
 * @author matthi-g
 */
@RequiredArgsConstructor
public class SessionManagerProvider {

    private final SessionFactory sessionFactory;

    /**
     * Perform work using a reactive session. The executions contained in function are
     * transactional.
     *
     * @param function the function that contains all database operations
     * @return a Single that emits an item of type E
     * @param <E> any datatype that can be returned by the reactive session
     */
    public <E> Single<E> withTransactionSingle(Function<SessionManager, Single<E>> function) {
        CompletionStage<E> transaction = sessionFactory.withTransaction(session -> {
            SessionManager sessionManager = new SessionManager(session);
            return function.apply(sessionManager).toCompletionStage();
        });
        return Single.fromCompletionStage(transaction);
    }

    /**
     * Perform work using a reactive session. The executions contained in function are
     * transactional.
     *
     * @param function the function that contains all database operations
     * @return a Maybe that emits an item of type E
     * @param <E> any datatype that can be returned by the reactive session
     */
    public <E> Maybe<E> withTransactionMaybe(Function<SessionManager, Maybe<E>> function) {
        CompletionStage<E> transaction = sessionFactory.withTransaction(session -> {
            SessionManager sessionManager = new SessionManager(session);
            return function.apply(sessionManager).toCompletionStage();
        });
        return Maybe.fromCompletionStage(transaction);
    }

    /**
     * Perform work using a reactive session. The executions contained in function are
     * transactional.
     *
     * @param function the function that contains all database operations
     * @return a Completable
     */
    public Completable withTransactionCompletable(Function<SessionManager, Completable> function) {
        CompletionStage<Void> transaction = sessionFactory.withTransaction(session -> {
            SessionManager sessionManager = new SessionManager(session);
            return function.apply(sessionManager).toCompletionStage(null);
        });
        return Completable.fromCompletionStage(transaction);
    }
}
