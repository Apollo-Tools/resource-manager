package at.uibk.dps.rm.handler.reservation;

import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.service.rxjava3.database.reservation.ServiceReservationService;

public class ServiceReservationChecker extends EntityChecker {
    public ServiceReservationChecker(ServiceReservationService service) {
        super(service);
    }
}
