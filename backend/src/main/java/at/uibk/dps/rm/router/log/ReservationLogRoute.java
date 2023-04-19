package at.uibk.dps.rm.router.log;

import at.uibk.dps.rm.handler.ResultHandler;
import at.uibk.dps.rm.handler.log.LogChecker;
import at.uibk.dps.rm.handler.log.ReservationLogChecker;
import at.uibk.dps.rm.handler.log.ReservationLogHandler;
import at.uibk.dps.rm.handler.reservation.ReservationChecker;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;

public class ReservationLogRoute {
    public static void init(final RouterBuilder router, final ServiceProxyProvider serviceProxyProvider) {
        final ReservationLogChecker reservationLogChecker =
            new ReservationLogChecker(serviceProxyProvider.getReservationLogService());
        final LogChecker logChecker = new LogChecker(serviceProxyProvider.getLogService());
        final ReservationChecker reservationChecker = new ReservationChecker(serviceProxyProvider
            .getReservationService());
        final ReservationLogHandler reservationLogHandler = new ReservationLogHandler(reservationLogChecker, logChecker,
            reservationChecker);
        final ResultHandler resultHandler = new ResultHandler(reservationLogHandler);

        router
            .operation("listReservationLogs")
            .handler(resultHandler::handleFindAllRequest);
    }
}
