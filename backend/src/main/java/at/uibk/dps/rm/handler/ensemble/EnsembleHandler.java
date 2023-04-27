package at.uibk.dps.rm.handler.ensemble;

import at.uibk.dps.rm.handler.ValidationHandler;

public class EnsembleHandler extends ValidationHandler {

    private final EnsembleChecker ensembleChecker;

    private final EnsembleSLOChecker ensembleSLOChecker;

    /**
     * Create an instance from the ensembleChecker.
     *
     * @param ensembleChecker the ensemble checker
     */
    public EnsembleHandler(EnsembleChecker ensembleChecker, EnsembleSLOChecker ensembleSLOChecker) {
        super(ensembleChecker);
        this.ensembleChecker = ensembleChecker;
        this.ensembleSLOChecker = ensembleSLOChecker;
    }
}
