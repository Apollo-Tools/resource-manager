package at.uibk.dps.rm.handler.reservation;

import at.uibk.dps.rm.entity.dto.ReserveResourcesRequest;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.vertx.rxjava3.ext.web.RoutingContext;
import lombok.experimental.UtilityClass;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Used to validate the inputs of the reservation endpoint and fails the
 * context if violations are found.
 *
 * @author matthi-g
 */
@UtilityClass
public class ReservationInputHandler {

    /**
     * Validate if a reserve resources request contains duplicated resources.
     *
     * @param rc the routing context
     */
    public static void validateResourceArrayHasNoDuplicates(RoutingContext rc) {
        ReserveResourcesRequest requestDTO = rc.body()
            .asJsonObject()
            .mapTo(ReserveResourcesRequest.class);
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
