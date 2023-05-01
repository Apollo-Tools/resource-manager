package at.uibk.dps.rm.handler.reservation;

import at.uibk.dps.rm.entity.dto.ReserveResourcesRequest;
import at.uibk.dps.rm.entity.dto.reservation.FunctionResourceIds;
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
        checkForFunctionResourceDuplicates(requestDTO.getFunctionResources())
                .subscribe(rc::next, throwable -> rc.fail(400, throwable))
                .dispose();
    }

    /**
     * Check the functionResourceIds for duplicates.
     *
     * @param functionResourceIds the list of function resource ids
     * @return a Completable
     */
    private static Completable checkForFunctionResourceDuplicates(List<FunctionResourceIds> functionResourceIds) {
        return Maybe.just(functionResourceIds)
                .mapOptional(ids -> {
                    Set<FunctionResourceIds> functionResourceIdsSet = new HashSet<>(ids);
                    if (ids.size() != functionResourceIdsSet.size()) {
                        throw new Throwable("duplicated input");
                    }
                    return Optional.empty();
                })
                .ignoreElement();
    }
}
