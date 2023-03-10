package at.uibk.dps.rm.handler.log;

import at.uibk.dps.rm.handler.ValidationHandler;
import at.uibk.dps.rm.handler.reservation.ReservationChecker;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import at.uibk.dps.rm.util.HttpHelper;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.rxjava3.ext.web.RoutingContext;

public class ReservationLogHandler extends ValidationHandler {

    private final LogChecker logChecker;

    private final ReservationChecker reservationChecker;

    public ReservationLogHandler(ServiceProxyProvider serviceProxyProvider) {
        super(new ReservationLogChecker(serviceProxyProvider.getReservationLogService()));
        this.logChecker = new LogChecker(serviceProxyProvider.getLogService());
        this.reservationChecker = new ReservationChecker(serviceProxyProvider.getReservationService());
    }

    @Override
    protected Single<JsonArray> getAll(RoutingContext rc) {
        long accountId = rc.user().principal().getLong("account_id");
        return HttpHelper.getLongPathParam(rc, "id")
            .flatMap(id -> reservationChecker.checkFindOne(id, accountId).map(res -> id))
            .flatMap(id -> logChecker.checkFindAllByReservationId(id, accountId));
    }
}
