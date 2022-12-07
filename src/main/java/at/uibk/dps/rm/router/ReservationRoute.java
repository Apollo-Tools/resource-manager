package at.uibk.dps.rm.router;

import at.uibk.dps.rm.handler.RequestHandler;
import at.uibk.dps.rm.handler.reservation.ReservationHandler;
import at.uibk.dps.rm.handler.reservation.ReservationInputHandler;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;

public class ReservationRoute {

    public static void init(RouterBuilder router, ServiceProxyProvider serviceProxyProvider) {
        ReservationHandler reservationHandler = new ReservationHandler(serviceProxyProvider);
        RequestHandler reservationRequestHandler = new RequestHandler(reservationHandler);

        router
            .operation("getReservation")
            .handler(reservationRequestHandler::getRequest);

        router
            .operation("reserveResources")
            .handler(ReservationInputHandler::validateResourceArrayHasNoDuplicates)
            .handler(reservationRequestHandler::postRequest);

        router
            .operation("cancelReservation")
            .handler(reservationRequestHandler::patchRequest);
    }
}
