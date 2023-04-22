package at.uibk.dps.rm.router.log;

import at.uibk.dps.rm.handler.ResultHandler;
import at.uibk.dps.rm.handler.log.LogChecker;
import at.uibk.dps.rm.handler.log.ReservationLogChecker;
import at.uibk.dps.rm.handler.log.ReservationLogHandler;
import at.uibk.dps.rm.handler.reservation.ReservationChecker;
import at.uibk.dps.rm.router.Route;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;

/**
 * Used to initialise the reservation log route.
 *
 * @author matthi-g
 */
public class ReservationLogRoute implements Route {

    @Override
    public void init(RouterBuilder router, ServiceProxyProvider serviceProxyProvider) {
        ReservationLogChecker reservationLogChecker =
            new ReservationLogChecker(serviceProxyProvider.getReservationLogService());
        LogChecker logChecker = new LogChecker(serviceProxyProvider.getLogService());
        ReservationChecker reservationChecker = new ReservationChecker(serviceProxyProvider
            .getReservationService());
        ReservationLogHandler reservationLogHandler = new ReservationLogHandler(reservationLogChecker, logChecker,
            reservationChecker);
        ResultHandler resultHandler = new ResultHandler(reservationLogHandler);

        router
            .operation("listReservationLogs")
            .handler(resultHandler::handleFindAllRequest);
    }
}
