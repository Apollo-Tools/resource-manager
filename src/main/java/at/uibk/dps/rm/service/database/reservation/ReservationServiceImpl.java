package at.uibk.dps.rm.service.database.reservation;

import at.uibk.dps.rm.entity.model.Reservation;
import at.uibk.dps.rm.repository.ReservationRepository;
import at.uibk.dps.rm.service.database.ServiceProxy;

public class ReservationServiceImpl extends ServiceProxy<Reservation> implements ReservationService {
    public ReservationServiceImpl(ReservationRepository reservationRepository) {
        super(reservationRepository, Reservation.class);
    }
}
