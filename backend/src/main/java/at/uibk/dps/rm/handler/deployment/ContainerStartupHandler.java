package at.uibk.dps.rm.handler.deployment;

import at.uibk.dps.rm.handler.ResultHandler;
import at.uibk.dps.rm.handler.reservation.ResourceReservationChecker;
import at.uibk.dps.rm.util.misc.HttpHelper;
import io.vertx.rxjava3.ext.web.RoutingContext;

public class ContainerStartupHandler {

    private final ResourceReservationChecker resourceReservationChecker;

    private final DeploymentChecker deploymentChecker;

    public ContainerStartupHandler(DeploymentChecker deploymentChecker,
            ResourceReservationChecker resourceReservationChecker) {
        this.deploymentChecker = deploymentChecker;
        this.resourceReservationChecker = resourceReservationChecker;
    }

    public void deployContainer(RoutingContext rc) {
        long accountId = rc.user().principal().getLong("account_id");
        HttpHelper.getLongPathParam(rc, "reservationId")
            .flatMapCompletable(reservationId -> HttpHelper.getLongPathParam(rc, "resourceReservationId")
                .flatMapCompletable(resourceReservationId -> resourceReservationChecker
                    .checkExistsForStartup(reservationId, resourceReservationId, accountId)
                    .flatMapCompletable(result -> deploymentChecker.deployContainer(reservationId, resourceReservationId))))
            .subscribe(() -> rc.response().setStatusCode(204).end(),
                throwable -> ResultHandler.handleRequestError(rc,
                throwable));
    }

    public void terminateContainer(RoutingContext rc) {
        long accountId = rc.user().principal().getLong("account_id");
        HttpHelper.getLongPathParam(rc, "reservationId")
            .flatMapCompletable(reservationId -> HttpHelper.getLongPathParam(rc, "resourceReservationId")
                .flatMapCompletable(resourceReservationId -> resourceReservationChecker
                    .checkExistsForStartup(reservationId, resourceReservationId, accountId)
                    .flatMapCompletable(result -> deploymentChecker.terminateContainer(reservationId, resourceReservationId))))
            .subscribe(() -> rc.response().setStatusCode(204).end(),
                throwable -> ResultHandler.handleRequestError(rc,
                throwable));
    }
}
