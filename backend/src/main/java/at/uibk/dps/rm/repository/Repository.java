package at.uibk.dps.rm.repository;

import org.hibernate.reactive.stage.Stage;

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

    protected final Class<E> entityClass;

    /**
     * Create an instance from the sessionFactory and entityClass.
     *
     * @param entityClass the class of the entity
     */
    public Repository(Class<E> entityClass) {
        this.entityClass = entityClass;
    }

    /**
     * Create and save a new entity.
     *
     * @param entity the new entity
     * @return a CompletionStage that emits the persisted entity
     */
    public CompletionStage<E> create(Stage.Session session, E entity) {
        return session.persist(entity).thenApply(result -> entity);
    }

    /**
     * Create and save a list of entities.
     *
     * @param entityList the list of entities
     * @return an empty CompletionStage
     */
    public CompletionStage<Void> createAll(Stage.Session session, List<E> entityList) {
        return session.persist(entityList.toArray());
    }

    /**
     * Delete an entity by its id.
     *
     * @param id the id of the entity
     * @return a CompletionStage that emits the row count
     */
    public CompletionStage<Integer> deleteById(Stage.Session session, long id) {
        //noinspection JpaQlInspection
        return session.createQuery("delete from " + entityClass.getName() + " where id=:id")
                .setParameter("id", id)
                .executeUpdate();
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
    public CompletionStage<List<E>> findAll(Stage.Session session) {
        // `id` is used as reference for the table key
        // displays error because the table is dynamic
        //noinspection JpaQlInspection
        return session.createQuery("from " + entityClass.getName() + " order by id", entityClass)
                    .getResultList();
    }
}
