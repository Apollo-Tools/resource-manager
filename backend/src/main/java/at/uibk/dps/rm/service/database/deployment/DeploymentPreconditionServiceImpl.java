package at.uibk.dps.rm.service.database.deployment;

import at.uibk.dps.rm.entity.dto.DeployResourcesRequest;
import at.uibk.dps.rm.entity.dto.deployment.FunctionResourceIds;
import at.uibk.dps.rm.entity.dto.deployment.ServiceResourceIds;
import at.uibk.dps.rm.entity.dto.resource.PlatformEnum;
import at.uibk.dps.rm.entity.dto.resource.ResourceTypeEnum;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.exception.NotFoundException;
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

    /**
     * Create an instance from the deploymentRepository.
     *
     * @param repository the deployment repository
     */
    public DeploymentPreconditionServiceImpl(DeploymentRepository repository, FunctionRepository functionRepository,
            ServiceRepository serviceRepository, ResourceRepository resourceRepository,
            PlatformMetricRepository platformMetricRepository, VPCRepository vpcRepository, SessionFactory sessionFactory) {
        super(repository, Deployment.class, sessionFactory);
        this.functionRepository = functionRepository;
        this.serviceRepository = serviceRepository;
        this.resourceRepository = resourceRepository;
        this.platformMetricRepository = platformMetricRepository;
        this.vpcRepository = vpcRepository;
    }

    @Override
    public String getServiceProxyAddress() {
        return  ServiceProxyAddress.getServiceProxyAddress("deployment-precondition");
    }

    @Override
    public Future<JsonArray> checkDeploymentIsValid(long accountId, DeployResourcesRequest requestDTO) {
        List<String> functionResourceTypes = List.of(ResourceTypeEnum.FAAS.getValue());
        List<String> serviceResourceTypes = List.of(ResourceTypeEnum.CONTAINER.getValue());
        Set<Long> serviceResourceIds = requestDTO.getServiceResources().stream()
            .map(ServiceResourceIds::getResourceId)
            .collect(Collectors.toSet());
        Set<Long> functionResourceIds = requestDTO.getFunctionResources().stream()
            .map(FunctionResourceIds::getResourceId)
            .collect(Collectors.toSet());
        List<Long> allResourceIds = Stream.of(functionResourceIds, serviceResourceIds)
            .flatMap(Set::stream)
            .collect(Collectors.toList());
        CompletionStage<List<Resource>> checkDeployment = withSession(session -> {
            Set<Long> functionIds = requestDTO.getFunctionResources().stream()
                .map(FunctionResourceIds::getFunctionId)
                .collect(Collectors.toSet());
            return functionRepository.findAllByIds(session, functionIds)
                .thenCompose(functions -> {
                    if (functions.size() < functionIds.size()) {
                        throw new NotFoundException(Function.class);
                    }
                    Set<Long> serviceIds = requestDTO.getServiceResources().stream()
                        .map(ServiceResourceIds::getServiceId)
                        .collect(Collectors.toSet());
                    return serviceRepository.findAllByIds(session, serviceIds)
                        .thenAccept(services -> {
                            if (services.size() < serviceIds.size()) {
                                throw new NotFoundException(Service.class);
                            }
                        });
                    })
                .thenCompose(result -> resourceRepository
                    .findAllByResourceIdsAndResourceTypes(session, serviceResourceIds, serviceResourceTypes)
                    .thenAccept(resources -> {
                        if (resources.size() < serviceResourceIds.size()) {
                            throw new NotFoundException(Resource.class);
                        }
                    }))
                .thenCompose(result -> resourceRepository
                    .findAllByResourceIdsAndResourceTypes(session, functionResourceIds, functionResourceTypes)
                    .thenAccept(resources -> {
                        if (resources.size() < functionResourceIds.size()) {
                            throw new NotFoundException(Resource.class);
                        }
                    }))
                .thenCompose(result -> resourceRepository.findAllByResourceIdsAndFetch(session, allResourceIds))
                .thenCompose(resources -> {
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
                    return CompletableFuture.allOf(checkMissingRequiredMetrics.toArray(new CompletableFuture[0]))
                        .thenCompose(result -> {
                            List<CompletionStage<VPC>> findVPCs = new ArrayList<>();
                            HashSet<Long> regionIds = new HashSet<>();
                            for (Resource resource: resources) {
                                long regionId = resource.getRegion().getRegionId();
                                String platform = resource.getPlatform().getPlatform();
                                if (!regionIds.contains(regionId) && platform.equals(PlatformEnum.EC2.getValue())) {
                                    findVPCs.add(vpcRepository.findByRegionIdAndAccountId(session, regionId, accountId)
                                        .thenApply(vpc -> {
                                            ServiceResultValidator.checkFound(vpc, VPC.class);
                                            return vpc;
                                        }));
                                    regionIds.add(regionId);
                                }
                            }
                            return CompletableFuture.allOf(findVPCs.toArray(new CompletableFuture[0]))
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
                });
        });

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
}
