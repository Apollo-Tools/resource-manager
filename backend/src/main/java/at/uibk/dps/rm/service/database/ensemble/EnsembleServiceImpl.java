package at.uibk.dps.rm.service.database.ensemble;

import at.uibk.dps.rm.entity.model.Ensemble;
import at.uibk.dps.rm.repository.ensemble.EnsembleRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;

public class EnsembleServiceImpl extends DatabaseServiceProxy<Ensemble> implements EnsembleService {

    private final EnsembleRepository ensembleRepository;

    /**
     * Create an instance from the ensembleRepository.
     *
     * @param ensembleRepository the ensemble repository
     */
    public EnsembleServiceImpl(EnsembleRepository ensembleRepository) {
        super(ensembleRepository, Ensemble.class);
        this.ensembleRepository = ensembleRepository;
    }
}
