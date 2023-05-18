package at.uibk.dps.rm.handler.reservation;

import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.handler.ErrorHandler;
import at.uibk.dps.rm.service.rxjava3.database.reservation.FunctionReservationService;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;

/**
 * Implements methods to perform CRUD operations on the function_reservation entity.
 *
 * @see EntityChecker
 *
 * @author matthi-g
 */
public class FunctionReservationChecker extends EntityChecker {

    private final FunctionReservationService service;

    /**
     * Create an instance from the service.
     *
     * @param service the function reservation service
     */
    public FunctionReservationChecker(FunctionReservationService service) {
        super(service);
        this.service = service;
    }

    /**
     * Find all function reservations by reservation.
     *
     * @return a Single that emits all found resource reservations as JsonArray
     */
    public Single<JsonArray> checkFindAllByReservationId(long id) {
        final Single<JsonArray> findAllByResourceId = service.findAllByReservationId(id);
        return ErrorHandler.handleFindAll(findAllByResourceId);
    }
}
