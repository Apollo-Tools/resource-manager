package at.uibk.dps.rm.router.deployment;

import at.uibk.dps.rm.handler.ResultHandler;
import at.uibk.dps.rm.handler.account.CredentialsChecker;
import at.uibk.dps.rm.handler.deploymentexecution.DeploymentExecutionChecker;
import at.uibk.dps.rm.handler.deploymentexecution.DeploymentExecutionHandler;
import at.uibk.dps.rm.handler.log.LogChecker;
import at.uibk.dps.rm.handler.log.DeploymentLogChecker;
import at.uibk.dps.rm.handler.deployment.*;
import at.uibk.dps.rm.handler.resourceprovider.VPCChecker;
import at.uibk.dps.rm.handler.util.FileSystemChecker;
import at.uibk.dps.rm.router.Route;
import at.uibk.dps.rm.service.ServiceProxyProvider;
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
        DeploymentExecutionChecker deploymentExecutionChecker =
            new DeploymentExecutionChecker(serviceProxyProvider.getDeploymentExecutionService(),
            serviceProxyProvider.getLogService(), serviceProxyProvider.getDeploymentLogService());
        CredentialsChecker credentialsChecker = new CredentialsChecker(serviceProxyProvider
            .getCredentialsService());
        ResourceDeploymentChecker resourceDeploymentChecker =
            new ResourceDeploymentChecker(serviceProxyProvider.getResourceDeploymentService());
        FunctionDeploymentChecker functionDeploymentChecker = new FunctionDeploymentChecker(serviceProxyProvider
            .getFunctionDeploymentService());
        ServiceDeploymentChecker serviceDeploymentChecker = new ServiceDeploymentChecker(serviceProxyProvider
            .getServiceDeploymentService());
        LogChecker logChecker = new LogChecker(serviceProxyProvider.getLogService());
        DeploymentLogChecker deploymentLogChecker = new DeploymentLogChecker(serviceProxyProvider
            .getDeploymentLogService());
        FileSystemChecker fileSystemChecker = new FileSystemChecker(serviceProxyProvider.getFilePathService());
        DeploymentChecker deploymentChecker = new DeploymentChecker(serviceProxyProvider.getDeploymentService());
        VPCChecker vpcChecker = new VPCChecker(serviceProxyProvider.getVpcService());
        /* Handler initialization */
        DeploymentExecutionHandler deploymentExecutionHandler =
            new DeploymentExecutionHandler(deploymentExecutionChecker, credentialsChecker, functionDeploymentChecker,
                serviceDeploymentChecker, resourceDeploymentChecker, vpcChecker);
        DeploymentErrorHandler deploymentErrorHandler = new DeploymentErrorHandler(resourceDeploymentChecker,
            logChecker, deploymentLogChecker, fileSystemChecker, deploymentExecutionHandler);
        DeploymentHandler deploymentHandler = new DeploymentHandler(deploymentChecker,
            resourceDeploymentChecker, functionDeploymentChecker, serviceDeploymentChecker,
            deploymentExecutionHandler, deploymentErrorHandler);
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
