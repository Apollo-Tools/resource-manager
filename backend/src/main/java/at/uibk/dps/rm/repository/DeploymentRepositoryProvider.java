package at.uibk.dps.rm.repository;

import at.uibk.dps.rm.repository.account.CredentialsRepository;
import at.uibk.dps.rm.repository.account.NamespaceRepository;
import at.uibk.dps.rm.repository.deployment.*;
import at.uibk.dps.rm.repository.function.FunctionRepository;
import at.uibk.dps.rm.repository.metric.PlatformMetricRepository;
import at.uibk.dps.rm.repository.resource.ResourceRepository;
import at.uibk.dps.rm.repository.resourceprovider.VPCRepository;
import at.uibk.dps.rm.repository.service.ServiceRepository;
import lombok.Getter;

/**
 * Provides all repositories that are necessary to validate, modify and persist deployments.
 *
 * @author matthi-g
 */
@Getter
public class DeploymentRepositoryProvider {

    private final DeploymentRepository deploymentRepository;
    private final ResourceDeploymentRepository resourceDeploymentRepository;
    private final FunctionDeploymentRepository functionDeploymentRepository;
    private final ServiceDeploymentRepository serviceDeploymentRepository;
    private final ResourceDeploymentStatusRepository statusRepository;
    private final FunctionRepository functionRepository;
    private final ServiceRepository serviceRepository;
    private final ResourceRepository resourceRepository;
    private final PlatformMetricRepository platformMetricRepository;
    private final VPCRepository vpcRepository;
    private final CredentialsRepository credentialsRepository;
    private final NamespaceRepository namespaceRepository;

    /**
     * Create an instance.
     */
    public DeploymentRepositoryProvider() {
        deploymentRepository = new DeploymentRepository();
        resourceDeploymentRepository = new ResourceDeploymentRepository();
        functionDeploymentRepository = new FunctionDeploymentRepository();
        serviceDeploymentRepository = new ServiceDeploymentRepository();
        statusRepository = new ResourceDeploymentStatusRepository();
        functionRepository = new FunctionRepository();
        serviceRepository = new ServiceRepository();
        resourceRepository = new ResourceRepository();
        platformMetricRepository = new PlatformMetricRepository();
        vpcRepository = new VPCRepository();
        credentialsRepository = new CredentialsRepository();
        namespaceRepository = new NamespaceRepository();
    }
}
