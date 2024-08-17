package at.uibk.dps.rm.service.database.util;

import at.uibk.dps.rm.entity.dto.DeployResourcesRequest;
import at.uibk.dps.rm.entity.dto.credentials.DockerCredentials;
import at.uibk.dps.rm.entity.dto.deployment.DeployResourcesDTO;
import at.uibk.dps.rm.entity.dto.deployment.DeploymentValidation;
import at.uibk.dps.rm.entity.dto.deployment.FunctionResourceIds;
import at.uibk.dps.rm.entity.dto.deployment.ServiceResourceIds;
import at.uibk.dps.rm.entity.dto.resource.PlatformEnum;
import at.uibk.dps.rm.entity.dto.resource.ResourceTypeEnum;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.exception.BadInputException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.exception.UnauthorizedException;
import at.uibk.dps.rm.repository.DeploymentRepositoryProvider;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A utility class that provides various methods to validate a deployment.
 *
 * @author matthi-g
 */
@RequiredArgsConstructor
public class DeploymentValidationUtility {

    private final long accountId;

    private final DeploymentRepositoryProvider repositoryProvider;


    /**
     * Check if the resources from an deployment are also part of the selected ensemble.
     *
     * @param sm the database session manager
     * @param request the deploy request
     * @param deployment the deployment
     * @param accountId the creator id
     * @return a Single that emits a long value if it succeeds, else an {@link BadInputException} is
     * emitted
     */
    public Single<Long> checkEnsembleResourcesForAlerting(SessionManager sm, DeployResourcesRequest request,
                                                          Deployment deployment, long accountId) {
        DeploymentValidation validation = request.getValidation();
        deployment.setAlertNotificationUrl(request.getValidation().getAlertNotificationUrl());
        return repositoryProvider.getEnsembleRepository()
            .findByIdAndAccountId(sm, request.getValidation().getEnsembleId(), accountId)
            .switchIfEmpty(Single.error(new NotFoundException(Ensemble.class)))
            .flatMap(ensemble -> {
                deployment.setEnsemble(ensemble);
                return repositoryProvider.getResourceRepository()
                    .findAllByEnsembleId(sm, validation.getEnsembleId());
            })
            .flatMapObservable(Observable::fromIterable)
            .map(Resource::getResourceId)
            .collect(Collectors.toSet())
            .flatMap(ensembleResourceIds -> {
                Observable<Long> serviceResourceIds = Observable.fromIterable(request.getServiceResources())
                    .map(ServiceResourceIds::getResourceId);
                Observable<Long> functionResourceIds = Observable
                    .fromIterable(request.getFunctionResources())
                    .map(FunctionResourceIds::getResourceId);
                return Observable.merge(serviceResourceIds, functionResourceIds)
                    .filter(ensembleResourceIds::contains)
                    .isEmpty();
            })
            .flatMap(containsNonEnsembleResource -> {
                if (containsNonEnsembleResource) {
                    return Single.error(new BadInputException("Request contains non ensemble resource"));
                }
                return Single.just(1L);
            });
    }

    /**
     * Check if a new deployment is valid and get the details of all resources.
     * This includes checking for missing credentials, resource metrics for selected resources and
     * missing VPCs for EC2 deployments.
     *
     * @param sm the database session manager
     * @param requestDTO the deploy resources request
     * @param deployResources the deployment data that is used for the actual deployment of
     *                        resources
     * @return a Single that emits a list of all resource that have to be deployed
     */
    public Single<List<Resource>> checkDeploymentIsValid(SessionManager sm, DeployResourcesRequest requestDTO,
            DeployResourcesDTO deployResources) {
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

        return repositoryProvider.getFunctionRepository().findAllByIds(sm, functionIds)
            .flatMap(functions -> {
                if (functions.size() < functionIds.size()) {
                    return Single.error(new NotFoundException(Function.class));
                }
                return repositoryProvider.getServiceRepository().findAllByIds(sm, serviceIds);
            })
            .flatMap(services -> {
                if (services.size() < serviceIds.size()) {
                    return Single.error(new NotFoundException(Service.class));
                }
                return repositoryProvider.getResourceRepository()
                    .findAllByResourceIdsAndResourceTypes(sm, serviceResourceIds, serviceResourceTypes);
            })
            .flatMap(resources -> {
                if (resources.size() < serviceResourceIds.size()) {
                    return Single.error(new NotFoundException(Resource.class));
                }
                return repositoryProvider.getResourceRepository()
                    .findAllByResourceIdsAndResourceTypes(sm, functionResourceIds, functionResourceTypes);
            })
            .flatMap(resources -> {
                if (resources.size() < functionResourceIds.size()) {
                    return Single.error(new NotFoundException(Resource.class));
                }
                return repositoryProvider.getResourceRepository().findAllByResourceIdsAndFetch(sm,
                    allResourceIds);
            })
            .flatMap(resources -> {
                Completable checkResources = checkResourcesForDeployment(sm, resources, deployResources);
                Completable checkMetrics = checkMissingCustomMetrics(sm, resources);
                return Completable.mergeArray(checkResources, checkMetrics)
                    .andThen(Single.defer(() -> Single.just(resources)));
            });
    }

    /**
     * Check resources if they are suited for the deployment.
     *
     * @param sm the database session manager
     * @param resources the list of resources
     * @param deployResources the deployment data that is used for the actual deployment of
     *                        resources
     * @return a Completable that indicates an error if at least one resource is not suited for the
     *         deployment
     */
    private Completable checkResourcesForDeployment(SessionManager sm, List<Resource> resources,
            DeployResourcesDTO deployResources) {
        List<Completable> completables = new ArrayList<>();
        HashSet<Long> resourceProviderIds = new HashSet<>();
        HashSet<Long> regionIds = new HashSet<>();
        HashSet<Long> platformIds = new HashSet<>();
        for (Resource resource: resources) {
            MainResource mainResource = resource.getMain();
            long providerId = mainResource.getRegion().getResourceProvider().getProviderId();
            long regionId = mainResource.getRegion().getRegionId();
            PlatformEnum platform = PlatformEnum.fromPlatform(mainResource.getPlatform());
            checkDockerCredentials(deployResources.getDeploymentCredentials().getDockerCredentials(),
                mainResource.getPlatform().getPlatformId(), platform, platformIds);
            completables.add(checkCloudCredentials(sm, providerId, platform, resourceProviderIds)
                .andThen(checkResourceNotLocked(resource))
                .andThen(checkMissingVPC(sm, regionId, platform, regionIds, deployResources)));
        }
        return Completable.merge(completables);
    }

    /**
     * Check if necessary cloud credentials are present.
     *
     * @param sm the database session manager
     * @param providerId the id of the resource provider
     * @param platform the platform
     * @param resourceProviderIds the set of resource providers that have already been checked
     * @return a Completable that indicates an error if the credentials are missing
     */
    private Completable checkCloudCredentials(SessionManager sm, long providerId,
                                              PlatformEnum platform, Set<Long> resourceProviderIds) {
        if (!resourceProviderIds.contains(providerId) && (platform.equals(PlatformEnum.LAMBDA) ||
            platform.equals(PlatformEnum.EC2))) {
            resourceProviderIds.add(providerId);
            return repositoryProvider.getCredentialsRepository()
                .findByAccountIdAndProviderId(sm, accountId, providerId)
                .switchIfEmpty(Maybe.error(new UnauthorizedException("missing credentials for " + platform.getValue())))
                .ignoreElement();
        }
        return Completable.complete();
    }

    /**
     * Check if necessary docker credentials are present.
     *
     * @param dockerCredentials the docker credentials to check
     * @param platformId the id of the platform
     * @param platform the platform
     * @param platformIds the set of platforms that have already been checked
     * @throws UnauthorizedException if the credentials are necessary but not present
     */
    private void checkDockerCredentials(DockerCredentials dockerCredentials, long platformId, PlatformEnum platform,
            Set<Long> platformIds) {
        if (!platformIds.contains(platformId) && (platform.equals(PlatformEnum.EC2)) ||
            platform.equals(PlatformEnum.OPENFAAS)) {
            if (dockerCredentials == null) {
                throw new UnauthorizedException("missing docker credentials for " + platform.getValue());
            }
            platformIds.add(platformId);
        }
    }

    /**
     * Check if a necessary VPC is present or not.
     *
     * @param sm the database session manager
     * @param regionId the id of the region
     * @param platform the platform
     * @param regionIds the set of regions that have already been checked
     * @param deployResources the deployment data that is used for the actual deployment of
     *                        resources
     * @return a Completable that indicates an error if the VPC is missing
     */
    private Completable checkMissingVPC(SessionManager sm, long regionId, PlatformEnum platform,
                                        Set<Long> regionIds, DeployResourcesDTO deployResources) {
        if (!regionIds.contains(regionId) && platform.equals(PlatformEnum.EC2)) {
            regionIds.add(regionId);
            return repositoryProvider.getVpcRepository()
                .findByRegionIdAndAccountId(sm, regionId, accountId)
                .switchIfEmpty(Maybe.error(new NotFoundException(VPC.class)))
                .flatMapCompletable(vpc -> {
                    Region region = Hibernate.unproxy(vpc.getRegion(), Region.class);
                    vpc.setRegion(region);
                    deployResources.getVpcList().add(vpc);
                    return Completable.complete();
                });
        }
        return Completable.complete();
    }

    /**
     * Check if resources miss custom metrics
     *
     * @param sm the database session manager
     * @param resources the list of resources
     * @return a Completable that indicates an error if at least one resource has a missing
     *         required metric
     */
    private Completable checkMissingCustomMetrics(SessionManager sm, List<Resource> resources) {
        return Observable.fromIterable(resources)
            .flatMapCompletable(resource -> {
                boolean isMainResource = resource.getMain().equals(resource);
                return repositoryProvider.getPlatformMetricRepository()
                    .countMissingCustomMetricValuesByResourceId(sm, resource.getResourceId(),
                        isMainResource)
                    .flatMapCompletable(missingRequiredMetrics -> {
                        if (missingRequiredMetrics > 0) {
                            return Completable.error(new NotFoundException("missing required metrics for resource (" +
                                resource.getResourceId() + ")"));
                        }
                        return Completable.complete();
                    });
            });
    }

    /**
     * Check if a resource is locked.
     *
     * @param resource the resource
     * @return a Completable that emits nothing if it is not locked else an error
     */
    private Completable checkResourceNotLocked(Resource resource) {
        if (resource.getLockedByDeployment() != null) {
            return Completable.error(new BadInputException("resource " + resource + " is locked"));
        }
        return Completable.complete();
    }
}
