package at.uibk.dps.rm.repository.ensemble;

import at.uibk.dps.rm.entity.model.Ensemble;
import at.uibk.dps.rm.repository.Repository;
import at.uibk.dps.rm.service.database.util.SessionManager;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

import java.util.List;

/**
 * Implements database operations for the ensemble entity.
 *
 * @author matthi-g
 */
public class EnsembleRepository extends Repository<Ensemble> {
    /**
     * Create an instance.
     */
    public EnsembleRepository() {
        super(Ensemble.class);
    }

    /**
     * Find all ensembles and fetch the creator.
     *
     * @param sessionManager the database session manager
     * @return a Single that emits a list of all ensembles
     */
    public Single<List<Ensemble>> findAllAndFetch(SessionManager sessionManager) {
        return Single.fromCompletionStage(sessionManager.getSession()
            .createQuery("select distinct e from Ensemble e " +
                "left join fetch e.createdBy cb " +
                "order by e.ensembleId", entityClass)
            .getResultList()
        );
    }

    /**
     * Find all ensembles by their creator.
     *
     * @param sessionManager the database session manager
     * @param accountId the account id of the creator
     * @return a Single that emits the list of all ensembles
     */
    public Single<List<Ensemble>> findAllByAccountId(SessionManager sessionManager, long accountId) {
        return Single.fromCompletionStage(sessionManager.getSession()
            .createQuery("select distinct e from Ensemble e " +
                "where e.createdBy.accountId=:accountId " +
                "order by e.id", entityClass)
            .setParameter("accountId", accountId)
            .getResultList()
        );
    }

    /**
     * Find an ensemble by its id and creator.
     *
     * @param sessionManager the database session manager
     * @param id the id of the ensemble
     * @param accountId the account id of the creator
     * @return a Maybe that emits the ensemble if it exists, else null
     */
    public Maybe<Ensemble> findByIdAndAccountId(SessionManager sessionManager, long id, long accountId) {
        return Maybe.fromCompletionStage(sessionManager.getSession()
            .createQuery("select e from Ensemble e " +
                "where e.ensembleId=:id and e.createdBy.accountId=:accountId", entityClass)
            .setParameter("id", id)
            .setParameter("accountId", accountId)
            .getSingleResultOrNull()
        );
    }

    /**
     * Find an ensemble by its name and creator.
     *
     * @param sessionManager the database session manager
     * @param name the name of the ensemble
     * @param accountId the account id of the creator
     * @return a Maybe that emits the ensemble if it exists, else null
     */
    public Maybe<Ensemble> findByNameAndAccountId(SessionManager sessionManager, String name, long accountId) {
        return Maybe.fromCompletionStage(sessionManager.getSession()
            .createQuery("select e from Ensemble e " +
                "where e.name=:name and e.createdBy.accountId=:accountId", entityClass)
            .setParameter("name", name)
            .setParameter("accountId", accountId)
            .getSingleResultOrNull()
        );
    }

    /**
     * Update the isValid column of an ensemble.
     *
     * @param sessionManager the database session manager
     * @param ensembleId the id of the ensemble
     * @param isValid the new validity value
     * @return a Completable
     */
    public Completable updateValidity(SessionManager sessionManager, long ensembleId, boolean isValid) {
        return Completable.fromCompletionStage(sessionManager.getSession()
            .createQuery("update Ensemble e " +
                "set isValid=:isValid " +
                "where e.ensembleId=:ensembleId")
            .setParameter("isValid", isValid)
            .setParameter("ensembleId", ensembleId)
            .executeUpdate()
            .thenAccept(res -> {})
        );
    }
}
