package at.uibk.dps.rm.handler.ensemble;

import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.handler.ErrorHandler;
import at.uibk.dps.rm.service.rxjava3.database.ensemble.EnsembleSLOService;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;

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

    public Single<JsonArray> checkFindAllByEnsemble(long ensembleId) {
        Single<JsonArray> findAllByEnsemble = ensembleSLOService.findAllByEnsembleId(ensembleId);
        return ErrorHandler.handleFindAll(findAllByEnsemble);
    }
}
