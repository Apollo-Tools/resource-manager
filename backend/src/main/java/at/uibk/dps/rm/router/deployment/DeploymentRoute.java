package at.uibk.dps.rm.router.deployment;

import at.uibk.dps.rm.entity.deployment.DeploymentStatusValue;
import at.uibk.dps.rm.entity.dto.deployment.DeployResourcesDTO;
import at.uibk.dps.rm.entity.dto.deployment.TerminateResourcesDTO;
import at.uibk.dps.rm.handler.PrivateEntityResultHandler;
import at.uibk.dps.rm.handler.ResultHandler;
import at.uibk.dps.rm.handler.deploymentexecution.DeploymentExecutionChecker;
import at.uibk.dps.rm.handler.deployment.*;
import at.uibk.dps.rm.router.Route;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import io.reactivex.rxjava3.core.Completable;
import io.vertx.core.json.JsonObject;
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
        /* Handler initialization */
        DeploymentErrorHandler deploymentErrorHandler = new DeploymentErrorHandler(
            serviceProxyProvider.getDeploymentService(), deploymentExecutionChecker);
        DeploymentHandler deploymentHandler = new DeploymentHandler(serviceProxyProvider.getDeploymentService());
        ResultHandler resultHandler = new PrivateEntityResultHandler(deploymentHandler);

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
                    DeployResourcesDTO deployResources = result.mapTo(DeployResourcesDTO.class);
                    Completable completable = deploymentExecutionChecker.applyResourceDeployment(deployResources)
                        .flatMapCompletable(tfOutput -> serviceProxyProvider.getDeploymentService()
                                .handleDeploymentSuccessful(new JsonObject(tfOutput.getOutput()), deployResources));
                    deploymentErrorHandler.handleDeployResources(completable, deployResources);
                    return result.getJsonObject("deployment");
                })
                .subscribe(result -> ResultHandler.getSaveResponse(rc, result),
                    throwable -> ResultHandler.handleRequestError(rc, throwable))
            );

        router
            .operation("cancelDeployment")
            .handler(rc -> deploymentHandler.cancelDeployment(rc)
                .flatMapCompletable(terminationJson -> {
                    TerminateResourcesDTO terminateResources = terminationJson.mapTo(TerminateResourcesDTO.class);
                    long deploymentId = terminateResources.getDeployment().getDeploymentId();
                    Completable completable = deploymentExecutionChecker.terminateResources(terminateResources)
                        .andThen(Completable.defer(() -> deploymentExecutionChecker.deleteTFDirs(deploymentId)))
                        .andThen(Completable.defer(() -> serviceProxyProvider.getResourceDeploymentService()
                            .updateStatusByDeploymentId(deploymentId, DeploymentStatusValue.TERMINATED)));
                    deploymentErrorHandler.handleTerminateResources(completable, deploymentId);
                    return Completable.complete();
                })
                .subscribe(() -> ResultHandler.getSaveAllUpdateDeleteResponse(rc),
                    throwable -> ResultHandler.handleRequestError(rc, throwable))
            );
    }
}
