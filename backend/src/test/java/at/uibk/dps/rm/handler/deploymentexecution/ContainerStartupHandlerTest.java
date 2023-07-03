package at.uibk.dps.rm.handler.deploymentexecution;

import at.uibk.dps.rm.entity.model.Account;
import at.uibk.dps.rm.exception.BadInputException;
import at.uibk.dps.rm.exception.DeploymentTerminationFailedException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.handler.deployment.ServiceDeploymentChecker;
import at.uibk.dps.rm.testutil.RoutingContextMockHelper;
import at.uibk.dps.rm.testutil.objectprovider.TestAccountProvider;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.reactivex.rxjava3.core.Completable;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.core.http.HttpServerResponse;
import io.vertx.rxjava3.ext.web.RoutingContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Implements tests for the {@link ContainerStartupHandler} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class ContainerStartupHandlerTest {

    private ContainerStartupHandler handler;

    @Mock
    private DeploymentExecutionChecker deploymentChecker;

    @Mock
    private ServiceDeploymentChecker serviceDeploymentChecker;

    @Mock
    private RoutingContext rc;

    @Mock
    private HttpServerResponse response;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        handler = new ContainerStartupHandler(deploymentChecker, serviceDeploymentChecker);
    }

    @ParameterizedTest
    @CsvSource({
        "true, true, true, true",
        "true, false, true, false",
        "true, true, false, false",
        "false, true, true, true",
        "false, false, true, false",
        "false, true, false, false"
    })
    void deployContainer(boolean isStartup, boolean readyForStartup, boolean successfulDeployment, boolean isValid,
            VertxTestContext testContext) {
        long deploymentId = 1L, resourceDeploymentId = 2L;
        Account account = TestAccountProvider.createAccount(1L);

        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        when(rc.pathParam("deploymentId")).thenReturn(String.valueOf(deploymentId));
        when(rc.pathParam("resourceDeploymentId")).thenReturn(String.valueOf(resourceDeploymentId));
        when(serviceDeploymentChecker.checkReadyForStartup(deploymentId, resourceDeploymentId, account.getAccountId()))
            .thenReturn(readyForStartup ? Completable.complete() : Completable.error(NotFoundException::new));
        if (isValid) {
            when(rc.response()).thenReturn(response);
            when(response.setStatusCode(204)).thenReturn(response);
            when(response.end()).thenReturn(Completable.complete());
        }
        if (isStartup) {
            if (isValid || readyForStartup) {
                when(deploymentChecker.startContainer(deploymentId, resourceDeploymentId))
                    .thenReturn(successfulDeployment ? Completable.complete() :
                        Completable.error(DeploymentTerminationFailedException::new));
            }
            handler.deployContainer(rc);
        } else {
            if (isValid || readyForStartup) {
                when(deploymentChecker.stopContainer(deploymentId, resourceDeploymentId))
                    .thenReturn(successfulDeployment ? Completable.complete() :
                        Completable.error(DeploymentTerminationFailedException::new));
            }
            handler.terminateContainer(rc);
        }
        if (!readyForStartup) {
            verify(rc).fail(eq(404), any(NotFoundException.class));
        } else if (!successfulDeployment) {
            verify(rc).fail(eq(400), any(BadInputException.class));
        }
        testContext.completeNow();
    }
}