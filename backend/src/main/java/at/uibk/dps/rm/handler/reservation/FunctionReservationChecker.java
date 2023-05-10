package at.uibk.dps.rm.handler.reservation;

import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.service.rxjava3.database.reservation.FunctionReservationService;

public class FunctionReservationChecker extends EntityChecker {
    public FunctionReservationChecker(FunctionReservationService service) {
        super(service);
    }
}
