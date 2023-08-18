package at.uibk.dps.rm.handler.deployment;

import at.uibk.dps.rm.entity.dto.DeployResourcesRequest;
import at.uibk.dps.rm.entity.dto.deployment.FunctionResourceIds;
import at.uibk.dps.rm.entity.dto.deployment.ServiceResourceIds;
import at.uibk.dps.rm.exception.BadInputException;
import at.uibk.dps.rm.handler.ResultHandler;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.ext.web.RoutingContext;
import lombok.experimental.UtilityClass;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Used to validate the inputs of the deployment endpoint and fails the
 * context if violations are found.
 *
 * @author matthi-g
 */
@UtilityClass
public class DeploymentInputHandler {

    /**
     * Validate if a deploy resources request contains duplicated resources.
     *
     * @param rc the routing context
     */
    public static void validateResourceArrayHasNoDuplicates(RoutingContext rc) {
        DeployResourcesRequest requestDTO = rc.body()
            .asJsonObject()
            .mapTo(DeployResourcesRequest.class);
        checkForListDuplicates(requestDTO.getFunctionResources())
            .andThen(checkForListDuplicates(requestDTO.getServiceResources()))
            .andThen(checkForListDuplicates(requestDTO.getLockResources()))
            .andThen(Observable.fromIterable(requestDTO.getLockResources())
                .flatMapCompletable(resourceId -> checkLockResourceIsInDeployment(resourceId.getResourceId(),
                    requestDTO.getFunctionResources(), requestDTO.getServiceResources()))
            )
            .subscribe(rc::next, throwable -> ResultHandler.handleRequestError(rc, throwable))
            .dispose();
    }

    /**
     * Check the list of entries for duplicates.
     *
     * @param entries the list
     * @return a Completable
     */
    private static Completable checkForListDuplicates(List<?> entries) {
        if (entries == null || entries.isEmpty()) {
            return Completable.complete();
        }

        return Single.just(entries)
            .flatMapCompletable(ids -> {
                Set<?> resourceIdSet = new HashSet<>(ids);
                if (ids.size() != resourceIdSet.size()) {
                    return Completable.error(new BadInputException("duplicated input"));
                }
                return Completable.complete();
            });
    }

    private static Completable checkLockResourceIsInDeployment(long resourceId,
        List<FunctionResourceIds> functionResourceIds, List<ServiceResourceIds> serviceResourceIds) {
        return Observable.fromIterable(functionResourceIds)
            .filter(functionResource -> functionResource.getResourceId() == resourceId)
            .isEmpty()
            .flatMapCompletable(isEmpty1 -> {
                    if (!isEmpty1) {
                        return Completable.complete();
                    }
                    return Observable.fromIterable(serviceResourceIds)
                        .filter(serviceResource -> serviceResource.getResourceId() == resourceId)
                        .isEmpty()
                        .flatMapCompletable(isEmpty2 -> {
                            if (!isEmpty2) {
                                return Completable.complete();
                            }
                            return Completable.error(new BadInputException("invalid lock resource (" + resourceId +
                                ")"));
                        });
                }
            );
    }
}
