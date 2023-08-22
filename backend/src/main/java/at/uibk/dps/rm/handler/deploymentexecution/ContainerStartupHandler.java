package at.uibk.dps.rm.handler.deploymentexecution;

import at.uibk.dps.rm.exception.BadInputException;
import at.uibk.dps.rm.exception.DeploymentTerminationFailedException;
import at.uibk.dps.rm.handler.ResultHandler;
import at.uibk.dps.rm.handler.deployment.ServiceDeploymentChecker;
import at.uibk.dps.rm.util.misc.HttpHelper;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.ext.web.RoutingContext;

/**
 * Processes requests that concern startup of containers.
 *
 * @author matthi-g
 */
public class ContainerStartupHandler {

    private final DeploymentExecutionChecker deploymentChecker;

    private final ServiceDeploymentChecker serviceDeploymentChecker;

    /**
     * Create an instance from the deploymentChecker and serviceDeploymentChecker.
     *
     * @param deploymentChecker the deployment checker
     * @param serviceDeploymentChecker the service deployment checker
     */
    public ContainerStartupHandler(DeploymentExecutionChecker deploymentChecker,
            ServiceDeploymentChecker serviceDeploymentChecker) {
        this.deploymentChecker = deploymentChecker;
        this.serviceDeploymentChecker = serviceDeploymentChecker;
    }

    /**
     * Deploy a container.
     *
     * @param rc the routing context
     */
    public void deployContainer(RoutingContext rc) {
        processDeployTerminateRequest(rc, true);
    }

    /**
     * Terminate a container.
     *
     * @param rc the routing context
     */
    public void terminateContainer(RoutingContext rc) {
        processDeployTerminateRequest(rc, false);
    }

    /**
     * Process a deploy or terminate request for containers.
     *
     * @param rc the routing context
     * @param isStartup if the request is for startup or termination of a container
     */
    private void processDeployTerminateRequest(RoutingContext rc, boolean isStartup) {
        long accountId = rc.user().principal().getLong("account_id");
        HttpHelper.getLongPathParam(rc, "deploymentId")
            .flatMapMaybe(deploymentId -> HttpHelper.getLongPathParam(rc, "resourceDeploymentId")
                .flatMapMaybe(resourceDeploymentId -> serviceDeploymentChecker
                    .checkReadyForStartup(deploymentId, resourceDeploymentId, accountId)
                    .andThen(Single.defer(() -> Single.just(1L)))
                    .flatMapMaybe(result -> {
                        if (isStartup) {
                            return deploymentChecker.startContainer(deploymentId, resourceDeploymentId).toMaybe();
                        } else {
                            return deploymentChecker.stopContainer(deploymentId, resourceDeploymentId)
                                .toMaybe();
                        }
                    })))
            .subscribe(result -> {
                    if (isStartup) {
                        rc.response().setStatusCode(200).end(result.encodePrettily());
                    } else {
                        rc.response().setStatusCode(204).end();
                    }
                },
                throwable -> {
                    Throwable throwable1 = throwable;
                    if (throwable instanceof DeploymentTerminationFailedException) {
                        throwable1 = new BadInputException("Deployment failed. See deployment logs for details.");
                    }
                    ResultHandler.handleRequestError(rc, throwable1);
                });
    }
}
