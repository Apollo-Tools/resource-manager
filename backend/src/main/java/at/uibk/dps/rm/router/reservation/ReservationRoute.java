package at.uibk.dps.rm.router.reservation;

import at.uibk.dps.rm.handler.ResultHandler;
import at.uibk.dps.rm.handler.account.CredentialsChecker;
import at.uibk.dps.rm.handler.deployment.DeploymentChecker;
import at.uibk.dps.rm.handler.deployment.DeploymentHandler;
import at.uibk.dps.rm.handler.function.FunctionChecker;
import at.uibk.dps.rm.handler.function.FunctionResourceChecker;
import at.uibk.dps.rm.handler.log.LogChecker;
import at.uibk.dps.rm.handler.log.ReservationLogChecker;
import at.uibk.dps.rm.handler.metric.ResourceTypeMetricChecker;
import at.uibk.dps.rm.handler.reservation.*;
import at.uibk.dps.rm.handler.resource.ResourceChecker;
import at.uibk.dps.rm.handler.resourceprovider.VPCChecker;
import at.uibk.dps.rm.handler.service.ServiceChecker;
import at.uibk.dps.rm.handler.util.FileSystemChecker;
import at.uibk.dps.rm.router.Route;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import at.uibk.dps.rm.handler.reservation.ReservationPreconditionHandler;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;

/**
 * Used to initialise the reservation route.
 *
 * @author matthi-g
 */
public class ReservationRoute implements Route {

    @Override
    public void init(RouterBuilder router, ServiceProxyProvider serviceProxyProvider) {
        /* Checker initialization */
        DeploymentChecker deploymentChecker = new DeploymentChecker(serviceProxyProvider.getDeploymentService(),
            serviceProxyProvider.getLogService(), serviceProxyProvider.getReservationLogService());
        CredentialsChecker credentialsChecker = new CredentialsChecker(serviceProxyProvider
            .getCredentialsService());
        FunctionResourceChecker functionResourceChecker =
            new FunctionResourceChecker(serviceProxyProvider.getFunctionResourceService());
        FunctionChecker functionChecker = new FunctionChecker(serviceProxyProvider.getFunctionService());
        ServiceChecker serviceChecker = new ServiceChecker(serviceProxyProvider.getServiceService());
        ResourceChecker resourceChecker = new ResourceChecker(serviceProxyProvider.getResourceService());
        ResourceReservationChecker resourceReservationChecker =
            new ResourceReservationChecker(serviceProxyProvider.getResourceReservationService());
        LogChecker logChecker = new LogChecker(serviceProxyProvider.getLogService());
        ReservationLogChecker reservationLogChecker = new ReservationLogChecker(serviceProxyProvider
            .getReservationLogService());
        FileSystemChecker fileSystemChecker = new FileSystemChecker(serviceProxyProvider.getFilePathService());
        ReservationChecker reservationChecker = new ReservationChecker(serviceProxyProvider
            .getReservationService());
        ResourceReservationStatusChecker statusChecker = new ResourceReservationStatusChecker(serviceProxyProvider
            .getResourceReservationStatusService());
        ResourceTypeMetricChecker resourceTypeMetricChecker = new ResourceTypeMetricChecker(serviceProxyProvider
            .getResourceTypeMetricService());
        VPCChecker vpcChecker = new VPCChecker(serviceProxyProvider.getVpcService());
        ReservationPreconditionHandler preconditionChecker =
            new ReservationPreconditionHandler(functionResourceChecker, functionChecker, serviceChecker,
                resourceChecker, resourceTypeMetricChecker, vpcChecker, credentialsChecker);
        /* Handler initialization */
        DeploymentHandler deploymentHandler = new DeploymentHandler(deploymentChecker, credentialsChecker,
            functionResourceChecker, resourceReservationChecker);
        ReservationErrorHandler reservationErrorHandler = new ReservationErrorHandler(resourceReservationChecker,
            logChecker, reservationLogChecker, fileSystemChecker, deploymentHandler);
        ReservationHandler reservationHandler = new ReservationHandler(reservationChecker,
            resourceReservationChecker, statusChecker, deploymentHandler, reservationErrorHandler, preconditionChecker);
        ResultHandler resultHandler = new ResultHandler(reservationHandler);

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
