package at.uibk.dps.rm.handler.log;

import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.service.rxjava3.database.log.ReservationLogService;

public class ReservationLogChecker extends EntityChecker {
    public ReservationLogChecker(ReservationLogService reservationLogService) {
        super(reservationLogService);
    }
}
