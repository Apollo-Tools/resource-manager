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

    public CompletionStage<Void> createAll(List<E> entityList) {
        return sessionFactory.withTransaction((session, tx) ->
            session.persist(entityList.toArray())
        );
    }

    public CompletionStage<E> update(E entity) {
        return sessionFactory.withTransaction((session, tx) -> session
                .merge(entity));
    }

    public CompletionStage<Integer> deleteById(long id) {
        //noinspection JpaQlInspection
        return this.sessionFactory.withTransaction(session ->
            session.createQuery("delete from " + entityClass.getName() + " where id=:id")
                .setParameter("id", id)
                .executeUpdate()
        );
    }

    public CompletionStage<E> findById(long id) {
        return sessionFactory.withSession(session ->
                session.find(entityClass, id)
        );
    }

    public CompletionStage<List<E>> findAll() {
        // `id` is used as reference for the table key
        // displays error because the table is dynamic
        //noinspection JpaQlInspection
        return sessionFactory.withSession(session ->
                session.createQuery("from " + entityClass.getName() + " order by id", entityClass)
                    .getResultList()
        );
    }
}
