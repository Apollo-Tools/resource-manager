package at.uibk.dps.rm.repository;

import at.uibk.dps.rm.service.database.util.SessionManager;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

import java.util.List;

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
     * @param sessionManager the database session manager
     * @param entity the new entity
     * @return a Single that emits the persisted entity
     */
    public Single<E> create(SessionManager sessionManager, E entity) {
        return sessionManager.persist(entity);
    }

    /**
     * Create and save a list of entities.
     *
     * @param sessionManager the database session manager
     * @param entityList the list of entities
     * @return a Completable
     */
    public Completable createAll(SessionManager sessionManager, List<E> entityList) {
        return sessionManager.persist(entityList.toArray());
    }

    /**
     * Find an entity by its id.
     *
     * @param sessionManager the database session manager
     * @param id the id of the entity
     * @return a Maybe that emits the entity if it exists, else null
     */
    public Maybe<E> findById(SessionManager sessionManager, long id) {
        return sessionManager.find(entityClass, id);
    }

    /**
     * Find an entity by its id and accountId.
     *
     * @param sessionManager the database session manager
     * @param id the id of the entity
     * @param accountId the id of the owner
     * @return a Maybe that emits the entity if it exists, else null
     */
    public Maybe<E> findByIdAndAccountId(SessionManager sessionManager, long id, long accountId) {
        throw new UnsupportedOperationException();
    }

    /**
     * Find all entities.
     *
     * @param sessionManager the database session manager
     * @return a Single that emits all entities
     */
    public Single<List<E>> findAll(SessionManager sessionManager) {
        // `id` is used as reference for the table key
        // displays error because the table is dynamic
        //noinspection JpaQlInspection
        return Single.fromCompletionStage(sessionManager.getSession()
            .createQuery("from " + entityClass.getName() + " order by id", entityClass)
            .getResultList());
    }

    /**
     * Find all entities by their account.
     *
     * @param sessionManager the database session manager
     * @param accountId the id of the account
     * @return a Single that emits a list of existing entities
     */
    public Single<List<E>> findAllByAccountId(SessionManager sessionManager, long accountId) {
        throw new UnsupportedOperationException();
    }
}
