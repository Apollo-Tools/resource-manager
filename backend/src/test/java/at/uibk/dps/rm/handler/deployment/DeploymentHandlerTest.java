package at.uibk.dps.rm.handler.deployment;

import at.uibk.dps.rm.entity.deployment.ProcessOutput;
import at.uibk.dps.rm.entity.dto.credentials.DockerCredentials;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.exception.DeploymentTerminationFailedException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.handler.account.CredentialsChecker;
import at.uibk.dps.rm.handler.function.FunctionResourceChecker;
import at.uibk.dps.rm.handler.reservation.ResourceReservationChecker;
import at.uibk.dps.rm.testutil.objectprovider.*;
import at.uibk.dps.rm.util.JsonMapperConfig;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class DeploymentHandlerTest {

    private DeploymentHandler deploymentHandler;

    @Mock
    private DeploymentChecker deploymentChecker;

    @Mock
    private CredentialsChecker credentialsChecker;

    @Mock
    private FunctionResourceChecker functionResourceChecker;

    @Mock
    private ResourceReservationChecker resourceReservationChecker;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        deploymentHandler = new DeploymentHandler(deploymentChecker, credentialsChecker, functionResourceChecker,
            resourceReservationChecker);
    }

    @Test
    void deployResources(VertxTestContext testContext) {
        long accountId = 1L;
        Reservation reservation = TestReservationProvider.createReservation(1L);
        DockerCredentials dockerCredentials = TestDTOProvider.createDockerCredentials();
        ResourceProvider rp = TestResourceProviderProvider.createResourceProvider(1L, "aws");
        Region region = TestResourceProviderProvider.createRegion(1L, "us-east-1", rp);
        VPC vpc = TestResourceProviderProvider.createVPC(1L, region);
        JsonObject credentials = JsonObject.mapFrom(TestAccountProvider.createCredentials(1L, rp));
        JsonObject fr1 = JsonObject.mapFrom(TestFunctionProvider.createFunctionResource(1L));
        JsonObject fr2 = JsonObject.mapFrom(TestFunctionProvider.createFunctionResource(1L));
        JsonArray functionResources = new JsonArray(List.of(fr1, fr2));

        when(credentialsChecker.checkFindAll(accountId)).thenReturn(Single.just(new JsonArray(List.of(credentials))));
        when(functionResourceChecker.checkFindAllByReservationId(reservation.getReservationId()))
            .thenReturn(Single.just(functionResources));
        when(deploymentChecker.deployResources(any())).thenReturn(Single.just(new ProcessOutput()));
        when(resourceReservationChecker.storeOutputToFunctionResources(any(), any())).thenReturn(Completable.complete());

        deploymentHandler.deployResources(reservation, accountId, dockerCredentials, List.of(vpc))
            .blockingSubscribe(() -> {
                },
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
        testContext.completeNow();
    }

    @Test
    void deployResourcesStoreOutputDeploymentFailed(VertxTestContext testContext) {
        long accountId = 1L;
        Reservation reservation = TestReservationProvider.createReservation(1L);
        DockerCredentials dockerCredentials = TestDTOProvider.createDockerCredentials();
        ResourceProvider rp = TestResourceProviderProvider.createResourceProvider(1L, "aws");
        Region region = TestResourceProviderProvider.createRegion(1L, "us-east-1", rp);
        VPC vpc = TestResourceProviderProvider.createVPC(1L, region);
        JsonObject credentials = JsonObject.mapFrom(TestAccountProvider.createCredentials(1L, rp));
        JsonObject fr1 = JsonObject.mapFrom(TestFunctionProvider.createFunctionResource(1L));
        JsonObject fr2 = JsonObject.mapFrom(TestFunctionProvider.createFunctionResource(1L));
        JsonArray functionResources = new JsonArray(List.of(fr1, fr2));

        when(credentialsChecker.checkFindAll(accountId)).thenReturn(Single.just(new JsonArray(List.of(credentials))));
        when(functionResourceChecker.checkFindAllByReservationId(reservation.getReservationId()))
            .thenReturn(Single.just(functionResources));
        when(deploymentChecker.deployResources(any())).thenReturn(Single.error(DeploymentTerminationFailedException::new));

        deploymentHandler.deployResources(reservation, accountId, dockerCredentials, List.of(vpc))
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(DeploymentTerminationFailedException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void deployResourcesStoreOutputFunctionResourcesNotFound(VertxTestContext testContext) {
        long accountId = 1L;
        Reservation reservation = TestReservationProvider.createReservation(1L);
        DockerCredentials dockerCredentials = TestDTOProvider.createDockerCredentials();
        ResourceProvider rp = TestResourceProviderProvider.createResourceProvider(1L, "aws");
        Region region = TestResourceProviderProvider.createRegion(1L, "us-east-1", rp);
        VPC vpc = TestResourceProviderProvider.createVPC(1L, region);
        JsonObject credentials = JsonObject.mapFrom(TestAccountProvider.createCredentials(1L, rp));

        when(credentialsChecker.checkFindAll(accountId)).thenReturn(Single.just(new JsonArray(List.of(credentials))));
        when(functionResourceChecker.checkFindAllByReservationId(reservation.getReservationId()))
            .thenReturn(Single.error(NotFoundException::new));

        deploymentHandler.deployResources(reservation, accountId, dockerCredentials, List.of(vpc))
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void deployResourcesStoreOutputCredentialsNotFound(VertxTestContext testContext) {
        long accountId = 1L;
        Reservation reservation = TestReservationProvider.createReservation(1L);
        DockerCredentials dockerCredentials = TestDTOProvider.createDockerCredentials();
        ResourceProvider rp = TestResourceProviderProvider.createResourceProvider(1L, "aws");
        Region region = TestResourceProviderProvider.createRegion(1L, "us-east-1", rp);
        VPC vpc = TestResourceProviderProvider.createVPC(1L, region);

        when(credentialsChecker.checkFindAll(accountId)).thenReturn(Single.error(NotFoundException::new));

        deploymentHandler.deployResources(reservation, accountId, dockerCredentials, List.of(vpc))
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void terminateResources(VertxTestContext testContext) {
        long accountId = 1L;
        Reservation reservation = TestReservationProvider.createReservation(1L);
        ResourceProvider rp = TestResourceProviderProvider.createResourceProvider(1L, "aws");
        JsonObject credentials = JsonObject.mapFrom(TestAccountProvider.createCredentials(1L, rp));
        JsonObject fr1 = JsonObject.mapFrom(TestFunctionProvider.createFunctionResource(1L));
        JsonObject fr2 = JsonObject.mapFrom(TestFunctionProvider.createFunctionResource(1L));
        JsonArray functionResources = new JsonArray(List.of(fr1, fr2));

        when(credentialsChecker.checkFindAll(accountId)).thenReturn(Single.just(new JsonArray(List.of(credentials))));
        when(functionResourceChecker.checkFindAllByReservationId(reservation.getReservationId()))
            .thenReturn(Single.just(functionResources));
        when(deploymentChecker.terminateResources(any())).thenReturn(Completable.complete());
        when(deploymentChecker.deleteTFDirs(reservation.getReservationId())).thenReturn(Completable.complete());

        deploymentHandler.terminateResources(reservation, accountId)
            .blockingSubscribe(() -> {
                },
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
        testContext.completeNow();
    }

    @Test
    void terminateResourcesTerminationFailed(VertxTestContext testContext) {
        long accountId = 1L;
        Reservation reservation = TestReservationProvider.createReservation(1L);
        ResourceProvider rp = TestResourceProviderProvider.createResourceProvider(1L, "aws");
        JsonObject credentials = JsonObject.mapFrom(TestAccountProvider.createCredentials(1L, rp));
        JsonObject fr1 = JsonObject.mapFrom(TestFunctionProvider.createFunctionResource(1L));
        JsonObject fr2 = JsonObject.mapFrom(TestFunctionProvider.createFunctionResource(1L));
        JsonArray functionResources = new JsonArray(List.of(fr1, fr2));

        when(credentialsChecker.checkFindAll(accountId)).thenReturn(Single.just(new JsonArray(List.of(credentials))));
        when(functionResourceChecker.checkFindAllByReservationId(reservation.getReservationId()))
            .thenReturn(Single.just(functionResources));
        when(deploymentChecker.terminateResources(any())).thenReturn(Completable.error(DeploymentTerminationFailedException::new));

        deploymentHandler.terminateResources(reservation, accountId)
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(DeploymentTerminationFailedException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void terminateResourcesFunctionResourcesNotFound(VertxTestContext testContext) {
        long accountId = 1L;
        Reservation reservation = TestReservationProvider.createReservation(1L);
        ResourceProvider rp = TestResourceProviderProvider.createResourceProvider(1L, "aws");
        JsonObject credentials = JsonObject.mapFrom(TestAccountProvider.createCredentials(1L, rp));

        when(credentialsChecker.checkFindAll(accountId)).thenReturn(Single.just(new JsonArray(List.of(credentials))));
        when(functionResourceChecker.checkFindAllByReservationId(reservation.getReservationId()))
            .thenReturn(Single.error(NotFoundException::new));

        deploymentHandler.terminateResources(reservation, accountId)
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void terminateResourcesCredentialsNotFound(VertxTestContext testContext) {
        long accountId = 1L;
        Reservation reservation = TestReservationProvider.createReservation(1L);

        when(credentialsChecker.checkFindAll(accountId)).thenReturn(Single.error(NotFoundException::new));

        deploymentHandler.terminateResources(reservation, accountId)
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }
}
