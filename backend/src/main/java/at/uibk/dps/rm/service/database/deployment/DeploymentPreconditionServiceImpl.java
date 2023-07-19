package at.uibk.dps.rm.service.database.deployment;

import at.uibk.dps.rm.entity.dto.DeployResourcesRequest;
import at.uibk.dps.rm.entity.dto.credentials.DockerCredentials;
import at.uibk.dps.rm.entity.dto.deployment.FunctionResourceIds;
import at.uibk.dps.rm.entity.dto.deployment.ServiceResourceIds;
import at.uibk.dps.rm.entity.dto.resource.PlatformEnum;
import at.uibk.dps.rm.entity.dto.resource.ResourceTypeEnum;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.exception.UnauthorizedException;
import at.uibk.dps.rm.repository.account.CredentialsRepository;
import at.uibk.dps.rm.repository.deployment.DeploymentRepository;
import at.uibk.dps.rm.repository.function.FunctionRepository;
import at.uibk.dps.rm.repository.metric.PlatformMetricRepository;
import at.uibk.dps.rm.repository.resource.ResourceRepository;
import at.uibk.dps.rm.repository.resourceprovider.VPCRepository;
import at.uibk.dps.rm.repository.service.ServiceRepository;
import at.uibk.dps.rm.service.ServiceProxyAddress;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import at.uibk.dps.rm.util.validation.ServiceResultValidator;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.hibernate.Hibernate;
import org.hibernate.reactive.stage.Stage.Session;
import org.hibernate.reactive.stage.Stage.SessionFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is the implementation of the #DeploymentService.
 *
 * @author matthi-g
 */
public class DeploymentPreconditionServiceImpl extends DatabaseServiceProxy<Deployment> implements DeploymentPreconditionService {

    private final FunctionRepository functionRepository;

    private final ServiceRepository serviceRepository;

    private final ResourceRepository resourceRepository;

    private final PlatformMetricRepository platformMetricRepository;

    private final VPCRepository vpcRepository;

    private final CredentialsRepository credentialsRepository;

    /**
     * Create an instance from the deploymentRepository.
     *
     * @param repository the deployment repository
     */
    public DeploymentPreconditionServiceImpl(DeploymentRepository repository, FunctionRepository functionRepository,
            ServiceRepository serviceRepository, ResourceRepository resourceRepository,
            PlatformMetricRepository platformMetricRepository, VPCRepository vpcRepository,
            CredentialsRepository credentialsRepository, SessionFactory sessionFactory) {
        super(repository, Deployment.class, sessionFactory);
        this.functionRepository = functionRepository;
        this.serviceRepository = serviceRepository;
        this.resourceRepository = resourceRepository;
        this.platformMetricRepository = platformMetricRepository;
        this.vpcRepository = vpcRepository;
        this.credentialsRepository = credentialsRepository;
    }

    @Override
    public String getServiceProxyAddress() {
        return  ServiceProxyAddress.getServiceProxyAddress("deployment-precondition");
    }

    @Override
    public Future<JsonArray> checkDeploymentIsValid(long accountId, DeployResourcesRequest requestDTO) {
        List<String> functionResourceTypes = List.of(ResourceTypeEnum.FAAS.getValue());
        List<String> serviceResourceTypes = List.of(ResourceTypeEnum.CONTAINER.getValue());
        Set<Long> functionIds = requestDTO.getFunctionResources().stream()
            .map(FunctionResourceIds::getFunctionId)
            .collect(Collectors.toSet());
        Set<Long> serviceIds = requestDTO.getServiceResources().stream()
            .map(ServiceResourceIds::getServiceId)
            .collect(Collectors.toSet());
        Set<Long> serviceResourceIds = requestDTO.getServiceResources().stream()
            .map(ServiceResourceIds::getResourceId)
            .collect(Collectors.toSet());
        Set<Long> functionResourceIds = requestDTO.getFunctionResources().stream()
            .map(FunctionResourceIds::getResourceId)
            .collect(Collectors.toSet());
        List<Long> allResourceIds = Stream.of(functionResourceIds, serviceResourceIds)
            .flatMap(Set::stream)
            .collect(Collectors.toList());
        CompletionStage<List<Resource>> checkDeployment = withSession(session ->
            functionRepository.findAllByIds(session, functionIds)
                .thenAccept(functions -> {
                    if (functions.size() < functionIds.size()) {
                        throw new NotFoundException(Function.class);
                    }
                })
                .thenCompose(result -> serviceRepository.findAllByIds(session, serviceIds)
                    .thenAccept(services -> {
                        if (services.size() < serviceIds.size()) {
                            throw new NotFoundException(Service.class);
                        }
                    })
                )
                .thenCompose(result -> resourceRepository.findAllByResourceIdsAndResourceTypes(session,
                        serviceResourceIds, serviceResourceTypes)
                    .thenAccept(resources -> {
                        if (resources.size() < serviceResourceIds.size()) {
                            throw new NotFoundException(Resource.class);
                        }
                    })
                )
                .thenCompose(result -> resourceRepository.findAllByResourceIdsAndResourceTypes(session,
                        functionResourceIds, functionResourceTypes)
                    .thenAccept(resources -> {
                        if (resources.size() < functionResourceIds.size()) {
                            throw new NotFoundException(Resource.class);
                        }
                    })
                )
                .thenCompose(result -> resourceRepository.findAllByResourceIdsAndFetch(session, allResourceIds))
                .thenCompose(resources -> {
                    List<CompletableFuture<Void>> checks = new ArrayList<>();
                    checks.add(checkResourcesForDeployment(session, accountId, resources, requestDTO.getCredentials()
                        .getDockerCredentials()));
                    checks.add(checkMissingRequiredMetrics(session, resources));
                    return CompletableFuture.allOf(checks.toArray(new CompletableFuture[0]))
                        .thenApply(res -> {
                            for (Resource resource: resources) {
                                Region region = Hibernate.unproxy(resource.getRegion(), Region.class);
                                Platform platform = Hibernate.unproxy(resource.getPlatform(), Platform.class);
                                resource.setRegion(region);
                                resource.setPlatform(platform);
                            }
                            return resources;
                    });
                })
        );

        return sessionToFuture(checkDeployment)
            .map(resources -> {
                ArrayList<JsonObject> objects = new ArrayList<>();
                for (Resource resource: resources) {
                    resource.getRegion().getResourceProvider().setProviderPlatforms(null);
                    objects.add(JsonObject.mapFrom(resource));
                }
                return new JsonArray(objects);
            });
    }

    private CompletableFuture<Void> checkResourcesForDeployment(Session session, long accountId,
        List<Resource> resources, List<DockerCredentials> dockerCredentials) {
        List<CompletionStage<Void>> completionStages = new ArrayList<>();
        HashSet<Long> resourceProviderIds = new HashSet<>();
        HashSet<Long> regionIds = new HashSet<>();
        HashSet<Long> platformIds = new HashSet<>();
        for (Resource resource: resources) {
            long providerId = resource.getRegion().getResourceProvider().getProviderId();
            long regionId = resource.getRegion().getRegionId();
            PlatformEnum platform = PlatformEnum.fromPlatform(resource.getPlatform());
            checkCloudCredentials(session, accountId, providerId, platform, resourceProviderIds, completionStages);
            checkDockerCredentials(dockerCredentials, resource.getPlatform().getPlatformId(), platform, platformIds);
            checkMissingVPC(session, accountId, regionId, platform, regionIds, completionStages);
        }

        return CompletableFuture.allOf(completionStages.toArray(new CompletableFuture[0]));
    }

    private void checkCloudCredentials(Session session, long accountId, long providerId, PlatformEnum platform,
            Set<Long> resourceProviderIds, List<CompletionStage<Void>> completionStages) {
        if (resourceProviderIds.add(providerId) && (platform.equals(PlatformEnum.LAMBDA) ||
                platform.equals(PlatformEnum.EC2))) {
            completionStages.add(credentialsRepository.findByAccountIdAndProviderId(session, accountId, providerId)
                .thenAccept(credentials -> {
                    if (credentials == null) {
                        throw new UnauthorizedException("missing credentials for " + platform);
                    }
                }));
        }
    }

    private void checkDockerCredentials(List<DockerCredentials> dockerCredentials, long platformId,
            PlatformEnum platform, Set<Long> platformIds) {
        if (platformIds.add(platformId) && (platform.equals(PlatformEnum.EC2)) ||
                platform.equals(PlatformEnum.OPENFAAS)) {
            if (dockerCredentials == null || dockerCredentials.isEmpty()) {
                throw new UnauthorizedException("missing docker credentials for " + platform);
            }
        }
    }

    private void checkMissingVPC(Session session, long accountId, long regionId, PlatformEnum platform,
        Set<Long> regionIds, List<CompletionStage<Void>> completionStages) {
        if (regionIds.add(regionId) && platform.equals(PlatformEnum.EC2)) {
            completionStages.add(vpcRepository.findByRegionIdAndAccountId(session, regionId, accountId)
                .thenAccept(vpc -> ServiceResultValidator.checkFound(vpc, VPC.class)));
        }
    }

    private CompletableFuture<Void> checkMissingRequiredMetrics(Session session, List<Resource> resources) {
        List<CompletionStage<Void>> checkMissingRequiredMetrics = resources.stream()
            .map(resource -> platformMetricRepository
                .countMissingRequiredMetricValuesByResourceId(session, resource.getResourceId())
                .thenAccept(missingRequiredMetrics -> {
                    if (missingRequiredMetrics > 0) {
                        throw new NotFoundException("missing required metrics for resource (" +
                            resource.getResourceId() + ")");
                    }
                }).toCompletableFuture())
            .collect(Collectors.toList());
        return CompletableFuture.allOf(checkMissingRequiredMetrics.toArray(new CompletableFuture[0]));
    }
}
