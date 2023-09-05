package at.uibk.dps.rm.rx.handler.deploymentexecution;

import at.uibk.dps.rm.entity.model.ServiceDeployment;
import at.uibk.dps.rm.exception.BadInputException;
import at.uibk.dps.rm.exception.DeploymentTerminationFailedException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.rx.handler.ResultHandler;
import at.uibk.dps.rm.rx.service.rxjava3.database.deployment.ServiceDeploymentService;
import at.uibk.dps.rm.util.misc.HttpHelper;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RoutingContext;

/**
 * Processes requests that concern startup of containers.
 *
 * @author matthi-g
 */
public class ContainerStartupHandler {

    private final DeploymentExecutionChecker deploymentChecker;

    private final ServiceDeploymentService serviceDeploymentService;

    /**
     * Create an instance from the deploymentChecker and serviceDeploymentService.
     *
     * @param deploymentChecker the deployment checker
     * @param serviceDeploymentService the service deployment service
     */
    public ContainerStartupHandler(DeploymentExecutionChecker deploymentChecker,
            ServiceDeploymentService serviceDeploymentService) {
        this.deploymentChecker = deploymentChecker;
        this.serviceDeploymentService = serviceDeploymentService;
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
            .flatMap(deploymentId -> HttpHelper.getLongPathParam(rc, "resourceDeploymentId")
                .flatMap(resourceDeploymentId -> serviceDeploymentService
                    .existsReadyForContainerStartupAndTermination(deploymentId, resourceDeploymentId, accountId)
                    .flatMap(exists -> {
                        if (!exists) {
                            return Single.error(new NotFoundException(ServiceDeployment.class));
                        }
                        if (isStartup) {
                            return deploymentChecker.startContainer(deploymentId, resourceDeploymentId);
                        } else {
                            return deploymentChecker.stopContainer(deploymentId, resourceDeploymentId)
                                .toSingle(JsonObject::new);
                        }
                    }))
            )
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
                }
            );
    }
}
