package at.uibk.dps.rm.handler.reservation;

import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.handler.ErrorHandler;
import at.uibk.dps.rm.service.rxjava3.database.reservation.ResourceReservationService;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;

public class ResourceReservationChecker extends EntityChecker {

    private final ResourceReservationService resourceReservationService;

    public ResourceReservationChecker(ResourceReservationService resourceReservationService) {
        super(resourceReservationService);
        this.resourceReservationService = resourceReservationService;
    }

    public Single<JsonArray> checkFindAllByReservationId(long id) {
        Single<JsonArray> findAllByResourceId = resourceReservationService.findAllByReservationId(id);
        return ErrorHandler.handleFindAll(findAllByResourceId);
    }
}
