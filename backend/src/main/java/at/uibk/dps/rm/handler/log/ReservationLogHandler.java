package at.uibk.dps.rm.handler.log;

import at.uibk.dps.rm.handler.ValidationHandler;
import at.uibk.dps.rm.handler.reservation.ReservationChecker;
import at.uibk.dps.rm.util.HttpHelper;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.rxjava3.ext.web.RoutingContext;

public class ReservationLogHandler extends ValidationHandler {

    private final LogChecker logChecker;

    private final ReservationChecker reservationChecker;

    public ReservationLogHandler(ReservationLogChecker reservationLogChecker, LogChecker logChecker,
                                 ReservationChecker reservationChecker) {
        super(reservationLogChecker);
        this.logChecker = logChecker;
        this.reservationChecker = reservationChecker;
    }

    @Override
    protected Single<JsonArray> getAll(RoutingContext rc) {
        long accountId = rc.user().principal().getLong("account_id");
        return HttpHelper.getLongPathParam(rc, "id")
            .flatMap(id -> reservationChecker.checkFindOne(id, accountId).map(res -> id))
            .flatMap(id -> logChecker.checkFindAllByReservationId(id, accountId));
    }
}
