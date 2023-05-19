package at.uibk.dps.rm.repository.ensemble;

import at.uibk.dps.rm.entity.model.Ensemble;
import at.uibk.dps.rm.repository.Repository;
import org.hibernate.reactive.stage.Stage;

import java.util.List;
import java.util.concurrent.CompletionStage;

/**
 * Implements database operations for the ensemble entity.
 *
 * @author matthi-g
 */
public class EnsembleRepository extends Repository<Ensemble> {
    /**
     * Create an instance from the sessionFactory.
     *
     * @param sessionFactory the session factory
     */
    public EnsembleRepository(Stage.SessionFactory sessionFactory) {
        super(sessionFactory, Ensemble.class);
    }

    /**
     * Find all ensembles by their creator.
     *
     * @param accountId the account id of the creator
     * @return a CompletionStage that emits the list of all ensembles
     */
    public CompletionStage<List<Ensemble>> findAllByAccountId(long accountId) {
        return sessionFactory.withSession(session ->
            session.createQuery("select distinct e from Ensemble e " +
                    "where e.createdBy.accountId=:accountId " +
                    "order by e.id", entityClass)
                .setParameter("accountId", accountId)
                .getResultList()
        );
    }

    /**
     * Find an ensemble by its id and creator.
     *
     * @param id the id of the ensemble
     * @param accountId the account id of the creator
     * @return a CompletionStage that emits the ensemble if it exists, else null
     */
    public CompletionStage<Ensemble> findByIdAndAccountId(long id, long accountId) {
        return sessionFactory.withSession(session ->
            session.createQuery("select e from Ensemble e " +
                    "where e.ensembleId=:id and e.createdBy.accountId=:accountId", entityClass)
                .setParameter("id", id)
                .setParameter("accountId", accountId)
                .getSingleResultOrNull()
        );
    }

    /**
     * Find an ensemble by its name and creator.
     *
     * @param name the name of the ensemble
     * @param accountId the account id of the creator
     * @return a CompletionStage that emits the ensemble if it exists, else null
     */
    public CompletionStage<Ensemble> findByNameAndAccountId(String name, long accountId) {
        return sessionFactory.withSession(session ->
            session.createQuery("select e from Ensemble e " +
                    "where e.name=:name and e.createdBy.accountId=:accountId", entityClass)
                .setParameter("name", name)
                .setParameter("accountId", accountId)
                .getSingleResultOrNull()
        );
    }

    /**
     * Update the isValid column of an ensemble.
     *
     * @param ensembleId the id of the ensemble
     * @param isValid the new validity value
     * @return a CompletionStage that emits the row count
     */
    public CompletionStage<Integer> updateValidity(long ensembleId, boolean isValid) {
        return sessionFactory.withSession(session ->
            session.createQuery("update Ensemble e " +
                    "set isValid=:isValid " +
                    "where e.ensembleId=:ensembleId")
                .setParameter("isValid", isValid)
                .setParameter("ensembleId", ensembleId)
                .executeUpdate()
        );
    }
}
