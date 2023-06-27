package at.uibk.dps.rm.repository;

import at.uibk.dps.rm.exception.NotFoundException;
import io.vertx.core.json.JsonObject;
import org.hibernate.reactive.stage.Stage;
import org.hibernate.reactive.stage.Stage.SessionFactory;

import java.util.List;
import java.util.concurrent.CompletionStage;

/**
 * Classes that inherit the Repository implement various database operations. The most basic
 * operations methods include create, read, updated or delete.
 *
 * @param <E> the class of the database entity
 * @author matthi-g
 */
public abstract class Repository<E> {

    protected final SessionFactory sessionFactory;

    protected final Class<E> entityClass;

    /**
     * Create an instance from the sessionFactory and entityClass.
     *
     * @param sessionFactory the session factory
     * @param entityClass the class of the entity
     */
    public Repository(SessionFactory sessionFactory, Class<E> entityClass) {
        this.sessionFactory = sessionFactory;
        this.entityClass = entityClass;
    }

    /**
     * Create and save a new entity.
     *
     * @param entity the new entity
     * @return a CompletionStage that emits the persisted entity
     */
    public CompletionStage<E> create(E entity) {
        return sessionFactory.withTransaction((session, tx) -> session
                .persist(entity)
                .thenApply(result -> entity));
    }

    /**
     * Create and save a list of entities.
     *
     * @param entityList the list of entities
     * @return an empty CompletionStage
     */
    public CompletionStage<Void> createAll(List<E> entityList) {
        return sessionFactory.withTransaction((session, tx) ->
            session.persist(entityList.toArray())
        );
    }

    /**
     * Update an entity.
     *
     * @param entity the entity
     * @return a CompletionStage that emits the new state of the entity
     */
    public CompletionStage<E> update(E entity) {
        return sessionFactory.withTransaction((session, tx) -> session
                .merge(entity));
    }

    /**
     * Delete an entity by its id.
     *
     * @param id the id of the entity
     * @return a CompletionStage that emits the row count
     */
    public CompletionStage<Integer> deleteById(long id) {
        //noinspection JpaQlInspection
        return this.sessionFactory.withTransaction(session ->
            session.createQuery("delete from " + entityClass.getName() + " where id=:id")
                .setParameter("id", id)
                .executeUpdate()
        );
    }

    /**
     * Find an entity by its id.
     *
     * @param id the id of the entity
     * @return a CompletionStage that emits the entity if it exists, else null
     */
    public CompletionStage<E> findById(Stage.Session session, long id) {
        return session.find(entityClass, id);
    }

    /**
     * Find all entities.
     *
     * @return a CompletionStage that emits all entities
     */
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
