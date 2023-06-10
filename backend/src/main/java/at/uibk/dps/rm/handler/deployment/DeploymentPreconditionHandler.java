package at.uibk.dps.rm.handler.deployment;

import at.uibk.dps.rm.entity.dto.DeployResourcesRequest;
import at.uibk.dps.rm.entity.dto.deployment.FunctionResourceIds;
import at.uibk.dps.rm.entity.dto.deployment.ServiceResourceIds;
import at.uibk.dps.rm.entity.dto.resource.PlatformEnum;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.handler.account.CredentialsChecker;
import at.uibk.dps.rm.handler.function.FunctionChecker;
import at.uibk.dps.rm.handler.metric.PlatformMetricChecker;
import at.uibk.dps.rm.handler.resource.ResourceChecker;
import at.uibk.dps.rm.handler.resourceprovider.VPCChecker;
import at.uibk.dps.rm.handler.service.ServiceChecker;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Checks if the preconditions for a new deployment hold.
 *
 * @author matthi-g
 */
public class DeploymentPreconditionHandler {

    private final FunctionChecker functionChecker;

    private final ServiceChecker serviceChecker;

    private final ResourceChecker resourceChecker;

    private final PlatformMetricChecker platformMetricChecker;

    private final VPCChecker vpcChecker;

    private final CredentialsChecker credentialsChecker;

    /**
     * Create an instance from the functionChecker, serviceChecker, resourceChecker,
     * platformMetricChecker, vpcChecker and credentialsChecker.
     *
     * @param functionChecker the function checker
     * @param serviceChecker the service checker
     * @param resourceChecker the resource checker
     * @param platformMetricChecker the platform metric checker
     * @param vpcChecker the vpc checker
     * @param credentialsChecker the credentials checker
     */
    public DeploymentPreconditionHandler(FunctionChecker functionChecker, ServiceChecker serviceChecker,
            ResourceChecker resourceChecker, PlatformMetricChecker platformMetricChecker, VPCChecker vpcChecker,
            CredentialsChecker credentialsChecker) {
        this.functionChecker = functionChecker;
        this.serviceChecker = serviceChecker;
        this.resourceChecker = resourceChecker;
        this.platformMetricChecker = platformMetricChecker;
        this.vpcChecker = vpcChecker;
        this.credentialsChecker = credentialsChecker;
    }

    /**
     * Check if all preconditions for a valid deployment are fulfilled.
     *
     * @param requestDTO the data from the request
     * @param accountId the id of the creator
     * @param vpcList the list of vpcs
     * @return a Single that emits the list of functionResourceIds
     */
    public Single<JsonArray> checkDeploymentIsValid(DeployResourcesRequest requestDTO, long accountId,
                                                            List<VPC> vpcList) {
        return functionChecker.checkExistAllByIds(requestDTO.getFunctionResources())
            .andThen(serviceChecker.checkExistAllByIds(requestDTO.getServiceResources()))
            .andThen(resourceChecker.checkExistAllByIdsAndResourceType(requestDTO.getServiceResources(),
                requestDTO.getFunctionResources()))
            .andThen(Single.defer(() -> checkFindResources(requestDTO)))
            .flatMap(resources -> checkCloudCredentialsForResources(accountId, resources)
                .andThen(platformMetricChecker.checkMissingRequiredMetricsByResources(resources))
                .andThen(vpcChecker.checkVPCForFunctionResources(accountId, resources)
                    .map(vpcs -> {
                        vpcList.addAll(vpcs.stream().map(vpc -> vpc.mapTo(VPC.class)).collect(Collectors.toList()));
                        return vpcList;
                    }).ignoreElement()
                )
                .toSingle(() -> resources));
    }

    /**
     * Find all resources of the request
     *
     * @param request the deploy resources request
     * @return a Single that emits an array that contains all found resources
     */
    private Single<JsonArray> checkFindResources(DeployResourcesRequest request) {
        Single<List<Long>> functionResourceIds = Observable.fromIterable(request.getFunctionResources())
            .map(FunctionResourceIds::getResourceId)
            .toList();
        Single<List<Long>> serviceResourceIds = Observable.fromIterable(request.getServiceResources())
            .map(ServiceResourceIds::getResourceId)
            .toList();

        return functionResourceIds
            .flatMap(ids -> {
                Set<Long> resourceIds = new HashSet<>(ids);
                return serviceResourceIds.map(ids2 -> {
                    resourceIds.addAll(ids2);
                    return resourceIds;
                });
            })
            .map(ArrayList::new)
            .flatMap(resourceChecker::checkFindAllByResourceIds);
    }

    /**
     * Check if all necessary cloud credentials exist for the resources to be deployed.
     *
     * @param accountId the id of the account
     * @param resources the resources
     * @return a Completable
     */
    private Completable checkCloudCredentialsForResources(long accountId, JsonArray resources) {
        List<Completable> completables = new ArrayList<>();
        HashSet<Long> resourceProviderIds = new HashSet<>();
        for (Object object: resources.getList()) {
            Resource resource = ((JsonObject) object).mapTo(Resource.class);
            long providerId = resource.getRegion().getResourceProvider().getProviderId();
            String platform = resource.getPlatform().getPlatform();
            if (!resourceProviderIds.contains(providerId) && (platform.equals(PlatformEnum.LAMBDA.getValue()) ||
                    platform.equals(PlatformEnum.EC2.getValue())))  {
                completables.add(credentialsChecker.checkExistsOneByProviderId(accountId, providerId));
                resourceProviderIds.add(providerId);
            }
        }
        return Completable.merge(completables);
    }
}
