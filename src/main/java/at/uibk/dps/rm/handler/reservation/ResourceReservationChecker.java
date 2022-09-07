package at.uibk.dps.rm.handler.reservation;

import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.service.rxjava3.database.reservation.ResourceReservationService;

public class ResourceReservationChecker extends EntityChecker {

    public ResourceReservationChecker(ResourceReservationService resourceReservationService) {
        super(resourceReservationService);
    }
}
