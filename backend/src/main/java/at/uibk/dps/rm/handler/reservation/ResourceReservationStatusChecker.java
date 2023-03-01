package at.uibk.dps.rm.handler.reservation;

import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.handler.ErrorHandler;
import at.uibk.dps.rm.service.rxjava3.database.reservation.ResourceReservationStatusService;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;

public class ResourceReservationStatusChecker extends EntityChecker {
    private final ResourceReservationStatusService service;

    public ResourceReservationStatusChecker(ResourceReservationStatusService service) {
        super(service);
        this.service = service;
    }

    public Single<JsonObject> checkFindOneByStatusValue(String value) {
        Single<JsonObject> findOneByStatusValue = service.findOneByStatusValue(value);
        return ErrorHandler.handleFindOne(findOneByStatusValue);
    }
}
