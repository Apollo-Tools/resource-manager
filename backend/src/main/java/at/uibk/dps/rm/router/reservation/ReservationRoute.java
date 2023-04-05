package at.uibk.dps.rm.router.reservation;

import at.uibk.dps.rm.handler.RequestHandler;
import at.uibk.dps.rm.handler.account.CredentialsChecker;
import at.uibk.dps.rm.handler.deployment.DeploymentChecker;
import at.uibk.dps.rm.handler.deployment.DeploymentHandler;
import at.uibk.dps.rm.handler.function.FunctionResourceChecker;
import at.uibk.dps.rm.handler.reservation.ReservationHandler;
import at.uibk.dps.rm.handler.reservation.ReservationInputHandler;
import at.uibk.dps.rm.handler.reservation.ResourceReservationChecker;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;

public class ReservationRoute {

    public static void init(RouterBuilder router, ServiceProxyProvider serviceProxyProvider) {
        DeploymentChecker deploymentChecker = new DeploymentChecker(serviceProxyProvider.getDeploymentService(),
            serviceProxyProvider.getLogService(), serviceProxyProvider.getReservationLogService());
        CredentialsChecker credentialsChecker = new CredentialsChecker(serviceProxyProvider.getCredentialsService());
        FunctionResourceChecker functionResourceChecker =
            new FunctionResourceChecker(serviceProxyProvider.getFunctionResourceService());
        ResourceReservationChecker resourceReservationChecker = new ResourceReservationChecker(serviceProxyProvider
            .getResourceReservationService());
        DeploymentHandler deploymentHandler = new DeploymentHandler(deploymentChecker, credentialsChecker,
            functionResourceChecker, resourceReservationChecker);
        ReservationHandler reservationHandler = new ReservationHandler(serviceProxyProvider, deploymentHandler);
        RequestHandler reservationRequestHandler = new RequestHandler(reservationHandler);

        router
            .operation("getReservation")
            .handler(reservationRequestHandler::getRequest);

        router
            .operation("listMyReservations")
            .handler(reservationRequestHandler::getAllRequest);

        router
            .operation("reserveResources")
            .handler(ReservationInputHandler::validateResourceArrayHasNoDuplicates)
            .handler(reservationRequestHandler::postRequest);

        router
            .operation("cancelReservation")
            .handler(reservationRequestHandler::patchRequest);
    }
}
