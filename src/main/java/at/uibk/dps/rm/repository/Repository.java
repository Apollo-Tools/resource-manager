package at.uibk.dps.rm.repository;

import org.hibernate.reactive.stage.Stage.SessionFactory;

import java.util.List;
import java.util.concurrent.CompletionStage;

public abstract class Repository<E> {

    protected final SessionFactory sessionFactory;

    protected final Class<E> entityClass;

    public Repository(SessionFactory sessionFactory, Class<E> entityClass) {
        this.sessionFactory = sessionFactory;
        this.entityClass = entityClass;
    }

    public CompletionStage<E> create(E entity) {
        return sessionFactory.withTransaction((session, tx) -> session
                .persist(entity)
                .thenApply(result -> entity));
    }

    public CompletionStage<E> update(E entity) {
        return sessionFactory.withTransaction((session, tx) -> session
                .merge(entity));
    }

    public CompletionStage<Void> delete(long id) {
        return sessionFactory.withTransaction((session, tx) -> session
                .find(entityClass, id)
                .thenAccept(session::remove));
    }

    public CompletionStage<E> findById(long id) {
        return sessionFactory.withSession(session ->
                session.find(entityClass, id)
        );
    }

    public CompletionStage<List<E>> findAll() {
        // `id` is used as reference for the table key, displays error because the table is dynamic
        //noinspection JpaQlInspection
        return sessionFactory.withSession(session ->
                session.createQuery("from " + entityClass.getName() + " order by id", entityClass)
                    .getResultList()
        );
    }
}
