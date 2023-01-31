package at.uibk.dps.rm.handler.reservation;

import at.uibk.dps.rm.entity.dto.ReserveResourcesRequest;
import at.uibk.dps.rm.entity.dto.reservation.FunctionResourceIds;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.vertx.rxjava3.ext.web.RoutingContext;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class ReservationInputHandler {

    public static void validateResourceArrayHasNoDuplicates(RoutingContext rc) {
        ReserveResourcesRequest requestDTO = rc.body()
                .asJsonObject()
                .mapTo(ReserveResourcesRequest.class);
        checkForFunctionResourceDuplicates(requestDTO.getFunctionResources())
                .subscribe(rc::next, throwable -> rc.fail(400, throwable))
                .dispose();
    }

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
