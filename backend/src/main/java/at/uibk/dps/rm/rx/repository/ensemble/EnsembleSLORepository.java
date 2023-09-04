package at.uibk.dps.rm.rx.repository.ensemble;

import at.uibk.dps.rm.entity.model.EnsembleSLO;
import at.uibk.dps.rm.rx.repository.Repository;
import at.uibk.dps.rm.rx.service.database.util.SessionManager;
import io.reactivex.rxjava3.core.Single;

import java.util.List;

/**
 * Implements database operations for the ensemble_slo entity.
 *
 * @author matthi-g
 */
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
     * @param sessionManager the database session manager
     * @return a Single that emits the list of all ensembleSLOs
     */
    public Single<List<EnsembleSLO>> findAllAndFetch(SessionManager sessionManager) {
        return Single.fromCompletionStage(sessionManager.getSession()
            .createQuery("select distinct slo from EnsembleSLO slo " +
                "left join fetch slo.ensemble ensemble ", entityClass)
            .getResultList()
        );
    }

    /**
     * Find all ensembleSlos by their ensembleId.
     *
     * @param sessionManager the database session manager
     * @param ensembleId the id of the ensemble
     * @return a Single that emits the list of ensembleSLOs
     */
    public Single<List<EnsembleSLO>> findAllByEnsembleId(SessionManager sessionManager, long ensembleId) {
        return Single.fromCompletionStage(sessionManager.getSession()
            .createQuery("select distinct slo from EnsembleSLO slo " +
                "where slo.ensemble.ensembleId=:ensembleId", entityClass)
            .setParameter("ensembleId", ensembleId)
            .getResultList()
        );
    }
}
