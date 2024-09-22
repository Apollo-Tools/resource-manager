package at.uibk.dps.rm.testutil.mockprovider;

import at.uibk.dps.rm.repository.DeploymentRepositoryProvider;
import at.uibk.dps.rm.repository.account.CredentialsRepository;
import at.uibk.dps.rm.repository.account.NamespaceRepository;
import at.uibk.dps.rm.repository.deployment.*;
import at.uibk.dps.rm.repository.ensemble.EnsembleRepository;
import at.uibk.dps.rm.repository.ensemble.EnsembleSLORepository;
import at.uibk.dps.rm.repository.function.FunctionRepository;
import at.uibk.dps.rm.repository.metric.PlatformMetricRepository;
import at.uibk.dps.rm.repository.resource.ResourceRepository;
import at.uibk.dps.rm.repository.resourceprovider.VPCRepository;
import at.uibk.dps.rm.repository.service.ServiceRepository;
import lombok.Getter;

import static org.mockito.Mockito.*;

/**
 * Utility class to mock the {@link DeploymentRepositoryProvider} for tests.
 *
 * @author matthi-g
 */
@Getter
public class DeploymentRepositoryProviderMock {
    private final DeploymentRepositoryProvider repositoryProvider = mock(DeploymentRepositoryProvider.class);
    private final DeploymentRepository deploymentRepository = mock(DeploymentRepository.class);
    private final EnsembleRepository ensembleRepository = mock(EnsembleRepository.class);
    private final EnsembleSLORepository ensembleSLORepository = mock(EnsembleSLORepository.class);
    private final ResourceDeploymentRepository resourceDeploymentRepository = mock(ResourceDeploymentRepository.class);
    private final FunctionDeploymentRepository functionDeploymentRepository = mock(FunctionDeploymentRepository.class);
    private final ServiceDeploymentRepository serviceDeploymentRepository = mock(ServiceDeploymentRepository.class);
    private final ResourceDeploymentStatusRepository statusRepository = mock(ResourceDeploymentStatusRepository.class);
    private final FunctionRepository functionRepository = mock(FunctionRepository.class);
    private final ServiceRepository serviceRepository = mock(ServiceRepository.class);
    private final ResourceRepository resourceRepository = mock(ResourceRepository.class);
    private final PlatformMetricRepository platformMetricRepository = mock(PlatformMetricRepository.class);
    private final VPCRepository vpcRepository = mock(VPCRepository.class);
    private final CredentialsRepository credentialsRepository = mock(CredentialsRepository.class);
    private final NamespaceRepository namespaceRepository = mock(NamespaceRepository.class);

    public void mockRepositories() {
        lenient().when(repositoryProvider.getDeploymentRepository()).thenReturn(deploymentRepository);
        lenient().when(repositoryProvider.getEnsembleRepository()).thenReturn(ensembleRepository);
        lenient().when(repositoryProvider.getEnsembleSLORepository()).thenReturn(ensembleSLORepository);
        lenient().when(repositoryProvider.getResourceDeploymentRepository()).thenReturn(resourceDeploymentRepository);
        lenient().when(repositoryProvider.getFunctionDeploymentRepository()).thenReturn(functionDeploymentRepository);
        lenient().when(repositoryProvider.getServiceDeploymentRepository()).thenReturn(serviceDeploymentRepository);
        lenient().when(repositoryProvider.getStatusRepository()).thenReturn(statusRepository);
        lenient().when(repositoryProvider.getFunctionRepository()).thenReturn(functionRepository);
        lenient().when(repositoryProvider.getServiceRepository()).thenReturn(serviceRepository);
        lenient().when(repositoryProvider.getResourceRepository()).thenReturn(resourceRepository);
        lenient().when(repositoryProvider.getPlatformMetricRepository()).thenReturn(platformMetricRepository);
        lenient().when(repositoryProvider.getVpcRepository()).thenReturn(vpcRepository);
        lenient().when(repositoryProvider.getPlatformMetricRepository()).thenReturn(platformMetricRepository);
        lenient().when(repositoryProvider.getCredentialsRepository()).thenReturn(credentialsRepository);
        lenient().when(repositoryProvider.getNamespaceRepository()).thenReturn(namespaceRepository);
    }
}
