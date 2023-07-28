package at.uibk.dps.rm.testutil.mockprovider;

import at.uibk.dps.rm.repository.EnsembleRepositoryProvider;
import at.uibk.dps.rm.repository.ensemble.EnsembleRepository;
import at.uibk.dps.rm.repository.ensemble.EnsembleSLORepository;
import at.uibk.dps.rm.repository.metric.MetricRepository;
import at.uibk.dps.rm.repository.resource.ResourceRepository;
import lombok.Getter;
import org.mockito.Mockito;

@Getter
public class EnsembleRepositoryProviderMock {

    private final EnsembleRepositoryProvider repositoryProvider = Mockito.mock(EnsembleRepositoryProvider.class);

    private final EnsembleRepository ensembleRepository = Mockito.mock(EnsembleRepository.class);

    private final EnsembleSLORepository ensembleSLORepository = Mockito.mock(EnsembleSLORepository.class);

    private final ResourceRepository resourceRepository = Mockito.mock(ResourceRepository.class);

    private final MetricRepository metricRepository = Mockito.mock(MetricRepository.class);

    public void mock() {
        Mockito.lenient().when(repositoryProvider.getEnsembleRepository()).thenReturn(ensembleRepository);
        Mockito.lenient().when(repositoryProvider.getEnsembleSLORepository())
            .thenReturn(ensembleSLORepository);
        Mockito.lenient().when(repositoryProvider.getResourceRepository())
            .thenReturn(resourceRepository);
        Mockito.lenient().when(repositoryProvider.getMetricRepository())
            .thenReturn(metricRepository);
    }
}
