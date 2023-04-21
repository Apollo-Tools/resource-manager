package at.uibk.dps.rm.handler.reservation;

import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.handler.ErrorHandler;
import at.uibk.dps.rm.service.rxjava3.database.reservation.ResourceReservationStatusService;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;

/**
 * Implements methods to perform CRUD operations on the resource_reservation_status entity.
 *
 * @see EntityChecker
 *
 * @author matthi-g
 */
public class ResourceReservationStatusChecker extends EntityChecker {
    private final ResourceReservationStatusService service;

    /**
     * Create an instance from the service
     *
     * @param service the resource reservation status service
     */
    public ResourceReservationStatusChecker(ResourceReservationStatusService service) {
        super(service);
        this.service = service;
    }

    /**
     * Find a resource reservation status by its value.
     *
     * @param value the string value of the status
     * @return a Single that emits the found resource reservation status as JsonObject
     */
    public Single<JsonObject> checkFindOneByStatusValue(String value) {
        Single<JsonObject> findOneByStatusValue = service.findOneByStatusValue(value);
        return ErrorHandler.handleFindOne(findOneByStatusValue);
    }
}
