package at.uibk.dps.rm.handler.ensemble;

import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.service.rxjava3.database.ensemble.EnsembleSLOService;

public class EnsembleSLOChecker extends EntityChecker {
    /**
     * Create an instance from ensembleSLOService
     *
     * @param ensembleSLOService the ensemble slo service
     */
    public EnsembleSLOChecker(EnsembleSLOService ensembleSLOService) {
        super(ensembleSLOService);
    }
}
