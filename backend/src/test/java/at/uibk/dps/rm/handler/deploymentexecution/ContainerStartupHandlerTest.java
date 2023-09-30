package at.uibk.dps.rm.handler.deploymentexecution;

import at.uibk.dps.rm.entity.model.Account;
import at.uibk.dps.rm.exception.BadInputException;
import at.uibk.dps.rm.exception.DeploymentTerminationFailedException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.service.rxjava3.database.deployment.ServiceDeploymentService;
import at.uibk.dps.rm.testutil.RoutingContextMockHelper;
import at.uibk.dps.rm.testutil.objectprovider.TestAccountProvider;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
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
    private ServiceDeploymentService serviceDeploymentService;

    @Mock
    private RoutingContext rc;

    @Mock
    private HttpServerResponse response;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        handler = new ContainerStartupHandler(deploymentChecker, serviceDeploymentService);
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
        when(serviceDeploymentService.existsReadyForContainerStartupAndTermination(deploymentId, resourceDeploymentId,
                account.getAccountId()))
            .thenReturn(Single.just(readyForStartup));
        if (isValid && !isStartup) {
            when(rc.response()).thenReturn(response);
            when(response.setStatusCode(204)).thenReturn(response);
            when(response.end()).thenReturn(Completable.complete());
        }
        if (isValid && isStartup) {
            when(rc.response()).thenReturn(response);
            when(response.setStatusCode(200)).thenReturn(response);
            when(response.end(new JsonObject().encodePrettily())).thenReturn(Completable.complete());
        }
        if (isStartup) {
            if (isValid || readyForStartup) {
                when(deploymentChecker.startContainer(deploymentId, resourceDeploymentId))
                    .thenReturn(successfulDeployment ? Single.just(new JsonObject()) :
                        Single.error(DeploymentTerminationFailedException::new));
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
