package at.uibk.dps.rm.handler.reservation;

import at.uibk.dps.rm.entity.dto.ReserveResourcesRequest;
import at.uibk.dps.rm.entity.dto.reservation.FunctionResourceIds;
import at.uibk.dps.rm.entity.model.FunctionResource;
import at.uibk.dps.rm.entity.model.Region;
import at.uibk.dps.rm.entity.model.VPC;
import at.uibk.dps.rm.handler.account.CredentialsChecker;
import at.uibk.dps.rm.handler.function.FunctionResourceChecker;
import at.uibk.dps.rm.handler.metric.ResourceTypeMetricChecker;
import at.uibk.dps.rm.handler.resourceprovider.VPCChecker;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Checks if the precondtions for a new reservation hold or not.
 *
 * @author matthi-g
 */
public class ReservationPreconditionHandler {

    private final FunctionResourceChecker functionResourceChecker;

    private final ResourceTypeMetricChecker resourceTypeMetricChecker;

    private final VPCChecker vpcChecker;

    private final CredentialsChecker credentialsChecker;

    /**
     * Create an instance from the functionResourceChecker, resourceTypeMetricChecker, vpcChecker
     * and credentialsChecker.
     *
     * @param functionResourceChecker the function resource checker
     * @param resourceTypeMetricChecker the resource type metric checker
     * @param vpcChecker the vpc checker
     * @param credentialsChecker the credentials checker
     */
    public ReservationPreconditionHandler(FunctionResourceChecker functionResourceChecker, ResourceTypeMetricChecker
        resourceTypeMetricChecker, VPCChecker vpcChecker, CredentialsChecker credentialsChecker) {
        this.functionResourceChecker = functionResourceChecker;
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
    public Single<List<JsonObject>> checkReservationIsValid(ReserveResourcesRequest requestDTO, long accountId,
                                                            List<VPC> vpcList) {
        return checkFindFunctionResources(requestDTO.getFunctionResources()).toList()
            .flatMap(functionResources -> checkCredentialsForResources(accountId, functionResources)
                .andThen(resourceTypeMetricChecker.checkMissingRequiredMetricsByFunctionResources(functionResources))
                .andThen(vpcChecker.checkVPCForFunctionResources(accountId, functionResources)
                    .map(vpcs -> {
                        vpcList.addAll(vpcs.stream().map(vpc -> vpc.mapTo(VPC.class)).collect(Collectors.toList()));
                        return vpcList;
                    }).ignoreElement()
                )
                .toSingle(() -> functionResources));
    }

    // TODO: move into functionResourceChecker
    /**
     * Fine all function resources by functionResourceIds
     *
     * @param functionResourceIds the function resource ids
     * @return an Observable that emits all found function resources as JsonObjects
     */
    private Observable<JsonObject> checkFindFunctionResources(List<FunctionResourceIds> functionResourceIds) {
        return Observable.fromIterable(functionResourceIds)
            .flatMapSingle(ids -> functionResourceChecker
                .checkFindOneByFunctionAndResource(ids.getFunctionId(), ids.getResourceId())
            );
    }

    /**
     * Check if all necessary credentials exist for the functionResources to be deployed.
     *
     * @param accountId the id of the account
     * @param functionResources the function resources
     * @return a Completable
     */
    private Completable checkCredentialsForResources(long accountId, List<JsonObject> functionResources) {
        List<Completable> completables = new ArrayList<>();
        HashSet<Long> resourceProviderIds = new HashSet<>();
        for (JsonObject jsonObject: functionResources) {
            Region region = jsonObject.mapTo(FunctionResource.class).getResource().getRegion();
            long providerId = region.getResourceProvider().getProviderId();
            if (!resourceProviderIds.contains(providerId) && !region.getName().equals("edge")) {
                completables.add(credentialsChecker.checkExistsOneByProviderId(accountId, providerId));
                resourceProviderIds.add(providerId);
            }
        }
        return Completable.merge(completables);
    }
}
