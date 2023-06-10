package at.uibk.dps.rm.router.deployment;

import at.uibk.dps.rm.handler.ResultHandler;
import at.uibk.dps.rm.handler.account.CredentialsChecker;
import at.uibk.dps.rm.handler.deploymentexecution.DeploymentExecutionChecker;
import at.uibk.dps.rm.handler.deploymentexecution.DeploymentExecutionHandler;
import at.uibk.dps.rm.handler.function.FunctionChecker;
import at.uibk.dps.rm.handler.log.LogChecker;
import at.uibk.dps.rm.handler.log.DeploymentLogChecker;
import at.uibk.dps.rm.handler.metric.PlatformMetricChecker;
import at.uibk.dps.rm.handler.deployment.*;
import at.uibk.dps.rm.handler.resource.ResourceChecker;
import at.uibk.dps.rm.handler.resourceprovider.VPCChecker;
import at.uibk.dps.rm.handler.service.ServiceChecker;
import at.uibk.dps.rm.handler.util.FileSystemChecker;
import at.uibk.dps.rm.router.Route;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import at.uibk.dps.rm.handler.deployment.DeploymentPreconditionHandler;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;

/**
 * Used to initialise the deployment route.
 *
 * @author matthi-g
 */
public class DeploymentRoute implements Route {

    @Override
    public void init(RouterBuilder router, ServiceProxyProvider serviceProxyProvider) {
        /* Checker initialization */
        DeploymentExecutionChecker deploymentExecutionChecker = new DeploymentExecutionChecker(serviceProxyProvider.getDeploymentService(),
            serviceProxyProvider.getLogService(), serviceProxyProvider.getReservationLogService());
        CredentialsChecker credentialsChecker = new CredentialsChecker(serviceProxyProvider
            .getCredentialsService());
        FunctionChecker functionChecker = new FunctionChecker(serviceProxyProvider.getFunctionService());
        ServiceChecker serviceChecker = new ServiceChecker(serviceProxyProvider.getServiceService());
        ResourceChecker resourceChecker = new ResourceChecker(serviceProxyProvider.getResourceService());
        ResourceDeploymentChecker resourceDeploymentChecker =
            new ResourceDeploymentChecker(serviceProxyProvider.getResourceReservationService());
        FunctionDeploymentChecker functionDeploymentChecker = new FunctionDeploymentChecker(serviceProxyProvider
            .getFunctionReservationService());
        ServiceDeploymentChecker serviceDeploymentChecker = new ServiceDeploymentChecker(serviceProxyProvider
            .getServiceReservationService());
        LogChecker logChecker = new LogChecker(serviceProxyProvider.getLogService());
        DeploymentLogChecker deploymentLogChecker = new DeploymentLogChecker(serviceProxyProvider
            .getReservationLogService());
        FileSystemChecker fileSystemChecker = new FileSystemChecker(serviceProxyProvider.getFilePathService());
        DeploymentChecker deploymentChecker = new DeploymentChecker(serviceProxyProvider
            .getReservationService());
        ResourceDeploymentStatusChecker statusChecker = new ResourceDeploymentStatusChecker(serviceProxyProvider
            .getResourceReservationStatusService());
        PlatformMetricChecker platformMetricChecker = new PlatformMetricChecker(serviceProxyProvider
            .getPlatformMetricService());
        VPCChecker vpcChecker = new VPCChecker(serviceProxyProvider.getVpcService());
        DeploymentPreconditionHandler preconditionChecker =
            new DeploymentPreconditionHandler(functionChecker, serviceChecker,
                resourceChecker, platformMetricChecker, vpcChecker, credentialsChecker);
        /* Handler initialization */
        DeploymentExecutionHandler deploymentExecutionHandler = new DeploymentExecutionHandler(deploymentExecutionChecker, credentialsChecker,
            functionDeploymentChecker, serviceDeploymentChecker, resourceDeploymentChecker);
        DeploymentErrorHandler deploymentErrorHandler = new DeploymentErrorHandler(resourceDeploymentChecker,
            logChecker, deploymentLogChecker, fileSystemChecker, deploymentExecutionHandler);
        DeploymentHandler deploymentHandler = new DeploymentHandler(deploymentChecker,
            resourceDeploymentChecker, functionDeploymentChecker, serviceDeploymentChecker, statusChecker,
            deploymentExecutionHandler, deploymentErrorHandler, preconditionChecker);
        ResultHandler resultHandler = new ResultHandler(deploymentHandler);

        router
            .operation("getDeployment")
            .handler(resultHandler::handleFindOneRequest);

        router
            .operation("listMyDeployments")
            .handler(resultHandler::handleFindAllRequest);

        router
            .operation("deployResources")
            .handler(DeploymentInputHandler::validateResourceArrayHasNoDuplicates)
            .handler(resultHandler::handleSaveOneRequest);

        router
            .operation("cancelDeployment")
            .handler(resultHandler::handleUpdateRequest);
    }
}
