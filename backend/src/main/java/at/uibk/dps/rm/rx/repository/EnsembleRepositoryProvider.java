package at.uibk.dps.rm.rx.repository;

import at.uibk.dps.rm.rx.repository.ensemble.EnsembleRepository;
import at.uibk.dps.rm.rx.repository.resource.ResourceRepository;
import at.uibk.dps.rm.rx.repository.ensemble.EnsembleSLORepository;
import at.uibk.dps.rm.rx.repository.metric.MetricRepository;
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

    /**
     * Create an instance.
     */
    public EnsembleRepositoryProvider() {
        this.ensembleRepository = new EnsembleRepository();
        this.ensembleSLORepository = new EnsembleSLORepository();
        this.resourceRepository = new ResourceRepository();
        this.metricRepository = new MetricRepository();
    }
}
