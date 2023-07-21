package at.uibk.dps.rm.service.database.deployment;

import at.uibk.dps.rm.entity.deployment.DeploymentStatusValue;
import at.uibk.dps.rm.entity.dto.DeployResourcesRequest;
import at.uibk.dps.rm.entity.dto.credentials.DockerCredentials;
import at.uibk.dps.rm.entity.dto.credentials.KubeConfig;
import at.uibk.dps.rm.entity.dto.credentials.k8s.Cluster;
import at.uibk.dps.rm.entity.dto.credentials.k8s.Context;
import at.uibk.dps.rm.entity.dto.deployment.DeploymentResponse;
import at.uibk.dps.rm.entity.dto.deployment.DeploymentWithResourcesDTO;
import at.uibk.dps.rm.entity.dto.deployment.FunctionResourceIds;
import at.uibk.dps.rm.entity.dto.deployment.ServiceResourceIds;
import at.uibk.dps.rm.entity.dto.resource.PlatformEnum;
import at.uibk.dps.rm.entity.dto.resource.ResourceTypeEnum;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.exception.BadInputException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.exception.UnauthorizedException;
import at.uibk.dps.rm.repository.account.AccountRepository;
import at.uibk.dps.rm.repository.account.CredentialsRepository;
import at.uibk.dps.rm.repository.deployment.*;
import at.uibk.dps.rm.repository.function.FunctionRepository;
import at.uibk.dps.rm.repository.metric.PlatformMetricRepository;
import at.uibk.dps.rm.repository.resource.ResourceRepository;
import at.uibk.dps.rm.repository.resourceprovider.VPCRepository;
import at.uibk.dps.rm.repository.service.ServiceRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import at.uibk.dps.rm.util.misc.MetricValueMapper;
import at.uibk.dps.rm.util.validation.ServiceResultValidator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
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
public class DeploymentServiceImpl extends DatabaseServiceProxy<Deployment> implements DeploymentService {

    private final DeploymentRepository repository;

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

    private final AccountRepository accountRepository;

    /**
     * Create an instance from the deploymentRepository.
     *
     * @param repository the deployment repository
     */
    public DeploymentServiceImpl(DeploymentRepository repository,
            ResourceDeploymentRepository resourceDeploymentRepository,
            FunctionDeploymentRepository functionDeploymentRepository,
            ServiceDeploymentRepository serviceDeploymentRepository,
            ResourceDeploymentStatusRepository statusRepository, FunctionRepository functionRepository,
            ServiceRepository serviceRepository, ResourceRepository resourceRepository,
            PlatformMetricRepository platformMetricRepository, VPCRepository vpcRepository,
            CredentialsRepository credentialsRepository, AccountRepository accountRepository,
            SessionFactory sessionFactory) {
        super(repository, Deployment.class, sessionFactory);
        this.repository = repository;
        this.resourceDeploymentRepository = resourceDeploymentRepository;
        this.functionDeploymentRepository = functionDeploymentRepository;
        this.serviceDeploymentRepository = serviceDeploymentRepository;
        this.statusRepository = statusRepository;
        this.functionRepository = functionRepository;
        this.serviceRepository = serviceRepository;
        this.resourceRepository = resourceRepository;
        this.platformMetricRepository = platformMetricRepository;
        this.vpcRepository = vpcRepository;
        this.credentialsRepository = credentialsRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    public Future<JsonObject> cancelDeployment(long id, long accountId) {
        CompletionStage<Deployment> update = withTransaction(session ->
            repository.findByIdAndAccountId(session, id, accountId)
                .thenCompose(deployment -> {
                    ServiceResultValidator.checkFound(deployment, Deployment.class);
                    return resourceDeploymentRepository.findAllByDeploymentIdAndFetch(session, id)
                        .thenCompose(resourceDeployments -> {
                            long deployedAmount = resourceDeployments.stream().filter(resourceDeployment ->
                                DeploymentStatusValue.fromDeploymentStatus(resourceDeployment.getStatus())
                                    .equals(DeploymentStatusValue.DEPLOYED))
                                .count();
                            if (resourceDeployments.isEmpty() || deployedAmount != resourceDeployments.size()) {
                                throw new BadInputException("invalid deployment state");
                            }
                            return statusRepository.findOneByStatusValue(session,
                                    DeploymentStatusValue.TERMINATING.getValue())
                                .thenAccept(status -> resourceDeployments
                                    .forEach(resourceDeployment -> resourceDeployment.setStatus(status)));
                        })
                        .thenApply(res -> deployment);
                })
        );
        return transactionToFuture(update).map(deployment -> {
            deployment.setCreatedBy(null);
            return JsonObject.mapFrom(deployment);
        });
    }

    @Override
    public Future<JsonObject> saveToAccount(long accountId, JsonObject data) {
        DeployResourcesRequest requestDTO = data.mapTo(DeployResourcesRequest.class);
        Deployment deployment = new Deployment();
        CompletionStage<Deployment> save = withTransaction(session ->
            accountRepository.findById(session, accountId)
                .thenCompose(account -> {
                    if (account == null) {
                        throw new UnauthorizedException();
                    }
                    deployment.setIsActive(true);
                    deployment.setCreatedBy(account);
                    return session.persist(deployment);
                })
                .thenCompose(result -> checkDeploymentIsValid(session, accountId, requestDTO))
                .thenCompose(resources -> statusRepository
                    .findOneByStatusValue(session, DeploymentStatusValue.NEW.name())
                        .thenApply(status -> {
                            ServiceResultValidator.checkFound(status, ResourceDeploymentStatus.class);
                            return status;
                        })
                        .thenCompose(statusNew -> {
                            CompletableFuture<Void> saveFunctionDeployments = saveFunctionDeployments(session, deployment,
                                requestDTO, statusNew);
                            CompletableFuture<Void> saveServiceDeployments = saveServiceDeployments(session, deployment,
                                requestDTO, statusNew, resources);
                            return CompletableFuture.allOf(saveFunctionDeployments, saveServiceDeployments);
                        })
                )
                .thenApply(res -> deployment)
        );
        return transactionToFuture(save)
            .map(persisted -> {
                persisted.setCreatedBy(null);
                return JsonObject.mapFrom(deployment);
            });
    }

    @Override
    public Future<JsonArray> findAllByAccountId(long accountId) {
        List<DeploymentResponse> deploymentResponses = new ArrayList<>();
        CompletionStage<List<DeploymentResponse>> findAll = withSession(session ->
            repository.findAllByAccountId(session, accountId)
                .thenCompose(deployments -> {
                    List<CompletableFuture<Void>> completables = new ArrayList<>();
                    for (Deployment deployment : deployments) {
                        completables.add(
                            composeDeploymentResponse(session, deployment, deploymentResponses)
                        );
                    }
                    return CompletableFuture.allOf(completables.toArray(CompletableFuture[]::new));
                })
                .thenApply(res -> deploymentResponses)
        );
        return sessionToFuture(findAll)
            .map(result -> {
                ArrayList<JsonObject> objects = new ArrayList<>();
                for (DeploymentResponse entity: result) {
                    objects.add(JsonObject.mapFrom(entity));
                }
                return new JsonArray(objects);
            });
    }

    private CompletableFuture<Void> composeDeploymentResponse(Session session, Deployment deployment,
            List<DeploymentResponse> deploymentResponses) {
        DeploymentResponse deploymentResponse = new DeploymentResponse();
        deploymentResponse.setDeploymentId(deployment.getDeploymentId());
        deploymentResponse.setCreatedAt(deployment.getCreatedAt());
        deploymentResponses.add(deploymentResponse);
        return resourceDeploymentRepository.findAllByDeploymentIdAndFetch(session,
            deployment.getDeploymentId())
            .thenAccept(resourceDeployments -> {
                DeploymentStatusValue crucialDeploymentStatus =
                    checkCrucialResourceDeploymentStatus(resourceDeployments);
                deploymentResponse.setStatusValue(crucialDeploymentStatus);
            })
            .toCompletableFuture();
    }


    /**
     * Get the crucial resource deployment status based on all resource deployments of a single
     * deployment.
     *
     * @param resourceDeployments the resource deployments
     * @return the crucial deployment status
     */
    public DeploymentStatusValue checkCrucialResourceDeploymentStatus(List<ResourceDeployment> resourceDeployments) {
        if (matchAnyResourceDeploymentsStatus(resourceDeployments,
            DeploymentStatusValue.ERROR)) {
            return DeploymentStatusValue.ERROR;
        }
        if (matchAnyResourceDeploymentsStatus(resourceDeployments, DeploymentStatusValue.NEW)) {
            return DeploymentStatusValue.NEW;
        }

        if (matchAnyResourceDeploymentsStatus(resourceDeployments,
            DeploymentStatusValue.TERMINATING)) {
            return DeploymentStatusValue.TERMINATING;
        }

        if (matchAnyResourceDeploymentsStatus(resourceDeployments,
            DeploymentStatusValue.DEPLOYED)) {
            return DeploymentStatusValue.DEPLOYED;
        }
        return DeploymentStatusValue.TERMINATED;
    }

    /**
     * Check if at least one status of resourceDeployments matches the given status value.
     *
     * @param resourceDeployments the resource deployments
     * @param statusValue the status value
     * @return true if at least one match was found, else false
     */
    private boolean matchAnyResourceDeploymentsStatus(List<ResourceDeployment> resourceDeployments,
        DeploymentStatusValue statusValue) {
        return resourceDeployments.stream()
            .anyMatch(rd -> DeploymentStatusValue.fromDeploymentStatus(rd.getStatus()).equals(statusValue));
    }


    @Override
    public Future<JsonObject> findOneByIdAndAccountId(long id, long accountId) {
        DeploymentWithResourcesDTO result = new DeploymentWithResourcesDTO();
        CompletionStage<DeploymentWithResourcesDTO> findOne = withSession(session ->
            repository.findByIdAndAccountId(session, id, accountId)
                .thenCompose(deployment -> {
                    ServiceResultValidator.checkFound(deployment, Deployment.class);
                    result.setDeploymentId(id);
                    result.setIsActive(deployment.getIsActive());
                    result.setCreatedAt(deployment.getCreatedAt());
                    return functionDeploymentRepository.findAllByDeploymentId(session, id);
                })
                .thenCompose(functionDeployments -> {
                    result.setFunctionResources(functionDeployments);
                    return serviceDeploymentRepository.findAllByDeploymentId(session, id);
                })
                .thenApply(serviceDeployments -> {
                    result.setServiceResources(serviceDeployments);
                    return result;
                })
        );
        return sessionToFuture(findOne)
            .map(foundDeployment -> {
                prepareResourceDeployments(foundDeployment.getFunctionResources());
                prepareResourceDeployments(foundDeployment.getServiceResources());
                return JsonObject.mapFrom(foundDeployment);
            });
    }

    private void prepareResourceDeployments(List<? extends ResourceDeployment> resourceDeployments) {
        resourceDeployments.forEach(resourceDeployment -> {
            resourceDeployment.setDeployment(null);
            resourceDeployment.getResource().getRegion().getResourceProvider()
                .setProviderPlatforms(null);
        });
    }

    private CompletionStage<List<Resource>> checkDeploymentIsValid(Session session, long accountId,
        DeployResourcesRequest requestDTO) {
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

        return functionRepository.findAllByIds(session, functionIds)
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
                CompletableFuture<Void> checkResources = checkResourcesForDeployment(session, accountId, resources,
                    requestDTO.getCredentials().getDockerCredentials());
                CompletableFuture<Void> checkMetrics = checkMissingRequiredMetrics(session, resources);
                return CompletableFuture.allOf(checkResources, checkMetrics)
                    .thenApply(res -> {
                        for (Resource resource: resources) {
                            Region region = Hibernate.unproxy(resource.getRegion(), Region.class);
                            Platform platform = Hibernate.unproxy(resource.getPlatform(), Platform.class);
                            resource.setRegion(region);
                            resource.setPlatform(platform);
                        }
                        return resources;
                    });
            });
    }

    private CompletableFuture<Void> checkResourcesForDeployment(Session session, long accountId,
        List<Resource> resources, List<DockerCredentials> dockerCredentials) {
        List<CompletableFuture<Void>> completables = new ArrayList<>();
        HashSet<Long> resourceProviderIds = new HashSet<>();
        HashSet<Long> regionIds = new HashSet<>();
        HashSet<Long> platformIds = new HashSet<>();
        for (Resource resource: resources) {
            long providerId = resource.getRegion().getResourceProvider().getProviderId();
            long regionId = resource.getRegion().getRegionId();
            PlatformEnum platform = PlatformEnum.fromPlatform(resource.getPlatform());
            checkCloudCredentials(session, accountId, providerId, platform, resourceProviderIds, completables);
            checkDockerCredentials(dockerCredentials, resource.getPlatform().getPlatformId(), platform, platformIds);
            checkMissingVPC(session, accountId, regionId, platform, regionIds, completables);
        }
        return CompletableFuture.allOf(completables.toArray(CompletableFuture[]::new));
    }

    private void checkCloudCredentials(Session session, long accountId, long providerId, PlatformEnum platform,
        Set<Long> resourceProviderIds, List<CompletableFuture<Void>> completables) {
        if (resourceProviderIds.add(providerId) && (platform.equals(PlatformEnum.LAMBDA) ||
            platform.equals(PlatformEnum.EC2))) {
            completables.add(credentialsRepository.findByAccountIdAndProviderId(session, accountId, providerId)
                .thenAccept(credentials -> {
                    if (credentials == null) {
                        throw new UnauthorizedException("missing credentials for " + platform);
                    }
                })
                .toCompletableFuture()
            );
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
        Set<Long> regionIds, List<CompletableFuture<Void>> completables) {
        if (regionIds.add(regionId) && platform.equals(PlatformEnum.EC2)) {
            completables.add(vpcRepository.findByRegionIdAndAccountId(session, regionId, accountId)
                .thenAccept(vpc -> ServiceResultValidator.checkFound(vpc, VPC.class))
                .toCompletableFuture()
            );
        }
    }

    private CompletableFuture<Void> checkMissingRequiredMetrics(Session session, List<Resource> resources) {
        List<CompletableFuture<Void>> checkMissingRequiredMetrics = resources.stream()
            .map(resource -> platformMetricRepository
                .countMissingRequiredMetricValuesByResourceId(session, resource.getResourceId())
                .thenAccept(missingRequiredMetrics -> {
                    if (missingRequiredMetrics > 0) {
                        throw new NotFoundException("missing required metrics for resource (" +
                            resource.getResourceId() + ")");
                    }
                }).toCompletableFuture())
            .collect(Collectors.toList());
        return CompletableFuture.allOf(checkMissingRequiredMetrics.toArray(CompletableFuture[]::new));
    }

    private CompletableFuture<Void> saveFunctionDeployments(Session session, Deployment deployment,
        DeployResourcesRequest request, ResourceDeploymentStatus status) {
        List<FunctionDeployment> functionDeployments = request.getFunctionResources()
            .stream()
            .map(functionResourceIds -> createNewResourceDeployment(deployment, functionResourceIds, status))
            .collect(Collectors.toList());
        return functionDeploymentRepository.createAll(session, functionDeployments).toCompletableFuture();
    }

    private CompletableFuture<Void> saveServiceDeployments(Session session, Deployment deployment,
        DeployResourcesRequest request, ResourceDeploymentStatus status, List<Resource> resources) {
        if (request.getServiceResources().isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }
        KubeConfig kubeConfig = deserializeKubeConfig(request);
        List<ServiceDeployment> serviceDeployments = request.getServiceResources()
            .stream()
            .map(serviceResourceIds -> {
                Resource resource = resources.stream()
                    .filter(r -> r.getResourceId() == serviceResourceIds.getResourceId())
                    .findFirst()
                    .orElseThrow(IllegalStateException::new);
                return createNewResourceDeployment(deployment, resource, serviceResourceIds, kubeConfig, status);
            })
            .collect(Collectors.toList());
        return serviceDeploymentRepository.createAll(session, serviceDeployments).toCompletableFuture();
    }

    private Context getContextByClusterUrl(KubeConfig kubeConfig, String clusterUrl) {
        Cluster cluster = kubeConfig.getClusters().stream()
            .filter(c -> c.getCluster().getServer().equals(clusterUrl))
            .findFirst()
            .orElseThrow(() -> new UnauthorizedException("Cluster url not found in kube config"));
        return kubeConfig.getContexts().stream()
            .filter(c -> c.getContext().getCluster().equals(cluster.getName()))
            .findFirst()
            .orElseThrow(() -> new UnauthorizedException("No suitable context found in kube config"));
    }

    private FunctionDeployment createNewResourceDeployment(Deployment deployment, FunctionResourceIds ids,
        ResourceDeploymentStatus status) {
        Resource resource = new Resource();
        resource.setResourceId(ids.getResourceId());
        Function function = new Function();
        function.setFunctionId(ids.getFunctionId());
        FunctionDeployment functionDeployment = new FunctionDeployment();
        functionDeployment.setDeployment(deployment);
        functionDeployment.setResource(resource);
        functionDeployment.setFunction(function);
        functionDeployment.setStatus(status);
        return functionDeployment;
    }

    private ServiceDeployment createNewResourceDeployment(Deployment deployment, Resource resource,
            ServiceResourceIds ids, KubeConfig kubeConfig, ResourceDeploymentStatus status) {
        Map<String, MetricValue> metricValues = MetricValueMapper.mapMetricValues(resource.getMetricValues());
        String clusterUrl = metricValues.get("cluster-url").getValueString();
        Context context = getContextByClusterUrl(kubeConfig, clusterUrl);
        String namespace = context.getContext().getNamespace() != null ? context.getContext().getNamespace() :
            "default";
        Service service = new Service();
        service.setServiceId(ids.getServiceId());
        ServiceDeployment serviceDeployment = new ServiceDeployment();
        serviceDeployment.setDeployment(deployment);
        serviceDeployment.setResource(resource);
        serviceDeployment.setService(service);
        serviceDeployment.setStatus(status);
        serviceDeployment.setNamespace(namespace);
        serviceDeployment.setContext(context.getName());
        return serviceDeployment;
    }

    private KubeConfig deserializeKubeConfig(DeployResourcesRequest request) {
        try {
            return new YAMLMapper().readValue(request.getCredentials().getKubeConfig(), KubeConfig.class);
        } catch (JsonProcessingException e) {
            throw new BadInputException("Unsupported schema of kube config");
        }
    }
}
