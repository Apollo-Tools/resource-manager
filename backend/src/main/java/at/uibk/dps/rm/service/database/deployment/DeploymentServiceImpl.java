package at.uibk.dps.rm.service.database.deployment;

import at.uibk.dps.rm.entity.deployment.DeploymentStatusValue;
import at.uibk.dps.rm.entity.deployment.output.DeploymentOutput;
import at.uibk.dps.rm.entity.dto.DeployResourcesRequest;
import at.uibk.dps.rm.entity.dto.credentials.DockerCredentials;
import at.uibk.dps.rm.entity.dto.deployment.*;
import at.uibk.dps.rm.entity.dto.resource.PlatformEnum;
import at.uibk.dps.rm.entity.dto.resource.ResourceTypeEnum;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.exception.BadInputException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.exception.UnauthorizedException;
import at.uibk.dps.rm.repository.DeploymentRepositoryProvider;
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
@Deprecated
public class DeploymentServiceImpl extends DatabaseServiceProxy<Deployment> implements DeploymentService {

    private final DeploymentRepositoryProvider repositoryProvider;

    /**
     * Create an instance from the repository provider
     *
     * @param repositoryProvider the necessary repositories
     */
    public DeploymentServiceImpl(DeploymentRepositoryProvider repositoryProvider,
            SessionFactory sessionFactory) {
        super(repositoryProvider.getDeploymentRepository(), Deployment.class, sessionFactory);
        this.repositoryProvider = repositoryProvider;
    }

    @Override
    public Future<JsonObject> cancelDeployment(long id, long accountId) {
        TerminateResourcesDTO terminateResources = new TerminateResourcesDTO();
        terminateResources.setFunctionDeployments(new ArrayList<>());
        terminateResources.setServiceDeployments(new ArrayList<>());
        CompletionStage<TerminateResourcesDTO> update = withTransaction(session ->
            repositoryProvider.getDeploymentRepository()
                .findByIdAndAccountId(session, id, accountId)
                .thenCompose(deployment -> {
                    ServiceResultValidator.checkFound(deployment, Deployment.class);
                    terminateResources.setDeployment(deployment);
                    return repositoryProvider.getResourceDeploymentRepository()
                        .findAllByDeploymentIdAndFetch(session, id)
                        .thenCompose(resourceDeployments -> {
                            long deployedAmount = resourceDeployments.stream().filter(resourceDeployment ->
                                DeploymentStatusValue.fromDeploymentStatus(resourceDeployment.getStatus())
                                    .equals(DeploymentStatusValue.DEPLOYED))
                                .count();
                            if (resourceDeployments.isEmpty() || deployedAmount != resourceDeployments.size()) {
                                throw new BadInputException("invalid deployment state");
                            }
                            return repositoryProvider.getStatusRepository()
                                .findOneByStatusValue(session, DeploymentStatusValue.TERMINATING.getValue())
                                .thenAccept(status -> resourceDeployments
                                    .forEach(resourceDeployment -> resourceDeployment.setStatus(status)));
                        })
                        .thenCompose(res -> repositoryProvider.getCredentialsRepository()
                            .findAllByAccountId(session, accountId)
                            .thenAccept(terminateResources::setCredentialsList))
                        .thenCompose(res -> mapResourceDeploymentsToDTO(session, terminateResources))
                        .thenApply(res -> terminateResources);
                })
        );
        return sessionToFuture(update)
            .map(JsonObject::mapFrom);
    }

    @Override
    public Future<JsonArray> findAllByAccountId(long accountId) {
        List<DeploymentResponse> deploymentResponses = new ArrayList<>();
        CompletionStage<List<DeploymentResponse>> findAll = withSession(session ->
            repositoryProvider.getDeploymentRepository()
                .findAllByAccountId(session, accountId)
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
        return repositoryProvider.getResourceDeploymentRepository()
            .findAllByDeploymentIdAndFetch(session, deployment.getDeploymentId())
            .thenAccept(resourceDeployments -> {
                DeploymentStatusValue crucialDeploymentStatus =
                    checkCrucialResourceDeploymentStatus(resourceDeployments);
                deploymentResponse.setStatusValue(crucialDeploymentStatus);
            })
            .toCompletableFuture();
    }


    // TODO: refactor into separate class
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

    // TODO: refactor into separate class
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
            repositoryProvider.getDeploymentRepository()
                .findByIdAndAccountId(session, id, accountId)
                .thenCompose(deployment -> {
                    ServiceResultValidator.checkFound(deployment, Deployment.class);
                    result.setDeploymentId(id);
                    result.setIsActive(deployment.getIsActive());
                    result.setCreatedAt(deployment.getCreatedAt());
                    return repositoryProvider.getFunctionDeploymentRepository().findAllByDeploymentId(session, id);
                })
                .thenCompose(functionDeployments -> {
                    result.setFunctionResources(functionDeployments);
                    return repositoryProvider.getServiceDeploymentRepository().findAllByDeploymentId(session, id);
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
        resourceDeployments.forEach(resourceDeployment -> resourceDeployment.setDeployment(null));
    }

    @Override
    public Future<JsonObject> saveToAccount(long accountId, JsonObject data) {
        DeployResourcesRequest request = data.mapTo(DeployResourcesRequest.class);
        DeployResourcesDTO deployResources = new DeployResourcesDTO();
        Deployment deployment = new Deployment();
        deployResources.setDeployment(deployment);
        deployResources.setDeploymentCredentials(request.getCredentials());
        deployResources.setVpcList(new ArrayList<>());
        CompletionStage<DeployResourcesDTO> save = withTransaction(session ->
            repositoryProvider.getAccountRepository().findById(session, accountId)
                .thenCompose(account -> {
                    if (account == null) {
                        throw new UnauthorizedException();
                    }
                    deployment.setIsActive(true);
                    deployment.setCreatedBy(account);
                    return session.persist(deployment)
                        .thenAccept(res -> session.flush());
                })
                .thenCompose(result -> checkDeploymentIsValid(session, accountId, request, deployResources))
                .thenCompose(resources -> repositoryProvider.getStatusRepository()
                    .findOneByStatusValue(session, DeploymentStatusValue.NEW.name())
                    .thenApply(status -> {
                        ServiceResultValidator.checkFound(status, ResourceDeploymentStatus.class);
                        return status;
                    })
                    .thenCompose(statusNew -> {
                        CompletableFuture<Void> saveFunctionDeployments = saveFunctionDeployments(session, deployment,
                            request, statusNew, resources);
                        CompletableFuture<Void> saveServiceDeployments = repositoryProvider.getNamespaceRepository()
                            .findAllByAccountIdAndFetch(session, deployment.getCreatedBy().getAccountId())
                            .thenCompose(namespaces -> saveServiceDeployments(session, deployment, request, statusNew,
                                namespaces, resources))
                            .toCompletableFuture();
                        return CompletableFuture.allOf(saveFunctionDeployments, saveServiceDeployments);
                    })
                )
                .thenCompose(res -> repositoryProvider.getCredentialsRepository().findAllByAccountId(session, accountId)
                    .thenAccept(deployResources::setCredentialsList))
        )
        .thenCompose(res -> withSession(session -> mapResourceDeploymentsToDTO(session, deployResources)
            .thenApply(result -> deployResources))
        );

        return sessionToFuture(save)
            .map(result -> {
                result.getDeployment().setCreatedBy(null);
                return JsonObject.mapFrom(result);
            });
    }

    private CompletionStage<List<Resource>> checkDeploymentIsValid(Session session, long accountId,
        DeployResourcesRequest requestDTO, DeployResourcesDTO deployResources) {
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

        return repositoryProvider.getFunctionRepository().findAllByIds(session, functionIds)
            .thenAccept(functions -> {
                if (functions.size() < functionIds.size()) {
                    throw new NotFoundException(Function.class);
                }
            })
            .thenCompose(result -> repositoryProvider.getServiceRepository().findAllByIds(session, serviceIds)
                .thenAccept(services -> {
                    if (services.size() < serviceIds.size()) {
                        throw new NotFoundException(Service.class);
                    }
                })
            )
            .thenCompose(result -> repositoryProvider.getResourceRepository()
                .findAllByResourceIdsAndResourceTypes(session,serviceResourceIds, serviceResourceTypes)
                .thenAccept(resources -> {
                    if (resources.size() < serviceResourceIds.size()) {
                        throw new NotFoundException(Resource.class);
                    }
                })
            )
            .thenCompose(result -> repositoryProvider.getResourceRepository()
                .findAllByResourceIdsAndResourceTypes(session, functionResourceIds, functionResourceTypes)
                .thenAccept(resources -> {
                    if (resources.size() < functionResourceIds.size()) {
                        throw new NotFoundException(Resource.class);
                    }
                })
            )
            .thenCompose(result -> repositoryProvider.getResourceRepository()
                .findAllByResourceIdsAndFetch(session, allResourceIds))
            .thenCompose(resources -> {
                CompletableFuture<Void> checkResources = checkResourcesForDeployment(session, accountId, resources,
                    deployResources);
                CompletableFuture<Void> checkMetrics = checkMissingRequiredMetrics(session, resources);
                return CompletableFuture.allOf(checkResources, checkMetrics)
                    .thenApply(res -> resources);
            });
    }

    private CompletableFuture<Void> checkResourcesForDeployment(Session session, long accountId,
        List<Resource> resources, DeployResourcesDTO deployResources) {
        List<CompletableFuture<Void>> completables = new ArrayList<>();
        HashSet<Long> resourceProviderIds = new HashSet<>();
        HashSet<Long> regionIds = new HashSet<>();
        HashSet<Long> platformIds = new HashSet<>();
        for (Resource resource: resources) {
            MainResource mainResource = resource.getMain();
            long providerId = mainResource.getRegion().getResourceProvider().getProviderId();
            long regionId = mainResource.getRegion().getRegionId();
            PlatformEnum platform = PlatformEnum.fromPlatform(mainResource.getPlatform());
            checkCloudCredentials(session, accountId, providerId, platform, resourceProviderIds, completables);
            checkDockerCredentials(deployResources.getDeploymentCredentials().getDockerCredentials(),
                mainResource.getPlatform().getPlatformId(), platform, platformIds);
            checkMissingVPC(session, accountId, regionId, platform, regionIds, deployResources, completables);
        }
        return CompletableFuture.allOf(completables.toArray(CompletableFuture[]::new));
    }

    private void checkCloudCredentials(Session session, long accountId, long providerId, PlatformEnum platform,
        Set<Long> resourceProviderIds, List<CompletableFuture<Void>> completables) {
        if (!resourceProviderIds.contains(providerId) && (platform.equals(PlatformEnum.LAMBDA) ||
            platform.equals(PlatformEnum.EC2))) {
            completables.add(repositoryProvider.getCredentialsRepository()
                .findByAccountIdAndProviderId(session, accountId, providerId)
                .thenAccept(credentials -> {
                    if (credentials == null) {
                        throw new UnauthorizedException("missing credentials for " + platform);
                    }
                })
                .toCompletableFuture()
            );
            resourceProviderIds.add(providerId);
        }
    }

    private void checkDockerCredentials(DockerCredentials dockerCredentials, long platformId, PlatformEnum platform,
            Set<Long> platformIds) {
        if (!platformIds.contains(platformId) && (platform.equals(PlatformEnum.EC2)) ||
            platform.equals(PlatformEnum.OPENFAAS)) {
            if (dockerCredentials == null) {
                throw new UnauthorizedException("missing docker credentials for " + platform);
            }
            platformIds.add(platformId);
        }
    }

    private void checkMissingVPC(Session session, long accountId, long regionId, PlatformEnum platform,
        Set<Long> regionIds, DeployResourcesDTO deployResourcesDTO, List<CompletableFuture<Void>> completables) {
        if (!regionIds.contains(regionId) && platform.equals(PlatformEnum.EC2)) {
            completables.add(repositoryProvider.getVpcRepository()
                .findByRegionIdAndAccountId(session, regionId, accountId)
                .thenAccept(vpc -> {
                    ServiceResultValidator.checkFound(vpc, VPC.class);
                    Region region = Hibernate.unproxy(vpc.getRegion(), Region.class);
                    vpc.setRegion(region);
                    deployResourcesDTO.getVpcList().add(vpc);
                })
                .toCompletableFuture()
            );
            regionIds.add(regionId);
        }
    }

    private CompletableFuture<Void> checkMissingRequiredMetrics(Session session, List<Resource> resources) {
        List<CompletableFuture<Void>> checkMissingRequiredMetrics = resources.stream()
            .map(resource -> {
                boolean isMainResource = resource.getMain().equals(resource);
                return repositoryProvider.getPlatformMetricRepository()
                    .countMissingRequiredMetricValuesByResourceId(session, resource.getResourceId(), isMainResource)
                    .thenAccept(missingRequiredMetrics -> {
                        if (missingRequiredMetrics > 0) {
                            throw new NotFoundException("missing required metrics for resource (" +
                                resource.getResourceId() + ")");
                        }
                    }).toCompletableFuture();
            })
            .collect(Collectors.toList());
        return CompletableFuture.allOf(checkMissingRequiredMetrics.toArray(CompletableFuture[]::new));
    }

    private CompletableFuture<Void> saveFunctionDeployments(Session session, Deployment deployment,
            DeployResourcesRequest request, ResourceDeploymentStatus status, List<Resource> resources) {
        List<FunctionDeployment> functionDeployments = request.getFunctionResources()
            .stream()
            .map(functionResourceIds -> {
                Resource resource = resources.stream()
                    .filter(r -> r.getResourceId() == functionResourceIds.getResourceId())
                    .findFirst()
                    .orElseThrow(IllegalStateException::new);
                return createNewResourceDeployment(deployment, functionResourceIds, status, resource);
            })
            .collect(Collectors.toList());
        return repositoryProvider.getFunctionDeploymentRepository().createAll(session, functionDeployments)
            .toCompletableFuture();
    }

    private CompletableFuture<Void> saveServiceDeployments(Session session, Deployment deployment,
            DeployResourcesRequest request, ResourceDeploymentStatus status, List<K8sNamespace> namespaces,
            List<Resource> resources) {
        if (request.getServiceResources().isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }
        List<ServiceDeployment> serviceDeployments = request.getServiceResources()
            .stream()
            .map(serviceResourceIds -> {
                Resource resource = resources.stream()
                    .filter(r -> r.getResourceId() == serviceResourceIds.getResourceId())
                    .findFirst()
                    .orElseThrow(IllegalStateException::new);
                return createNewResourceDeployment(deployment, resource, serviceResourceIds, namespaces, status);
            })
            .collect(Collectors.toList());
        return repositoryProvider.getServiceDeploymentRepository().createAll(session, serviceDeployments)
            .toCompletableFuture();
    }

    private FunctionDeployment createNewResourceDeployment(Deployment deployment, FunctionResourceIds ids,
        ResourceDeploymentStatus status, Resource resource) {
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
            ServiceResourceIds ids, List<K8sNamespace> namespaces, ResourceDeploymentStatus status) {
        K8sNamespace k8sNamespace = namespaces.stream()
            .filter(namespace -> namespace.getResource().getResourceId().equals(resource.getMain().getResourceId()))
            .findFirst()
            .orElseThrow(() -> new UnauthorizedException("missing namespace for resource " + resource.getName() + " (" +
                resource.getResourceId() + ")"));
        Service service = new Service();
        service.setServiceId(ids.getServiceId());
        ServiceDeployment serviceDeployment = new ServiceDeployment();
        serviceDeployment.setDeployment(deployment);
        serviceDeployment.setResource(resource);
        serviceDeployment.setService(service);
        serviceDeployment.setStatus(status);
        serviceDeployment.setNamespace(k8sNamespace.getNamespace());
        serviceDeployment.setContext("");
        return serviceDeployment;
    }


    /**
     * Map resource deployments to a deploy/terminate request.
     *
     * @param request the request
     * @return the request with the mapped values
     */
    private CompletionStage<Void> mapResourceDeploymentsToDTO(Session session, DeployTerminateDTO request) {
        long deploymentId = request.getDeployment().getDeploymentId();
        return repositoryProvider.getFunctionDeploymentRepository().findAllByDeploymentId(session, deploymentId)
            .thenAccept(request::setFunctionDeployments)
            .thenCompose(res -> repositoryProvider.getServiceDeploymentRepository().findAllByDeploymentId(session,
                deploymentId)
                .thenCompose(serviceDeployments -> {
                    List<CompletableFuture<Void>> completables = new ArrayList<>();
                    for (ServiceDeployment deployment : serviceDeployments) {
                        completables.add(session.fetch(deployment.getService().getEnvVars())
                                .thenCompose(envVars -> session.fetch(deployment.getService().getVolumeMounts()))
                                .thenAccept(volumeMounts -> {})
                                .toCompletableFuture()
                        );
                    }
                    return CompletableFuture.allOf(completables.toArray(CompletableFuture[]::new))
                        .thenApply(res1 -> serviceDeployments);
                }))
            .thenAccept(request::setServiceDeployments);
    }

    @Override
    public Future<Void> handleDeploymentError(long id, String errorMessage) {
        CompletionStage<Void> handleError = withTransaction(session -> {
            CompletableFuture<Integer> updateStatus = repositoryProvider.getResourceDeploymentRepository()
                .updateDeploymentStatusByDeploymentId(session, id, DeploymentStatusValue.ERROR)
                .toCompletableFuture();
            Log log = new Log();
            log.setLogValue(errorMessage);
            DeploymentLog deploymentLog = new DeploymentLog();
            deploymentLog.setLog(log);
            Deployment deployment = new Deployment();
            deployment.setDeploymentId(id);
            deploymentLog.setDeployment(deployment);
            CompletableFuture<Void> createLog = session.persist(log)
                .thenCompose(res -> session.persist(deploymentLog))
                .toCompletableFuture();
            return CompletableFuture.allOf(updateStatus, createLog);
        });

        return sessionToFuture(handleError);
    }

    @Override
    public Future<Void> handleDeploymentSuccessful(JsonObject terraformOutput, DeployResourcesDTO request) {
        DeploymentOutput deploymentOutput = DeploymentOutput.fromJson(terraformOutput);
        List<CompletableFuture<Integer>> completables = new ArrayList<>();
        CompletionStage<Void> updateDeployment = withTransaction(session -> {
            completables.addAll(setTriggerUrlsByResourceTypeSet(session,
                deploymentOutput.getFunctionUrls().getValue().entrySet(), request));
            completables.addAll(setTriggerUrlForContainers(session, request));
            return CompletableFuture.allOf(completables.toArray(CompletableFuture[]::new))
                .thenCompose(res -> repositoryProvider.getResourceDeploymentRepository()
                    .updateDeploymentStatusByDeploymentId(session, request.getDeployment().getDeploymentId(),
                        DeploymentStatusValue.DEPLOYED))
                .thenAccept(res -> {});
        });

        return sessionToFuture(updateDeployment);
    }

    /**
     * Store all trigger urls of a deployment by resource type.
     *
     * @param resourceTypeSet all function resources of a certain resource type
     * @param request all data needed for the deployment process
     * @return a list of Completables
     */
    private List<CompletableFuture<Integer>> setTriggerUrlsByResourceTypeSet(Session session,
            Set<Map.Entry<String, String>> resourceTypeSet, DeployResourcesDTO request) {
        List<CompletableFuture<Integer>> completables = new ArrayList<>();
        for (Map.Entry<String, String> entry : resourceTypeSet) {
            String[] entryInfo = entry.getKey().split("_");
            long resourceId = Long.parseLong(entryInfo[0].substring(1));
            String functionName = entryInfo[1], runtimeName = entryInfo[2];
            findFunctionDeploymentAndUpdateTriggerUrl(session, request, resourceId, functionName, runtimeName,
                entry.getValue(), completables);
        }
        return completables;
    }

    /**
     * Find the persisted function deployment and update its trigger url.
     *
     * @param request all data needed for the deployment process
     * @param resourceId the id of the resource
     * @param functionName the name of the function
     * @param runtimeName the name of the runtime
     * @param triggerUrl the trigger url
     * @param completables the list where to store the new completables
     */
    private void findFunctionDeploymentAndUpdateTriggerUrl(Session session, DeployResourcesDTO request, long resourceId,
        String functionName, String runtimeName, String triggerUrl, List<CompletableFuture<Integer>> completables) {
        request.getFunctionDeployments().stream()
            .filter(functionDeployment -> matchesFunctionDeployment(resourceId, functionName, runtimeName,
                functionDeployment))
            .findFirst()
            .ifPresent(functionDeployment ->
                completables.add(repositoryProvider.getResourceDeploymentRepository().updateTriggerUrl(session,
                        functionDeployment.getResourceDeploymentId(), triggerUrl).toCompletableFuture())
            );
    }

    /**
     * Check if the given parameters match the values of the given function resource
     *
     * @param deploymentId the id of the deployment
     * @param functionName the name of the function
     * @param runtimeName the name of the runtime
     * @param functionDeployment the function deployment
     * @return true if they match, else false
     */
    private static boolean matchesFunctionDeployment(long deploymentId, String functionName, String runtimeName,
        FunctionDeployment functionDeployment) {
        return functionDeployment.getResource().getResourceId() == deploymentId &&
            functionDeployment.getFunction().getName().equals(functionName) &&
            functionDeployment.getFunction().getRuntime().getName().replace(".", "").equals(runtimeName);
    }

    private List<CompletableFuture<Integer>> setTriggerUrlForContainers(Session session, DeployResourcesDTO request) {
        List<CompletableFuture<Integer>> completables = new ArrayList<>();
        for (ServiceDeployment serviceDeployment : request.getServiceDeployments()) {
            String triggerUrl = String.format("/deployments/%s/%s/startup",
                request.getDeployment().getDeploymentId(),
                serviceDeployment.getResourceDeploymentId()) ;
            completables.add(repositoryProvider.getResourceDeploymentRepository().updateTriggerUrl(session,
                serviceDeployment.getResourceDeploymentId(), triggerUrl).toCompletableFuture());
        }
        return completables;
    }
}
