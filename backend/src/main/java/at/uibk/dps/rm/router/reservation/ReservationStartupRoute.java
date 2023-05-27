package at.uibk.dps.rm.router.reservation;

import at.uibk.dps.rm.handler.deployment.DeploymentChecker;
import at.uibk.dps.rm.handler.deployment.ContainerStartupHandler;
import at.uibk.dps.rm.handler.reservation.*;
import at.uibk.dps.rm.router.Route;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;

/**
 * Used to initialise the reservation startup route.
 *
 * @author matthi-g
 */
public class ReservationStartupRoute implements Route {
    @Override
    public void init(RouterBuilder router, ServiceProxyProvider serviceProxyProvider) {
        DeploymentChecker deploymentChecker = new DeploymentChecker(serviceProxyProvider.getDeploymentService(),
            serviceProxyProvider.getLogService(), serviceProxyProvider.getReservationLogService());
        ServiceReservationChecker serviceReservationChecker =
            new ServiceReservationChecker(serviceProxyProvider.getServiceReservationService());
        ContainerStartupHandler startupHandler = new ContainerStartupHandler(deploymentChecker,
            serviceReservationChecker);

        router
            .operation("deployResourceReservation")
            .handler(startupHandler::deployContainer);

        router
            .operation("terminateResourceReservation")
            .handler(startupHandler::terminateContainer);
    }
}
