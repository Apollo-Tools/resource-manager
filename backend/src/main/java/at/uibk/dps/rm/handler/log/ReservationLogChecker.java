package at.uibk.dps.rm.handler.log;

import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.service.rxjava3.database.log.ReservationLogService;

/**
 * Implements methods to perform CRUD operations on the reservation_log entity.
 *
 * @see EntityChecker
 *
 * @author matthi-g
 */
public class ReservationLogChecker extends EntityChecker {

    /**
     * Create an instance from the reservationLogService
     *
     * @param reservationLogService the reservation log service
     */
    public ReservationLogChecker(ReservationLogService reservationLogService) {
        super(reservationLogService);
    }
}
