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
import at.uibk.dps.rm.testutil.mockprovider.Mockprovider;
import at.uibk.dps.rm.testutil.objectprovider.TestConfigProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestLogProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestDeploymentProvider;
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
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.when;

/**
 * Implements tests for the {@link DeploymentErrorHandler} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class DeploymentErrorHandlerTest {

    @RegisterExtension
    private static final RunTestOnContext rtoc = new RunTestOnContext();

    private DeploymentErrorHandler errorHandler;

    @Mock
    private ResourceDeploymentChecker resourceDeploymentChecker;

    @Mock
    private LogChecker logChecker;

    @Mock
    private DeploymentLogChecker deploymentLogChecker;

    @Mock
    private FileSystemChecker fileSystemChecker;

    @Mock
    private DeploymentExecutionHandler deploymentHandler;

    @BeforeEach
    void initTest() {
        rtoc.vertx();
        JsonMapperConfig.configJsonMapper();
        errorHandler = new DeploymentErrorHandler(resourceDeploymentChecker, logChecker, deploymentLogChecker,
            fileSystemChecker, deploymentHandler);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void onDeploymentError(boolean tfLockFileExists, VertxTestContext testContext) {
        long accountId = 1L, deploymentId = 1L;
        Deployment deployment = TestDeploymentProvider.createDeployment(deploymentId);
        Throwable exc = new DeploymentTerminationFailedException();
        Log log = new Log();
        log.setLogValue("deployment/termination failed");
        Log persistedLog = TestLogProvider.createLog(1L);
        DeploymentLog deploymentLog = new DeploymentLog();
        deploymentLog.setDeployment(deployment);
        deploymentLog.setLog(persistedLog);
        JsonObject config = TestConfigProvider.getConfig();
        DeploymentPath deploymentPath = new DeploymentPath(deploymentId, config);

        when(resourceDeploymentChecker.submitUpdateStatus(deploymentId, DeploymentStatusValue.ERROR))
            .thenReturn(Completable.complete());
        when(logChecker.submitCreate(JsonObject.mapFrom(log)))
            .thenReturn(Single.just(JsonObject.mapFrom(persistedLog)));
        when(deploymentLogChecker.submitCreate(JsonObject.mapFrom(deploymentLog)))
            .thenReturn(Single.just(JsonObject.mapFrom(deploymentLog)));
        when(fileSystemChecker.checkTFLockFileExists(deploymentPath.getRootFolder().toString()))
            .thenReturn(Single.just(tfLockFileExists));
        if (tfLockFileExists) {
            when(deploymentHandler.terminateResources(deployment, accountId))
                .thenReturn(Completable.complete());
        }

        try (MockedConstruction<ConfigUtility> ignoredConfig = Mockprovider.mockConfig(config)) {
            errorHandler.onDeploymentError(accountId, deployment, exc)
                .blockingSubscribe(() -> {
                    },
                    throwable -> testContext.verify(() -> fail("method has thrown exception"))
                );
        }
        testContext.completeNow();
    }

    @Test
    void onTerminationError(VertxTestContext testContext) {
        long deploymentId = 1L;
        Deployment deployment = TestDeploymentProvider.createDeployment(deploymentId);
        Throwable exc = new DeploymentTerminationFailedException();
        Log log = new Log();
        log.setLogValue("deployment/termination failed");
        Log persistedLog = TestLogProvider.createLog(1L);
        DeploymentLog deploymentLog = new DeploymentLog();
        deploymentLog.setDeployment(deployment);
        deploymentLog.setLog(persistedLog);

        when(resourceDeploymentChecker.submitUpdateStatus(deploymentId, DeploymentStatusValue.ERROR))
            .thenReturn(Completable.complete());
        when(logChecker.submitCreate(JsonObject.mapFrom(log)))
            .thenReturn(Single.just(JsonObject.mapFrom(persistedLog)));
        when(deploymentLogChecker.submitCreate(JsonObject.mapFrom(deploymentLog)))
            .thenReturn(Single.just(JsonObject.mapFrom(deploymentLog)));

        errorHandler.onTerminationError(deployment, exc)
            .blockingSubscribe(() -> {},
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
        testContext.completeNow();
    }
}
