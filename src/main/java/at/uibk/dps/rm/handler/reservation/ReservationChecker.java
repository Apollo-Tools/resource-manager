package at.uibk.dps.rm.handler.reservation;

import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.service.rxjava3.database.reservation.ReservationService;

public class ReservationChecker  extends EntityChecker {
    public ReservationChecker(ReservationService reservationService) {
        super(reservationService);
    }
}
