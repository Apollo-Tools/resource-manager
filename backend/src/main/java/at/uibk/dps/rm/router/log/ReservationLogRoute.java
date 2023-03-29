package at.uibk.dps.rm.router.log;

import at.uibk.dps.rm.handler.RequestHandler;
import at.uibk.dps.rm.handler.log.LogChecker;
import at.uibk.dps.rm.handler.log.ReservationLogChecker;
import at.uibk.dps.rm.handler.log.ReservationLogHandler;
import at.uibk.dps.rm.handler.reservation.ReservationChecker;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;

public class ReservationLogRoute {
    public static void init(RouterBuilder router, ServiceProxyProvider serviceProxyProvider) {
        ReservationLogChecker reservationLogChecker =
            new ReservationLogChecker(serviceProxyProvider.getReservationLogService());
        LogChecker logChecker = new LogChecker(serviceProxyProvider.getLogService());
        ReservationChecker reservationChecker = new ReservationChecker(serviceProxyProvider.getReservationService());
        ReservationLogHandler reservationLogHandler = new ReservationLogHandler(reservationLogChecker, logChecker,
            reservationChecker);
        RequestHandler reservationLogRequestHandler = new RequestHandler(reservationLogHandler);

        router
            .operation("listReservationLogs")
            .handler(reservationLogRequestHandler::getAllRequest);
    }
}
