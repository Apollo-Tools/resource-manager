package at.uibk.dps.rm.service.database.util;

import at.uibk.dps.rm.annotations.Generated;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hibernate.reactive.stage.Stage.SessionFactory;
import org.hibernate.reactive.stage.Stage.Session;

import java.util.concurrent.CompletionStage;
import java.util.function.Function;

/**
 * A utility class that provides a RxJava wrapper for some methods of the {@link Session}.
 *
 * @author matthi-g
 */
@Getter
@AllArgsConstructor
public class SessionManager {

    private final Session session;

    /**
     * See {@link Session#find(Class, Object)}
     */
    public <T> Maybe<T> find(Class<T> entityClass, Object id) {
        return Maybe.fromCompletionStage(session.find(entityClass, id));
    }

    /**
     * See {@link Session#persist(Object)}
     */
    public <T> Single<T> persist(T entity) {
        return Single.fromCompletionStage(session.persist(entity)
            .thenApply(res -> entity));
    }

    /**
     * See {@link Session#persist(Object...)}
     */
    public Completable persist(Object... entities) {
        return Completable.fromCompletionStage(session.persist(entities));
    }

    /**
     * See {@link Session#merge(Object)}
     */
    public <T> Single<T> merge(T entity) {
        return Single.fromCompletionStage(session.merge(entity));
    }

    /**
     * See {@link Session#merge(Object[])}
     */
    public Single<Void> merge(Object... entity) {
        return Single.fromCompletionStage(session.merge(entity));
    }

    /**
     * See {@link Session#fetch(Object)}
     */
    public <T> Single<T> fetch(T entity) {
        return Single.fromCompletionStage(session.fetch(entity));
    }

    /**
     * See {@link Session#remove(Object)}
     */
    public Completable remove(Object entity) {
        return Completable.fromCompletionStage(session.remove(entity));
    }

    /**
     * See {@link Session#remove(Object...)}
     */
    public Completable remove(Object... entities) {
        return Completable.fromCompletionStage(session.remove(entities));
    }

    /**
     * See {@link Session#flush()}
     */
    public Completable flush() {
        return Completable.fromCompletionStage(session.flush());
    }

    /**
     * Perform work using a reactive session. The executions contained in function are
     * transactional.
     *
     * @param function the function that contains all database operations
     * @return a Single that emits an item of type E
     * @param <E> any datatype that can be returned by the reactive session
     */
    public static <E> Single<E> withTransactionSingle(SessionFactory sessionFactory,
            Function<SessionManager, Single<E>> function) {
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
    public static <E> Maybe<E> withTransactionMaybe(SessionFactory sessionFactory,
            Function<SessionManager, Maybe<E>> function) {
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
    public static Completable withTransactionCompletable(SessionFactory sessionFactory,
            Function<SessionManager, Completable> function) {
        CompletionStage<Void> transaction = sessionFactory.withTransaction(session -> {
            SessionManager sessionManager = new SessionManager(session);
            return function.apply(sessionManager).toCompletionStage(null);
        });
        return Completable.fromCompletionStage(transaction);
    }


    @Generated
    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        SessionManager that = (SessionManager) object;

        return session.equals(that.session);
    }


    @Generated
    @Override
    public int hashCode() {
        return session.hashCode();
    }
}
