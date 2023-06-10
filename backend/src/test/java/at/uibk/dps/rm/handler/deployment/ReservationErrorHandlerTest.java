package at.uibk.dps.rm.handler.deployment;

import at.uibk.dps.rm.entity.deployment.DeploymentPath;
import at.uibk.dps.rm.entity.deployment.DeploymentStatusValue;
import at.uibk.dps.rm.entity.model.Log;
import at.uibk.dps.rm.entity.model.Deployment;
import at.uibk.dps.rm.entity.model.DeploymentLog;
import at.uibk.dps.rm.exception.DeploymentTerminationFailedException;
import at.uibk.dps.rm.handler.deploymentexecution.DeploymentExecutionHandler;
import at.uibk.dps.rm.handler.log.LogChecker;
import at.uibk.dps.rm.handler.log.DeploymentLogChecker;
import at.uibk.dps.rm.handler.util.FileSystemChecker;
import at.uibk.dps.rm.testutil.objectprovider.TestConfigProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestLogProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestReservationProvider;
import at.uibk.dps.rm.util.configuration.ConfigUtility;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.RunTestOnContext;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

/**
 * Implements tests for the {@link DeploymentErrorHandler} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class ReservationErrorHandlerTest {

    @RegisterExtension
    private static final RunTestOnContext rtoc = new RunTestOnContext();

    private DeploymentErrorHandler errorHandler;

    @Mock
    private ResourceDeploymentChecker resourceReservationChecker;

    @Mock
    private LogChecker logChecker;

    @Mock
    private DeploymentLogChecker reservationLogChecker;

    @Mock
    private FileSystemChecker fileSystemChecker;

    @Mock
    private DeploymentExecutionHandler deploymentHandler;

    @BeforeEach
    void initTest() {
        rtoc.vertx();
        JsonMapperConfig.configJsonMapper();
        errorHandler = new DeploymentErrorHandler(resourceReservationChecker, logChecker, reservationLogChecker,
            fileSystemChecker, deploymentHandler);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void onDeploymentError(boolean tfLockFileExists, VertxTestContext testContext) {
        long accountId = 1L, reservationId = 1L;
        Deployment reservation = TestReservationProvider.createReservation(reservationId);
        Throwable exc = new DeploymentTerminationFailedException();
        Log log = new Log();
        log.setLogValue("deployment/termination failed");
        Log persistedLog = TestLogProvider.createLog(1L);
        DeploymentLog reservationLog = new DeploymentLog();
        reservationLog.setDeployment(reservation);
        reservationLog.setLog(persistedLog);
        JsonObject config = TestConfigProvider.getConfig();
        DeploymentPath deploymentPath = new DeploymentPath(reservationId, config);

        when(resourceReservationChecker.submitUpdateStatus(reservationId, DeploymentStatusValue.ERROR))
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

        try (MockedConstruction<ConfigUtility> ignoredConfig = Mockito.mockConstruction(ConfigUtility.class,
            (mock, context) -> given(mock.getConfig()).willReturn(Single.just(config)))) {
            errorHandler.onDeploymentError(accountId, reservation, exc)
                .blockingSubscribe(() -> {
                    },
                    throwable -> testContext.verify(() -> fail("method has thrown exception"))
                );
        }
        testContext.completeNow();
    }

    @Test
    void onTerminationError(VertxTestContext testContext) {
        long reservationId = 1L;
        Deployment reservation = TestReservationProvider.createReservation(reservationId);
        Throwable exc = new DeploymentTerminationFailedException();
        Log log = new Log();
        log.setLogValue("deployment/termination failed");
        Log persistedLog = TestLogProvider.createLog(1L);
        DeploymentLog reservationLog = new DeploymentLog();
        reservationLog.setDeployment(reservation);
        reservationLog.setLog(persistedLog);

        when(resourceReservationChecker.submitUpdateStatus(reservationId, DeploymentStatusValue.ERROR))
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
