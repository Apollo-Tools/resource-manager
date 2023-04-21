package at.uibk.dps.rm.handler.reservation;

import at.uibk.dps.rm.entity.deployment.DeploymentPath;
import at.uibk.dps.rm.entity.deployment.ReservationStatusValue;
import at.uibk.dps.rm.entity.model.Log;
import at.uibk.dps.rm.entity.model.Reservation;
import at.uibk.dps.rm.entity.model.ReservationLog;
import at.uibk.dps.rm.handler.deployment.DeploymentHandler;
import at.uibk.dps.rm.handler.log.LogChecker;
import at.uibk.dps.rm.handler.log.ReservationLogChecker;
import at.uibk.dps.rm.handler.util.FileSystemChecker;
import at.uibk.dps.rm.util.ConfigUtility;
import io.reactivex.rxjava3.core.Completable;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.core.Vertx;

/**
 * Handles errors that may occur during the deployment and termination of a reservation.
 *
 * @author matthi-g
 */
public class ReservationErrorHandler {

    private final ResourceReservationChecker resourceReservationChecker;

    private final LogChecker logChecker;

    private final ReservationLogChecker reservationLogChecker;

    private final FileSystemChecker fileSystemChecker;

    private final DeploymentHandler deploymentHandler;

    /**
     * Create an instance from the resourceReservationChecker, logChecker, reservationLogChecker,
     * fileSystemChecker and deploymentHandler.
     *
     * @param resourceReservationChecker the resource reservation checker
     * @param logChecker the log checker
     * @param reservationLogChecker the reservation log checker
     * @param fileSystemChecker the file system checker
     * @param deploymentHandler the deployment handler
     */
    public ReservationErrorHandler(ResourceReservationChecker resourceReservationChecker, LogChecker logChecker,
                                   ReservationLogChecker reservationLogChecker, FileSystemChecker fileSystemChecker,
                                   DeploymentHandler deploymentHandler) {
        this.resourceReservationChecker = resourceReservationChecker;
        this.logChecker = logChecker;
        this.reservationLogChecker = reservationLogChecker;
        this.fileSystemChecker = fileSystemChecker;
        this.deploymentHandler = deploymentHandler;
    }

    /**
     * Handle an error that occurred during deployment.
     *
     * @param accountId the id of the creator of the reservation
     * @param reservation the reservation
     * @param throwable the thrown error
     * @return a Completable
     */
    public Completable onDeploymentError(long accountId, Reservation reservation, Throwable throwable) {
        Vertx vertx = Vertx.currentContext().owner();
        return handleError(reservation, throwable)
            .andThen(new ConfigUtility(vertx).getConfig()
                .flatMap(config -> {
                    String path = new DeploymentPath(reservation.getReservationId(), config).getRootFolder()
                        .toString();
                    return fileSystemChecker
                        .checkTFLockFileExists(path);
                }))
            .flatMapCompletable(tfLockFileExists -> {
                if (tfLockFileExists) {
                    return deploymentHandler.terminateResources(reservation, accountId);
                } else {
                    return Completable.complete();
                }
            });
    }

    /**
     * Handle an error that occurred during termination.
     *
     * @param reservation the reservation
     * @param throwable the thrown error
     * @return a Completable
     */
    public Completable onTerminationError(Reservation reservation, Throwable throwable) {
        return handleError(reservation, throwable);
    }

    /**
     * Handle an error of a reservation.
     *
     * @param reservation the reservation
     * @param throwable the thrown error
     * @return a Completable
     */
    private Completable handleError(Reservation reservation, Throwable throwable) {
        return resourceReservationChecker
            .submitUpdateStatus(reservation.getReservationId(), ReservationStatusValue.ERROR)
            .toSingle(() -> {
                Log log = new Log();
                log.setLogValue(throwable.getMessage());
                return logChecker.submitCreate(JsonObject.mapFrom(log));
            })
            .flatMap(res -> res)
            .flatMap(logResult -> {
                Log logStored = logResult.mapTo(Log.class);
                ReservationLog reservationLog = new ReservationLog();
                reservationLog.setReservation(reservation);
                reservationLog.setLog(logStored);
                return reservationLogChecker.submitCreate(JsonObject.mapFrom(reservationLog));
            })
            .ignoreElement();
    }
}
