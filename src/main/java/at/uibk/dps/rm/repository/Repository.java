package at.uibk.dps.rm.repository;

import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny.SessionFactory;

import java.util.List;

public class Repository<E> {

    private final SessionFactory sessionFactory;

    private final Class<E> entityClass;

    public Repository(SessionFactory sessionFactory, Class<E> entityClass) {
        this.sessionFactory = sessionFactory;
        this.entityClass = entityClass;
    }

    public Uni<E> create(E entity) {
        return sessionFactory.withTransaction((session, tx) -> session
                .persist(entity)
                .replaceWith(entity));
    }

    public Uni<E> update(E entity) {
        return sessionFactory.withTransaction((session, tx) -> session
                .merge(entity)
                .replaceWith(entity));
    }

    public Uni<Void> delete(int id) {
        return sessionFactory.withTransaction((session, tx) -> session
                .find(entityClass, id)
                .call(session::remove))
                .replaceWithVoid();
    }

    public Uni<E> findById(int id) {
        return sessionFactory.withSession(session ->
                session.find(entityClass, id)
        );
    }

    public Uni<List<E>> findAll() {
        return sessionFactory.withSession(session ->
                session.find(entityClass)
        );
    }
}
