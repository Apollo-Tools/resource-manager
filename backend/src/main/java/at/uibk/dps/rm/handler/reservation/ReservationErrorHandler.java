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
import io.reactivex.rxjava3.core.Completable;
import io.vertx.core.json.JsonObject;

public class ReservationErrorHandler {

    private final ResourceReservationChecker resourceReservationChecker;

    private final LogChecker logChecker;

    private final ReservationLogChecker reservationLogChecker;

    private final FileSystemChecker fileSystemChecker;

    private final DeploymentHandler deploymentHandler;

    public ReservationErrorHandler(ResourceReservationChecker resourceReservationChecker, LogChecker logChecker,
                                   ReservationLogChecker reservationLogChecker, FileSystemChecker fileSystemChecker,
                                   DeploymentHandler deploymentHandler) {
        this.resourceReservationChecker = resourceReservationChecker;
        this.logChecker = logChecker;
        this.reservationLogChecker = reservationLogChecker;
        this.fileSystemChecker = fileSystemChecker;
        this.deploymentHandler = deploymentHandler;
    }

    public Completable onDeploymentError(long accountId, Reservation reservation, Throwable throwable) {
        return handleError(reservation, throwable)
            .andThen(fileSystemChecker
                .checkTFLockFileExists(new DeploymentPath(reservation.getReservationId())
                    .getRootFolder().toString()))
            .flatMapCompletable(tfLockFileExists -> deploymentHandler
                .terminateResources(reservation, accountId));
    }

    public Completable onTerminationError(Reservation reservation, Throwable throwable) {
        return handleError(reservation, throwable);
    }

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
