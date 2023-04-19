package at.uibk.dps.rm.router.reservation;

import at.uibk.dps.rm.handler.ResultHandler;
import at.uibk.dps.rm.handler.account.CredentialsChecker;
import at.uibk.dps.rm.handler.deployment.DeploymentChecker;
import at.uibk.dps.rm.handler.deployment.DeploymentHandler;
import at.uibk.dps.rm.handler.function.FunctionResourceChecker;
import at.uibk.dps.rm.handler.log.LogChecker;
import at.uibk.dps.rm.handler.log.ReservationLogChecker;
import at.uibk.dps.rm.handler.metric.ResourceTypeMetricChecker;
import at.uibk.dps.rm.handler.reservation.*;
import at.uibk.dps.rm.handler.resourceprovider.VPCChecker;
import at.uibk.dps.rm.handler.util.FileSystemChecker;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import at.uibk.dps.rm.handler.reservation.ReservationPreconditionHandler;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;

public class ReservationRoute {

    public static void init(final RouterBuilder router, final ServiceProxyProvider serviceProxyProvider) {
        /* Checker initialization */
        final DeploymentChecker deploymentChecker = new DeploymentChecker(serviceProxyProvider.getDeploymentService(),
            serviceProxyProvider.getLogService(), serviceProxyProvider.getReservationLogService());
        final CredentialsChecker credentialsChecker = new CredentialsChecker(serviceProxyProvider
            .getCredentialsService());
        final FunctionResourceChecker functionResourceChecker =
            new FunctionResourceChecker(serviceProxyProvider.getFunctionResourceService());
        final ResourceReservationChecker resourceReservationChecker =
            new ResourceReservationChecker(serviceProxyProvider.getResourceReservationService());
        final LogChecker logChecker = new LogChecker(serviceProxyProvider.getLogService());
        final ReservationLogChecker reservationLogChecker = new ReservationLogChecker(serviceProxyProvider
            .getReservationLogService());
        final FileSystemChecker fileSystemChecker = new FileSystemChecker(serviceProxyProvider.getFilePathService());
        final ReservationChecker reservationChecker = new ReservationChecker(serviceProxyProvider
            .getReservationService());
        final ResourceReservationStatusChecker statusChecker = new ResourceReservationStatusChecker(serviceProxyProvider
            .getResourceReservationStatusService());
        final ResourceTypeMetricChecker resourceTypeMetricChecker = new ResourceTypeMetricChecker(serviceProxyProvider
            .getResourceTypeMetricService());
        final VPCChecker vpcChecker = new VPCChecker(serviceProxyProvider.getVpcService());
        final ReservationPreconditionHandler preconditionChecker =
            new ReservationPreconditionHandler(functionResourceChecker, resourceTypeMetricChecker, vpcChecker,
                credentialsChecker);
        /* Handler initialization */
        final DeploymentHandler deploymentHandler = new DeploymentHandler(deploymentChecker, credentialsChecker,
            functionResourceChecker, resourceReservationChecker);
        final ReservationErrorHandler reservationErrorHandler = new ReservationErrorHandler(resourceReservationChecker,
            logChecker, reservationLogChecker, fileSystemChecker, deploymentHandler);
        final ReservationHandler reservationHandler = new ReservationHandler(reservationChecker,
            resourceReservationChecker, statusChecker, deploymentHandler, reservationErrorHandler, preconditionChecker);
        final ResultHandler resultHandler = new ResultHandler(reservationHandler);

        router
            .operation("getReservation")
            .handler(resultHandler::handleFindOneRequest);

        router
            .operation("listMyReservations")
            .handler(resultHandler::handleFindAllRequest);

        router
            .operation("reserveResources")
            .handler(ReservationInputHandler::validateResourceArrayHasNoDuplicates)
            .handler(resultHandler::handleSaveOneRequest);

        router
            .operation("cancelReservation")
            .handler(resultHandler::handleUpdateRequest);
    }
}
