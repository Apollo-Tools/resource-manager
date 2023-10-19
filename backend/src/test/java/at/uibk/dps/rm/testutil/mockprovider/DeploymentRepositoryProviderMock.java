package at.uibk.dps.rm.testutil.mockprovider;

import at.uibk.dps.rm.repository.DeploymentRepositoryProvider;
import at.uibk.dps.rm.repository.account.CredentialsRepository;
import at.uibk.dps.rm.repository.account.NamespaceRepository;
import at.uibk.dps.rm.repository.deployment.*;
import at.uibk.dps.rm.repository.function.FunctionRepository;
import at.uibk.dps.rm.repository.metric.PlatformMetricRepository;
import at.uibk.dps.rm.repository.resource.ResourceRepository;
import at.uibk.dps.rm.repository.resourceprovider.VPCRepository;
import at.uibk.dps.rm.repository.service.ServiceRepository;
import lombok.Getter;
import org.mockito.Mockito;

/**
 * Utility class to mock the {@link DeploymentRepositoryProvider} for tests.
 *
 * @author matthi-g
 */
@Getter
public class DeploymentRepositoryProviderMock {
    private final DeploymentRepositoryProvider repositoryProvider = Mockito.mock(DeploymentRepositoryProvider.class);
    private final DeploymentRepository deploymentRepository = Mockito.mock(DeploymentRepository.class);
    private final ResourceDeploymentRepository resourceDeploymentRepository =
        Mockito.mock(ResourceDeploymentRepository.class);
    private final FunctionDeploymentRepository functionDeploymentRepository =
        Mockito.mock(FunctionDeploymentRepository.class);
    private final ServiceDeploymentRepository serviceDeploymentRepository =
        Mockito.mock(ServiceDeploymentRepository.class);
    private final ResourceDeploymentStatusRepository statusRepository =
        Mockito.mock(ResourceDeploymentStatusRepository.class);
    private final FunctionRepository functionRepository = Mockito.mock(FunctionRepository.class);
    private final ServiceRepository serviceRepository = Mockito.mock(ServiceRepository.class);
    private final ResourceRepository resourceRepository = Mockito.mock(ResourceRepository.class);
    private final PlatformMetricRepository platformMetricRepository = Mockito.mock(PlatformMetricRepository.class);
    private final VPCRepository vpcRepository = Mockito.mock(VPCRepository.class);
    private final CredentialsRepository credentialsRepository = Mockito.mock(CredentialsRepository.class);
    private final NamespaceRepository namespaceRepository = Mockito.mock(NamespaceRepository.class);

    public void mock() {
        Mockito.lenient().when(repositoryProvider.getDeploymentRepository()).thenReturn(deploymentRepository);
        Mockito.lenient().when(repositoryProvider.getResourceDeploymentRepository())
            .thenReturn(resourceDeploymentRepository);
        Mockito.lenient().when(repositoryProvider.getFunctionDeploymentRepository())
            .thenReturn(functionDeploymentRepository);
        Mockito.lenient().when(repositoryProvider.getServiceDeploymentRepository())
            .thenReturn(serviceDeploymentRepository);
        Mockito.lenient().when(repositoryProvider.getStatusRepository()).thenReturn(statusRepository);
        Mockito.lenient().when(repositoryProvider.getFunctionRepository()).thenReturn(functionRepository);
        Mockito.lenient().when(repositoryProvider.getServiceRepository()).thenReturn(serviceRepository);
        Mockito.lenient().when(repositoryProvider.getResourceRepository()).thenReturn(resourceRepository);
        Mockito.lenient().when(repositoryProvider.getPlatformMetricRepository()).thenReturn(platformMetricRepository);
        Mockito.lenient().when(repositoryProvider.getVpcRepository()).thenReturn(vpcRepository);
        Mockito.lenient().when(repositoryProvider.getPlatformMetricRepository()).thenReturn(platformMetricRepository);
        Mockito.lenient().when(repositoryProvider.getCredentialsRepository()).thenReturn(credentialsRepository);
        Mockito.lenient().when(repositoryProvider.getNamespaceRepository()).thenReturn(namespaceRepository);
    }
}
