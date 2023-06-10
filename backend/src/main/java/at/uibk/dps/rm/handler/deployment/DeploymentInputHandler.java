package at.uibk.dps.rm.handler.deployment;

import at.uibk.dps.rm.entity.dto.DeployResourcesRequest;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.vertx.rxjava3.ext.web.RoutingContext;
import lombok.experimental.UtilityClass;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
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
        checkForResourceIdDuplicates(requestDTO.getFunctionResources())
            .andThen(checkForResourceIdDuplicates(requestDTO.getServiceResources()))
            .subscribe(rc::next, throwable -> rc.fail(400, throwable))
            .dispose();
    }

    /**
     * Check the resourceIds for duplicates.
     *
     * @param resourceIds the list of resource ids
     * @return a Completable
     */
    private static Completable checkForResourceIdDuplicates(List<?> resourceIds) {
        return Maybe.just(resourceIds)
                .mapOptional(ids -> {
                    Set<?> resourceIdSet = new HashSet<>(ids);
                    if (ids.size() != resourceIdSet.size()) {
                        throw new Throwable("duplicated input");
                    }
                    return Optional.empty();
                })
                .ignoreElement();
    }
}
