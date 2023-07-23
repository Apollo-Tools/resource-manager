package at.uibk.dps.rm.handler.deployment;

import at.uibk.dps.rm.entity.dto.deployment.DeployResourcesDTO;
import at.uibk.dps.rm.entity.dto.deployment.TerminateResourcesDTO;
import at.uibk.dps.rm.exception.DeploymentTerminationFailedException;
import at.uibk.dps.rm.handler.deploymentexecution.DeploymentExecutionChecker;
import at.uibk.dps.rm.testutil.mockprovider.Mockprovider;
import at.uibk.dps.rm.testutil.objectprovider.*;
import at.uibk.dps.rm.util.configuration.ConfigUtility;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

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
    private DeploymentChecker deploymentChecker;

    @Mock
    private DeploymentExecutionChecker deploymentExecutionChecker;

    @BeforeEach
    void initTest() {
        rtoc.vertx();
        JsonMapperConfig.configJsonMapper();
        errorHandler = new DeploymentErrorHandler(deploymentChecker, deploymentExecutionChecker);
    }

    @Test
    void handleDeployResourcesNoError(VertxTestContext testContext) {
        Completable completable = Completable.complete();
        DeployResourcesDTO deployResourcesDTO = TestRequestProvider.createDeployRequest();

        errorHandler.handleDeployResources(completable, deployResourcesDTO);
        testContext.completeNow();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void handleDeployResourcesError(boolean lockFileExists,  VertxTestContext testContext) {
        Throwable throwable = new DeploymentTerminationFailedException();
        Completable completable = Completable.error(throwable);
        DeployResourcesDTO deployResourcesDTO = TestRequestProvider.createDeployRequest();

        when(deploymentChecker.handleDeploymentError(deployResourcesDTO.getDeployment().getDeploymentId(),
            throwable.getMessage())).thenReturn(Completable.complete());
        when(deploymentExecutionChecker.tfLockFileExists(anyString())).thenReturn(Single.just(lockFileExists));
        if (lockFileExists) {
            when(deploymentExecutionChecker.terminateResources(any(TerminateResourcesDTO.class)))
                .thenReturn(Completable.complete());
        }
        when(deploymentExecutionChecker.deleteTFDirs(deployResourcesDTO.getDeployment().getDeploymentId()))
            .thenReturn(Completable.complete());

        try (MockedConstruction<ConfigUtility> ignored = Mockprovider.mockConfig(TestConfigProvider.getConfig())) {
            errorHandler.handleDeployResources(completable, deployResourcesDTO);
            testContext.completeNow();
        }
    }

    @Test
    void handleTerminateResourcesNoError(VertxTestContext testContext) {
        Completable completable = Completable.complete();
        long deploymentId = 1L;

        errorHandler.handleTerminateResources(completable, deploymentId);
        testContext.completeNow();
    }

    @Test
    void handleTerminateResourcesError(VertxTestContext testContext) {
        Throwable throwable = new DeploymentTerminationFailedException();
        Completable completable = Completable.error(throwable);
        long deploymentId = 1L;

        when(deploymentChecker.handleDeploymentError(deploymentId, throwable.getMessage()))
            .thenReturn(Completable.complete());

        errorHandler.handleTerminateResources(completable, deploymentId);
        testContext.completeNow();
    }
}
