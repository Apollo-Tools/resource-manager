package at.uibk.dps.rm.repository.ensemble;

import at.uibk.dps.rm.entity.model.Ensemble;
import at.uibk.dps.rm.repository.Repository;
import org.hibernate.reactive.stage.Stage.Session;

import java.util.List;
import java.util.concurrent.CompletionStage;

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
     * Find all ensembles by their creator.
     *
     * @param session the database session
     * @param accountId the account id of the creator
     * @return a CompletionStage that emits the list of all ensembles
     */
    public CompletionStage<List<Ensemble>> findAllByAccountId(Session session, long accountId) {
        return session.createQuery("select distinct e from Ensemble e " +
                "where e.createdBy.accountId=:accountId " +
                "order by e.id", entityClass)
            .setParameter("accountId", accountId)
            .getResultList();
    }

    /**
     * Find an ensemble by its id and creator.
     *
     * @param session the database session
     * @param id the id of the ensemble
     * @param accountId the account id of the creator
     * @return a CompletionStage that emits the ensemble if it exists, else null
     */
    public CompletionStage<Ensemble> findByIdAndAccountId(Session session, long id, long accountId) {
        return session.createQuery("select e from Ensemble e " +
                "where e.ensembleId=:id and e.createdBy.accountId=:accountId", entityClass)
            .setParameter("id", id)
            .setParameter("accountId", accountId)
            .getSingleResultOrNull();
    }

    /**
     * Find an ensemble by its name and creator.
     *
     * @param session the database session
     * @param name the name of the ensemble
     * @param accountId the account id of the creator
     * @return a CompletionStage that emits the ensemble if it exists, else null
     */
    public CompletionStage<Ensemble> findByNameAndAccountId(Session session, String name, long accountId) {
        return session.createQuery("select e from Ensemble e " +
                "where e.name=:name and e.createdBy.accountId=:accountId", entityClass)
            .setParameter("name", name)
            .setParameter("accountId", accountId)
            .getSingleResultOrNull();
    }

    /**
     * Update the isValid column of an ensemble.
     *
     * @param session the database session
     * @param ensembleId the id of the ensemble
     * @param isValid the new validity value
     * @return a CompletionStage that emits the row count
     */
    public CompletionStage<Integer> updateValidity(Session session, long ensembleId, boolean isValid) {
        return session.createQuery("update Ensemble e " +
                "set isValid=:isValid " +
                "where e.ensembleId=:ensembleId")
            .setParameter("isValid", isValid)
            .setParameter("ensembleId", ensembleId)
            .executeUpdate();
    }
}
