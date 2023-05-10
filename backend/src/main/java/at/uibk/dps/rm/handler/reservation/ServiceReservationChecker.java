package at.uibk.dps.rm.handler.reservation;

import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.handler.ErrorHandler;
import at.uibk.dps.rm.service.rxjava3.database.reservation.ServiceReservationService;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;

public class ServiceReservationChecker extends EntityChecker {

    private final ServiceReservationService service;

    public ServiceReservationChecker(ServiceReservationService service) {
        super(service);
        this.service = service;
    }

    /**
     * Find all service reservations by reservation.
     *
     * @return a Single that emits all found service reservations as JsonArray
     */
    public Single<JsonArray> checkFindAllByReservationId(long id) {
        final Single<JsonArray> findAllByResourceId = service.findAllByReservationId(id);
        return ErrorHandler.handleFindAll(findAllByResourceId);
    }
}
