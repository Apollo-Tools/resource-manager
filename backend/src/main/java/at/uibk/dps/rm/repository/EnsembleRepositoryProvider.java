package at.uibk.dps.rm.repository;

import at.uibk.dps.rm.repository.ensemble.EnsembleRepository;
import at.uibk.dps.rm.repository.ensemble.EnsembleSLORepository;
import at.uibk.dps.rm.repository.metric.MetricRepository;
import at.uibk.dps.rm.repository.resource.ResourceRepository;
import lombok.Getter;

@Getter
public class EnsembleRepositoryProvider {
    private final EnsembleRepository ensembleRepository;

    private final EnsembleSLORepository ensembleSLORepository;

    private final ResourceRepository resourceRepository;

    private final MetricRepository metricRepository;

    public EnsembleRepositoryProvider() {
        this.ensembleRepository = new EnsembleRepository();
        this.ensembleSLORepository = new EnsembleSLORepository();
        this.resourceRepository = new ResourceRepository();
        this.metricRepository = new MetricRepository();
    }
}
