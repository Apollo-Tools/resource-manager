package at.uibk.dps.rm.handler.reservation;


import at.uibk.dps.rm.entity.deployment.DeploymentPath;
import at.uibk.dps.rm.entity.deployment.ReservationStatusValue;
import at.uibk.dps.rm.entity.model.Log;
import at.uibk.dps.rm.entity.model.Reservation;
import at.uibk.dps.rm.entity.model.ReservationLog;
import at.uibk.dps.rm.exception.DeploymentTerminationFailedException;
import at.uibk.dps.rm.handler.deployment.DeploymentHandler;
import at.uibk.dps.rm.handler.log.LogChecker;
import at.uibk.dps.rm.handler.log.ReservationLogChecker;
import at.uibk.dps.rm.handler.util.FileSystemChecker;
import at.uibk.dps.rm.testutil.objectprovider.TestLogProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestReservationProvider;
import at.uibk.dps.rm.util.JsonMapperConfig;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.when;

@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class ReservationErrorHandlerTest {

    private ReservationErrorHandler errorHandler;

    @Mock
    private ResourceReservationChecker resourceReservationChecker;

    @Mock
    private LogChecker logChecker;

    @Mock
    private ReservationLogChecker reservationLogChecker;

    @Mock
    private FileSystemChecker fileSystemChecker;

    @Mock
    private DeploymentHandler deploymentHandler;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        errorHandler = new ReservationErrorHandler(resourceReservationChecker, logChecker, reservationLogChecker,
            fileSystemChecker, deploymentHandler);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void onDeploymentError(boolean tfLockFileExists, VertxTestContext testContext) {
        long accountId = 1L, reservationId = 1L;
        Reservation reservation = TestReservationProvider.createReservation(reservationId);
        Throwable exc = new DeploymentTerminationFailedException();
        Log log = new Log();
        log.setLogValue("deployment/termination failed");
        Log persistedLog = TestLogProvider.createLog(1L);
        ReservationLog reservationLog = new ReservationLog();
        reservationLog.setReservation(reservation);
        reservationLog.setLog(persistedLog);
        DeploymentPath deploymentPath = new DeploymentPath(reservationId);

        when(resourceReservationChecker.submitUpdateStatus(reservationId, ReservationStatusValue.ERROR))
            .thenReturn(Completable.complete());
        when(logChecker.submitCreate(JsonObject.mapFrom(log)))
            .thenReturn(Single.just(JsonObject.mapFrom(persistedLog)));
        when(reservationLogChecker.submitCreate(JsonObject.mapFrom(reservationLog)))
            .thenReturn(Single.just(JsonObject.mapFrom(reservationLog)));
        when(fileSystemChecker.checkTFLockFileExists(deploymentPath.getRootFolder().toString()))
            .thenReturn(Single.just(tfLockFileExists));
        if (tfLockFileExists) {
            when(deploymentHandler.terminateResources(reservation, accountId))
                .thenReturn(Completable.complete());
        }

        errorHandler.onDeploymentError(accountId, reservation, exc)
            .blockingSubscribe(() -> {},
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
        testContext.completeNow();
    }

    @Test
    void onTerminationError(VertxTestContext testContext) {
        long reservationId = 1L;
        Reservation reservation = TestReservationProvider.createReservation(reservationId);
        Throwable exc = new DeploymentTerminationFailedException();
        Log log = new Log();
        log.setLogValue("deployment/termination failed");
        Log persistedLog = TestLogProvider.createLog(1L);
        ReservationLog reservationLog = new ReservationLog();
        reservationLog.setReservation(reservation);
        reservationLog.setLog(persistedLog);

        when(resourceReservationChecker.submitUpdateStatus(reservationId, ReservationStatusValue.ERROR))
            .thenReturn(Completable.complete());
        when(logChecker.submitCreate(JsonObject.mapFrom(log)))
            .thenReturn(Single.just(JsonObject.mapFrom(persistedLog)));
        when(reservationLogChecker.submitCreate(JsonObject.mapFrom(reservationLog)))
            .thenReturn(Single.just(JsonObject.mapFrom(reservationLog)));

        errorHandler.onTerminationError(reservation, exc)
            .blockingSubscribe(() -> {},
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
        testContext.completeNow();
    }
}
