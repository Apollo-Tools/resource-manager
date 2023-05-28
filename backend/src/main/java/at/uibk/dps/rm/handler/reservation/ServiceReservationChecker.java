package at.uibk.dps.rm.handler.reservation;

import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.handler.ErrorHandler;
import at.uibk.dps.rm.service.rxjava3.database.reservation.ServiceReservationService;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;

/**
 * Implements methods to perform CRUD operations on the service_reservation entity.
 *
 * @see EntityChecker
 *
 * @author matthi-g
 */
public class ServiceReservationChecker extends EntityChecker {

    private final ServiceReservationService service;

    /**
     * Create an instance from the service.
     *
     * @param service the service resrvation service
     */
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

    /**
     * Check whether a service reservation is ready for startup
     *
     * @return a Single that emits true if the service reservation is ready, else false
     */
    public Completable checkReadyForStartup(long reservationId,
        long resourceReservationId, long accountId) {
        Single<Boolean> exists = service.existsReadyForContainerStartupAndTermination(reservationId,
            resourceReservationId,
          accountId);
        return ErrorHandler.handleExistsOne(exists).ignoreElement();
    }
}
