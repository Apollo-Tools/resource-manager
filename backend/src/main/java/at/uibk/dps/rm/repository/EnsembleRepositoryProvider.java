package at.uibk.dps.rm.repository;

import at.uibk.dps.rm.repository.ensemble.EnsembleRepository;
import at.uibk.dps.rm.repository.ensemble.ResourceEnsembleRepository;
import at.uibk.dps.rm.repository.resource.ResourceRepository;
import at.uibk.dps.rm.repository.ensemble.EnsembleSLORepository;
import at.uibk.dps.rm.repository.metric.MetricRepository;
import lombok.Getter;

/**
 * Provides all repositories that are necessary to validate, modify and persist ensembles.
 *
 * @author matthi-g
 */
@Getter
public class EnsembleRepositoryProvider {
    private final EnsembleRepository ensembleRepository;

    private final EnsembleSLORepository ensembleSLORepository;

    private final ResourceRepository resourceRepository;

    private final MetricRepository metricRepository;

    private final ResourceEnsembleRepository resourceEnsembleRepository;

    /**
     * Create an instance.
     */
    public EnsembleRepositoryProvider() {
        this.ensembleRepository = new EnsembleRepository();
        this.ensembleSLORepository = new EnsembleSLORepository();
        this.resourceRepository = new ResourceRepository();
        this.metricRepository = new MetricRepository();
        this.resourceEnsembleRepository = new ResourceEnsembleRepository();
    }
}
