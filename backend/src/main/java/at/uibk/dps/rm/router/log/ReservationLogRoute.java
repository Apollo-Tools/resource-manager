package at.uibk.dps.rm.router.log;

import at.uibk.dps.rm.handler.RequestHandler;
import at.uibk.dps.rm.handler.log.ReservationLogHandler;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;

public class ReservationLogRoute {
    public static void init(RouterBuilder router, ServiceProxyProvider serviceProxyProvider) {
        ReservationLogHandler reservationLogHandler = new ReservationLogHandler(serviceProxyProvider);
        RequestHandler reservationLogRequestHandler = new RequestHandler(reservationLogHandler);

        router
            .operation("listReservationLogs")
            .handler(reservationLogRequestHandler::getAllRequest);
    }
}
