package at.uibk.dps.rm.handler.reservation;

import at.uibk.dps.rm.entity.dto.ReserveResourcesRequest;
import at.uibk.dps.rm.entity.dto.reservation.FunctionResourceIds;
import at.uibk.dps.rm.entity.dto.reservation.ServiceResourceIds;
import at.uibk.dps.rm.entity.dto.resource.ResourceTypeEnum;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.handler.account.CredentialsChecker;
import at.uibk.dps.rm.handler.function.FunctionChecker;
import at.uibk.dps.rm.handler.metric.ResourceTypeMetricChecker;
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
 * Checks if the precondtions for a new reservation hold or not.
 *
 * @author matthi-g
 */
public class ReservationPreconditionHandler {

    private final FunctionChecker functionChecker;

    private final ServiceChecker serviceChecker;

    private final ResourceChecker resourceChecker;

    private final ResourceTypeMetricChecker resourceTypeMetricChecker;

    private final VPCChecker vpcChecker;

    private final CredentialsChecker credentialsChecker;

    /**
     * Create an instance from the functionChecker, serviceChecker, resourceChecker,
     * resourceTypeMetricChecker, vpcChecker and credentialsChecker.
     *
     * @param functionChecker the function checker
     * @param serviceChecker the service checker
     * @param resourceChecker the resource checker
     * @param resourceTypeMetricChecker the resource type metric checker
     * @param vpcChecker the vpc checker
     * @param credentialsChecker the credentials checker
     */
    public ReservationPreconditionHandler(FunctionChecker functionChecker,
        ServiceChecker serviceChecker, ResourceChecker resourceChecker,
        ResourceTypeMetricChecker resourceTypeMetricChecker, VPCChecker vpcChecker,
        CredentialsChecker credentialsChecker) {
        this.functionChecker = functionChecker;
        this.serviceChecker = serviceChecker;
        this.resourceChecker = resourceChecker;
        this.resourceTypeMetricChecker = resourceTypeMetricChecker;
        this.vpcChecker = vpcChecker;
        this.credentialsChecker = credentialsChecker;
    }

    /**
     * Check if all preconditions for a valid reservation are fulfilled.
     *
     * @param requestDTO the data from the request
     * @param accountId the id of the creator
     * @param vpcList the list of vpcs
     * @return a Single that emits the list of functionResourceIds
     */
    public Single<JsonArray> checkReservationIsValid(ReserveResourcesRequest requestDTO, long accountId,
                                                            List<VPC> vpcList) {
        return functionChecker.checkExistAllByIds(requestDTO.getFunctionResources())
            .andThen(serviceChecker.checkExistAllByIds(requestDTO.getServiceResources()))
            .andThen(resourceChecker.checkExistAllByIdsAndResourceType(requestDTO.getServiceResources(),
                requestDTO.getFunctionResources()))
            .andThen(Single.defer(() -> checkFindResources(requestDTO)))
            .flatMap(resources -> checkCloudCredentialsForResources(accountId, resources)
                .andThen(resourceTypeMetricChecker.checkMissingRequiredMetricsByResources(resources))
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
     * @param request the reserve resources request
     * @return a Single that emits an array that contains all found resources
     */
    private Single<JsonArray> checkFindResources(ReserveResourcesRequest request) {
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
            String resourceType = resource.getResourceType().getResourceType();
            if (!resourceProviderIds.contains(providerId) && !resourceType.equals(ResourceTypeEnum.EDGE.getValue()) &&
                    !resourceType.equals(ResourceTypeEnum.CONTAINER.getValue())) {
                completables.add(credentialsChecker.checkExistsOneByProviderId(accountId, providerId));
                resourceProviderIds.add(providerId);
            }
        }
        return Completable.merge(completables);
    }
}
