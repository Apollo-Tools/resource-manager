package at.uibk.dps.rm.handler.ensemble;

import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.service.rxjava3.database.ensemble.EnsembleService;

public class EnsembleChecker extends EntityChecker {

    private final EnsembleService ensembleService;
    /**
     * Create an instance from the ensembleService.
     *
     * @param ensembleService the ensemble service to use
     */
    public EnsembleChecker(EnsembleService ensembleService) {
        super(ensembleService);
        this.ensembleService = ensembleService;
    }


}
