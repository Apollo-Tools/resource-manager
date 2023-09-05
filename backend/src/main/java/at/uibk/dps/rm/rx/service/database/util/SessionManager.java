package at.uibk.dps.rm.rx.service.database.util;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hibernate.reactive.stage.Stage.Session;

@Getter
@AllArgsConstructor
public class SessionManager {

    private final Session session;

    public <T> Maybe<T> find(Class<T> entityClass, Object id) {
        return Maybe.fromCompletionStage(session.find(entityClass, id));
    }

    public <T> Single<T> persist(T entity) {
        return Single.fromCompletionStage(session.persist(entity)
            .thenApply(res -> entity));
    }

    public Completable persist(Object... entities) {
        return Completable.fromCompletionStage(session.persist(entities));
    }

    public <T> Single<T> merge(T entity) {
        return Single.fromCompletionStage(session.merge(entity));
    }

    public Single<Void> merge(Object... entity) {
        return Single.fromCompletionStage(session.merge(entity));
    }

    public <T> Single<T> fetch(T entity) {
        return Single.fromCompletionStage(session.fetch(entity));
    }

    public Completable remove(Object entity) {
        return Completable.fromCompletionStage(session.remove(entity));
    }

    public Completable remove(Object... entities) {
        return Completable.fromCompletionStage(session.remove(entities));
    }

    public Completable flush() {
        return Completable.fromCompletionStage(session.flush());
    }



}
