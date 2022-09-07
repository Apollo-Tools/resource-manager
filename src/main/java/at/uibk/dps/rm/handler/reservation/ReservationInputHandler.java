package at.uibk.dps.rm.handler.reservation;

import at.uibk.dps.rm.entity.dto.ReserveResourcesRequest;
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
        checkForResourceDuplicates(requestDTO.getResources())
                .subscribe(rc::next, throwable -> rc.fail(400, throwable))
                .dispose();
    }

    private static Completable checkForResourceDuplicates(List<Long> resources) {
        return Maybe.just(resources)
                .mapOptional(ids -> {
                    Set<Long> resourceIds = new HashSet<>(ids);
                    if (ids.size() != resourceIds.size()) {
                        throw new Throwable("duplicated input");
                    }
                    return Optional.empty();
                })
                .ignoreElement();
    }
}
