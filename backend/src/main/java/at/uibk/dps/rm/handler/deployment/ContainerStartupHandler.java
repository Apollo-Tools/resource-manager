package at.uibk.dps.rm.handler.deployment;

import at.uibk.dps.rm.exception.BadInputException;
import at.uibk.dps.rm.exception.DeploymentTerminationFailedException;
import at.uibk.dps.rm.handler.ResultHandler;
import at.uibk.dps.rm.handler.reservation.ServiceReservationChecker;
import at.uibk.dps.rm.util.misc.HttpHelper;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.ext.web.RoutingContext;

/**
 * Processes requests that concern startup of containers.
 *
 * @author matthi-g
 */
public class ContainerStartupHandler {

    private final DeploymentChecker deploymentChecker;

    private final ServiceReservationChecker serviceReservationChecker;

    /**
     * Create an instance from the deploymentChecker and resourceReservationChecker.
     *
     * @param deploymentChecker the deployment checker
     * @param serviceReservationChecker the service reservation checker
     */
    public ContainerStartupHandler(DeploymentChecker deploymentChecker,
            ServiceReservationChecker serviceReservationChecker) {
        this.deploymentChecker = deploymentChecker;
        this.serviceReservationChecker = serviceReservationChecker;
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
        HttpHelper.getLongPathParam(rc, "reservationId")
            .flatMapCompletable(reservationId -> HttpHelper.getLongPathParam(rc, "resourceReservationId")
                .flatMapCompletable(resourceReservationId -> serviceReservationChecker
                    .checkReadyForStartup(reservationId, resourceReservationId, accountId)
                    .andThen(Single.defer(() -> Single.just(1L)))
                    .flatMapCompletable(result -> {
                        if (isStartup) {
                            return deploymentChecker.deployContainer(reservationId, resourceReservationId);
                        } else {
                            return deploymentChecker.terminateContainer(reservationId, resourceReservationId);
                        }
                    })))
            .subscribe(() -> rc.response().setStatusCode(204).end(),
                throwable -> {
                    Throwable throwable1 = throwable;
                    if (throwable instanceof DeploymentTerminationFailedException) {
                        throwable1 = new BadInputException("Deployment failed. See reservation logs for details.");
                    }
                    ResultHandler.handleRequestError(rc, throwable1);
                });
    }
}
