package at.uibk.dps.rm.service.database.reservation;

import at.uibk.dps.rm.entity.model.Reservation;
import at.uibk.dps.rm.repository.ReservationRepository;
import at.uibk.dps.rm.service.database.ServiceProxy;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

public class ReservationServiceImpl extends ServiceProxy<Reservation> implements ReservationService {

    private final ReservationRepository reservationRepository;

    public ReservationServiceImpl(ReservationRepository reservationRepository) {
        super(reservationRepository, Reservation.class);
        this.reservationRepository = reservationRepository;
    }

    @Override
    public Future<Void> cancelReservationById(long id) {
        return Future
                .fromCompletionStage(reservationRepository.cancelReservation(id))
                .mapEmpty();
    }

    @Override
    public Future<JsonObject> findOneByByIdAndAccountId(long id, long accountId) {
        return Future
            .fromCompletionStage(reservationRepository.findByIdAndAccountId(id, accountId))
            .map(JsonObject::mapFrom);
    }
}
