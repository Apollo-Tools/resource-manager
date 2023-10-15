package at.uibk.dps.rm.repository.ensemble;

import at.uibk.dps.rm.entity.model.EnsembleSLO;
import at.uibk.dps.rm.repository.Repository;
import at.uibk.dps.rm.service.database.util.SessionManager;
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
