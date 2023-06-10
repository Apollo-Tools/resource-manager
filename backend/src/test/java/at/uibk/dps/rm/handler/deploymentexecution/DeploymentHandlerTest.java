package at.uibk.dps.rm.handler.deploymentexecution;

import at.uibk.dps.rm.entity.deployment.ProcessOutput;
import at.uibk.dps.rm.entity.dto.credentials.DockerCredentials;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.exception.DeploymentTerminationFailedException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.handler.account.CredentialsChecker;
import at.uibk.dps.rm.handler.deployment.FunctionDeploymentChecker;
import at.uibk.dps.rm.handler.deployment.ResourceDeploymentChecker;
import at.uibk.dps.rm.handler.deployment.ServiceDeploymentChecker;
import at.uibk.dps.rm.testutil.objectprovider.*;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Implements tests for the {@link DeploymentExecutionHandler} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class DeploymentHandlerTest {

    private DeploymentExecutionHandler deploymentHandler;

    @Mock
    private DeploymentExecutionChecker deploymentChecker;

    @Mock
    private CredentialsChecker credentialsChecker;

    @Mock
    private FunctionDeploymentChecker functionReservationChecker;

    @Mock
    private ServiceDeploymentChecker serviceReservationChecker;

    @Mock
    private ResourceDeploymentChecker resourceReservationChecker;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        deploymentHandler = new DeploymentExecutionHandler(deploymentChecker, credentialsChecker, functionReservationChecker,
            serviceReservationChecker, resourceReservationChecker);
    }

    @ParameterizedTest
    @ValueSource(strings = {"valid", "deployResourcesFailed", "serviceReservationsNotFound",
        "functionReservationsNotFound"})
    void deployResources(String testCase, VertxTestContext testContext) {
        long accountId = 1L;
        Deployment reservation = TestReservationProvider.createReservation(1L);
        JsonObject sr1 = JsonObject.mapFrom(TestReservationProvider
            .createServiceReservation(2L, reservation));
        JsonObject sr2 = JsonObject.mapFrom(TestReservationProvider
            .createServiceReservation(3L, reservation));
        JsonObject fr1 = JsonObject.mapFrom(TestReservationProvider.createFunctionReservation(3L, reservation));
        DockerCredentials dockerCredentials = TestDTOProvider.createDockerCredentials();
        String kubeConfig = TestDTOProvider.createKubeConfigValue();
        ResourceProvider rp = TestResourceProviderProvider.createResourceProvider(1L, "aws");
        Region region = TestResourceProviderProvider.createRegion(1L, "us-east-1", rp);
        VPC vpc = TestResourceProviderProvider.createVPC(1L, region);
        JsonObject credentials = JsonObject.mapFrom(TestAccountProvider.createCredentials(1L, rp));

        when(credentialsChecker.checkFindAll(accountId)).thenReturn(Single.just(new JsonArray(List.of(credentials))));
        when(functionReservationChecker.checkFindAllByDeploymentId(1L)).thenReturn(testCase.startsWith("functionRes") ?
            Single.error(NotFoundException::new) : Single.just(new JsonArray(List.of(fr1))));
        if (!testCase.equals("functionReservationsNotFound")) {
            when(serviceReservationChecker.checkFindAllByDeploymentId(1L))
                .thenReturn(testCase.startsWith("serviceRes") ? Single.error(NotFoundException::new) :
                    Single.just(new JsonArray(List.of(sr1, sr2))));
        }

        if (testCase.equals("valid") || testCase.equals("deployResourcesFailed")) {
            when(deploymentChecker.applyResourceDeployment(any())).thenReturn(testCase.equals("deployResourcesFailed") ?
                Single.error(DeploymentTerminationFailedException::new) : Single.just(new ProcessOutput()));
        }
        if (testCase.equals("valid")) {
            when(resourceReservationChecker.storeOutputToResourceDeployments(any(), any()))
                .thenReturn(Completable.complete());
        }

        deploymentHandler.deployResources(reservation, accountId, dockerCredentials, kubeConfig, List.of(vpc))
            .blockingSubscribe(() -> testContext.verify(() -> {
                if (!testCase.equals("valid")) {
                    fail("method did not throw exception");
                }
            }), throwable -> testContext.verify(() -> {
                if (testCase.equals("valid")) {
                    fail("method has thrown exception");
                } else if (testCase.equals("deployResourcesFailed")) {
                    assertThat(throwable).isInstanceOf(DeploymentTerminationFailedException.class);
                } else {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                }
                testContext.completeNow();
            }));
        testContext.completeNow();
    }

    @ParameterizedTest
    @ValueSource(strings = {"valid", "deleteTFDirsFailed", "terminateResourcesFailed"})
    void terminateResources(String testCase, VertxTestContext testContext) {
        long accountId = 1L;
        Deployment reservation = TestReservationProvider.createReservation(1L);
        ResourceProvider rp = TestResourceProviderProvider.createResourceProvider(1L, "aws");
        JsonObject credentials = JsonObject.mapFrom(TestAccountProvider.createCredentials(1L, rp));
        JsonObject sr1 = JsonObject.mapFrom(TestReservationProvider
            .createServiceReservation(2L, reservation));
        JsonObject sr2 = JsonObject.mapFrom(TestReservationProvider
            .createServiceReservation(3L, reservation));
        JsonObject fr1 = JsonObject.mapFrom(TestReservationProvider.createFunctionReservation(3L, reservation));

        when(credentialsChecker.checkFindAll(accountId)).thenReturn(Single.just(new JsonArray(List.of(credentials))));
        when(functionReservationChecker.checkFindAllByDeploymentId(1L))
            .thenReturn(Single.just(new JsonArray(List.of(fr1))));
        when(serviceReservationChecker.checkFindAllByDeploymentId(1L))
            .thenReturn(Single.just(new JsonArray(List.of(sr1, sr2))));
        when(deploymentChecker.terminateResources(any())).thenReturn(testCase.equals("terminateResourcesFailed") ?
            Completable.error(DeploymentTerminationFailedException::new) : Completable.complete());
        if (testCase.equals("valid") || testCase.equals("deleteTFDirsFailed")) {
            when(deploymentChecker.deleteTFDirs(reservation.getDeploymentId()))
                .thenReturn(testCase.startsWith("delete") ? Completable.error(IOException::new) :
                    Completable.complete());
        }

        deploymentHandler.terminateResources(reservation, accountId)
            .blockingSubscribe(() -> testContext.verify(() -> {
                if (!testCase.equals("valid")) {
                    fail("method did not throw exception");
                }
            }), throwable -> testContext.verify(() -> {
                if (testCase.equals("valid")) {
                    fail("method has thrown exception");
                } else if (testCase.equals("terminateResourcesFailed")) {
                    assertThat(throwable).isInstanceOf(DeploymentTerminationFailedException.class);
                } else {
                    assertThat(throwable).isInstanceOf(IOException.class);
                }
                testContext.completeNow();
            }));
        testContext.completeNow();
    }
}
