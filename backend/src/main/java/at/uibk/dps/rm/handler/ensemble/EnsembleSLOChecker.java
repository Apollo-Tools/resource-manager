package at.uibk.dps.rm.handler.ensemble;

import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.handler.ErrorHandler;
import at.uibk.dps.rm.service.rxjava3.database.ensemble.EnsembleSLOService;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;

/**
 * Implements methods to perform CRUD operations on the ensemble_slo entity.
 *
 * @see EntityChecker
 *
 * @author matthi-g
 */
public class EnsembleSLOChecker extends EntityChecker {

    private final EnsembleSLOService ensembleSLOService;
    /**
     * Create an instance from ensembleSLOService
     *
     * @param ensembleSLOService the ensemble slo service
     */
    public EnsembleSLOChecker(EnsembleSLOService ensembleSLOService) {
        super(ensembleSLOService);
        this.ensembleSLOService = ensembleSLOService;
    }

    /**
     * Find all ensembleSLOs their ensemble.
     *
     * @param ensembleId the id of the ensemble
     * @return a Single that emits all found ensembleSLOs as JsonArray
     */
    public Single<JsonArray> checkFindAllByEnsemble(long ensembleId) {
        Single<JsonArray> findAllByEnsemble = ensembleSLOService.findAllByEnsembleId(ensembleId);
        return ErrorHandler.handleFindAll(findAllByEnsemble);
    }
}
