package at.uibk.dps.rm.router.deployment;

import at.uibk.dps.rm.entity.dto.DeployResourcesRequest;
import at.uibk.dps.rm.entity.model.Deployment;
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
import io.reactivex.rxjava3.core.Completable;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;

/**
 * Used to initialise the deployment route.
 *
 * @author matthi-g
 */
public class DeploymentRoute implements Route {

    private static final Logger logger = LoggerFactory.getLogger(DeploymentRoute.class);

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
            resourceDeploymentChecker, functionDeploymentChecker, serviceDeploymentChecker);
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
            .handler(rc -> deploymentHandler.postOneToAccount(rc)
                .map(result -> {
                    DeployResourcesRequest request = rc.body()
                        .asJsonObject()
                        .mapTo(DeployResourcesRequest.class);
                    Deployment deployment = result.mapTo(Deployment.class);
                    long accountId = rc.user().principal().getLong("account_id");
                    initiateDeployment(request, deployment, accountId, deploymentExecutionHandler,
                        deploymentErrorHandler);
                    return result;
                })
                .subscribe(result -> ResultHandler.getSaveResponse(rc, result),
                    throwable -> ResultHandler.handleRequestError(rc, throwable))
            );

        router
            .operation("cancelDeployment")
            .handler(rc -> deploymentHandler.cancelDeployment(rc)
                .flatMapCompletable(deploymentJson -> {
                    long accountId = rc.user().principal().getLong("account_id");
                    Deployment deployment = deploymentJson.mapTo(Deployment.class);
                    initiateTermination(deployment, accountId, deploymentExecutionHandler, deploymentErrorHandler);
                    return Completable.complete();
                })
                .subscribe(() -> ResultHandler.getSaveAllUpdateDeleteResponse(rc),
                    throwable -> ResultHandler.handleRequestError(rc, throwable))
            );
    }

    private static void initiateDeployment(DeployResourcesRequest request, Deployment deployment, long accountId,
            DeploymentExecutionHandler deploymentExecutionHandler, DeploymentErrorHandler deploymentErrorHandler) {
        deploymentExecutionHandler.deployResources(deployment, accountId, request.getCredentials())
            .doOnError(throwable -> logger.error(throwable.getMessage()))
            .onErrorResumeNext(throwable -> deploymentErrorHandler.onDeploymentError(accountId,
                deployment, throwable))
            .subscribe();
    }

    /**
     * Execute the termination of the resources contained in the deployment.
     *
     * @param deployment the deployment
     * @param accountId the id of the creator of the deployment
     */
    private static void initiateTermination(Deployment deployment, long accountId,
           DeploymentExecutionHandler deploymentExecutionHandler, DeploymentErrorHandler deploymentErrorHandler) {
        deploymentExecutionHandler.terminateResources(deployment, accountId)
            .doOnError(throwable -> logger.error(throwable.getMessage()))
            .onErrorResumeNext(throwable -> deploymentErrorHandler.onTerminationError(deployment, throwable))
            .subscribe();
    }
}
