package at.uibk.dps.rm.handler.reservation;

import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.handler.ErrorHandler;
import at.uibk.dps.rm.service.rxjava3.database.reservation.ReservationService;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;

public class ReservationChecker  extends EntityChecker {

    private final ReservationService reservationService;

    public ReservationChecker(ReservationService reservationService) {
        super(reservationService);
        this.reservationService = reservationService;

    }

    public Completable submitCancelReservation( JsonObject entity) {
        return reservationService.cancelReservationById(entity.getLong("reservation_id"));
    }

    public Single<JsonObject> checkFindOne(long id, long accountId) {
        Single<JsonObject> findOneById = reservationService.findOneByByIdAndAccountId(id, accountId);
        return ErrorHandler.handleFindOne(findOneById);
    }
}
