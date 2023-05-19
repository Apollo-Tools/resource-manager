package at.uibk.dps.rm.repository.ensemble;

import at.uibk.dps.rm.entity.model.EnsembleSLO;
import at.uibk.dps.rm.repository.Repository;
import org.hibernate.reactive.stage.Stage;

import java.util.List;
import java.util.concurrent.CompletionStage;

/**
 * Implements database operations for the ensemble_slo entity.
 *
 * @author matthi-g
 */
public class EnsembleSLORepository extends Repository<EnsembleSLO> {
    /**
     * Create an instance from the sessionFactory.
     *
     * @param sessionFactory the session factory
     */
    public EnsembleSLORepository(Stage.SessionFactory sessionFactory) {
        super(sessionFactory, EnsembleSLO.class);
    }

    /**
     * Find all ensembleSlos and fetch the ensemble.
     *
     * @return a CompletionStage that emits the list of all ensembleSLOs
     */
    public CompletionStage<List<EnsembleSLO>> findAllAndFetch() {
        return sessionFactory.withSession(session ->
                session.createQuery("select distinct slo from EnsembleSLO slo " +
                                "left join fetch slo.ensemble ensemble ", entityClass)
                        .getResultList()
        );
    }

    /**
     * Find all ensembleSlos by their ensembleId.
     *
     * @param ensembleId the id of the ensemble
     * @return a CompletionStage that emits the list of ensembleSLOs
     */
    public CompletionStage<List<EnsembleSLO>> findAllByEnsembleId(long ensembleId) {
        return sessionFactory.withSession(session ->
            session.createQuery("select distinct slo from EnsembleSLO slo " +
                    "where slo.ensemble.ensembleId=:ensembleId", entityClass)
                .setParameter("ensembleId", ensembleId)
                .getResultList()
        );
    }
}
