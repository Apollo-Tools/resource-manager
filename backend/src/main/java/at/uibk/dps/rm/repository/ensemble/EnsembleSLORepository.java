package at.uibk.dps.rm.repository.ensemble;

import at.uibk.dps.rm.entity.model.EnsembleSLO;
import at.uibk.dps.rm.repository.Repository;
import org.hibernate.reactive.stage.Stage.Session;

import java.util.List;
import java.util.concurrent.CompletionStage;

/**
 * Implements database operations for the ensemble_slo entity.
 *
 * @author matthi-g
 */
@Deprecated
public class EnsembleSLORepository extends Repository<EnsembleSLO> {
    /**
     * Create an instance.
     */
    public EnsembleSLORepository() {
        super(EnsembleSLO.class);
    }

    /**
     * Find all ensembleSlos and fetch the ensemble.
     *
     * @param session the database session
     * @return a CompletionStage that emits the list of all ensembleSLOs
     */
    public CompletionStage<List<EnsembleSLO>> findAllAndFetch(Session session) {
        return session.createQuery("select distinct slo from EnsembleSLO slo " +
                "left join fetch slo.ensemble ensemble ", entityClass)
            .getResultList();
    }

    /**
     * Find all ensembleSlos by their ensembleId.
     *
     * @param session the database session
     * @param ensembleId the id of the ensemble
     * @return a CompletionStage that emits the list of ensembleSLOs
     */
    public CompletionStage<List<EnsembleSLO>> findAllByEnsembleId(Session session, long ensembleId) {
        return session.createQuery("select distinct slo from EnsembleSLO slo " +
                "where slo.ensemble.ensembleId=:ensembleId", entityClass)
            .setParameter("ensembleId", ensembleId)
            .getResultList();
    }
}
