package at.uibk.dps.rm.handler.deployment;

import at.uibk.dps.rm.entity.deployment.ProcessOutput;
import at.uibk.dps.rm.entity.dto.credentials.DockerCredentials;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.exception.DeploymentTerminationFailedException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.handler.account.CredentialsChecker;
import at.uibk.dps.rm.handler.reservation.FunctionReservationChecker;
import at.uibk.dps.rm.handler.reservation.ResourceReservationChecker;
import at.uibk.dps.rm.handler.reservation.ServiceReservationChecker;
import at.uibk.dps.rm.testutil.objectprovider.*;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
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
    private FunctionReservationChecker functionReservationChecker;

    @Mock
    private ServiceReservationChecker serviceReservationChecker;

    @Mock
    private ResourceReservationChecker resourceReservationChecker;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        deploymentHandler = new DeploymentHandler(deploymentChecker, credentialsChecker, functionReservationChecker,
            serviceReservationChecker, resourceReservationChecker);
    }

    @Test
    void deployResources(VertxTestContext testContext) {
        long accountId = 1L;
        Reservation reservation = TestReservationProvider.createReservation(1L);
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
        when(functionReservationChecker.checkFindAllByReservationId(1L))
            .thenReturn(Single.just(new JsonArray(List.of(fr1))));
        when(serviceReservationChecker.checkFindAllByReservationId(1L))
            .thenReturn(Single.just(new JsonArray(List.of(sr1, sr2))));
        when(deploymentChecker.deployResources(any())).thenReturn(Single.just(new ProcessOutput()));
        when(resourceReservationChecker.storeOutputToResourceReservations(any(), any())).thenReturn(Completable.complete());

        deploymentHandler.deployResources(reservation, accountId, dockerCredentials, kubeConfig, List.of(vpc))
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
        String kubeConfig = TestDTOProvider.createKubeConfigValue();
        ResourceProvider rp = TestResourceProviderProvider.createResourceProvider(1L, "aws");
        Region region = TestResourceProviderProvider.createRegion(1L, "us-east-1", rp);
        VPC vpc = TestResourceProviderProvider.createVPC(1L, region);
        JsonObject credentials = JsonObject.mapFrom(TestAccountProvider.createCredentials(1L, rp));
        JsonObject sr1 = JsonObject.mapFrom(TestReservationProvider
            .createServiceReservation(2L, reservation));
        JsonObject sr2 = JsonObject.mapFrom(TestReservationProvider
            .createServiceReservation(3L, reservation));
        JsonObject fr1 = JsonObject.mapFrom(TestReservationProvider.createFunctionReservation(3L, reservation));

        when(credentialsChecker.checkFindAll(accountId)).thenReturn(Single.just(new JsonArray(List.of(credentials))));
        when(functionReservationChecker.checkFindAllByReservationId(1L))
            .thenReturn(Single.just(new JsonArray(List.of(fr1))));
        when(serviceReservationChecker.checkFindAllByReservationId(1L))
            .thenReturn(Single.just(new JsonArray(List.of(sr1, sr2))));
        when(deploymentChecker.deployResources(any())).thenReturn(Single.error(DeploymentTerminationFailedException::new));

        deploymentHandler.deployResources(reservation, accountId, dockerCredentials, kubeConfig, List.of(vpc))
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(DeploymentTerminationFailedException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void deployResourcesStoreOutputFunctionReservationsNotFound(VertxTestContext testContext) {
        long accountId = 1L;
        Reservation reservation = TestReservationProvider.createReservation(1L);
        DockerCredentials dockerCredentials = TestDTOProvider.createDockerCredentials();
        String kubeConfig = TestDTOProvider.createKubeConfigValue();
        ResourceProvider rp = TestResourceProviderProvider.createResourceProvider(1L, "aws");
        Region region = TestResourceProviderProvider.createRegion(1L, "us-east-1", rp);
        VPC vpc = TestResourceProviderProvider.createVPC(1L, region);
        JsonObject credentials = JsonObject.mapFrom(TestAccountProvider.createCredentials(1L, rp));

        when(credentialsChecker.checkFindAll(accountId)).thenReturn(Single.just(new JsonArray(List.of(credentials))));
        when(functionReservationChecker.checkFindAllByReservationId(1L))
            .thenReturn(Single.error(NotFoundException::new));

        deploymentHandler.deployResources(reservation, accountId, dockerCredentials, kubeConfig, List.of(vpc))
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void deployResourcesStoreOutputServiceReservationsNotFound(VertxTestContext testContext) {
        long accountId = 1L;
        Reservation reservation = TestReservationProvider.createReservation(1L);
        DockerCredentials dockerCredentials = TestDTOProvider.createDockerCredentials();
        String kubeConfig = TestDTOProvider.createKubeConfigValue();
        ResourceProvider rp = TestResourceProviderProvider.createResourceProvider(1L, "aws");
        Region region = TestResourceProviderProvider.createRegion(1L, "us-east-1", rp);
        VPC vpc = TestResourceProviderProvider.createVPC(1L, region);
        JsonObject credentials = JsonObject.mapFrom(TestAccountProvider.createCredentials(1L, rp));
        JsonObject fr1 = JsonObject.mapFrom(TestReservationProvider.createFunctionReservation(3L, reservation));

        when(credentialsChecker.checkFindAll(accountId)).thenReturn(Single.just(new JsonArray(List.of(credentials))));
        when(serviceReservationChecker.checkFindAllByReservationId(1L))
            .thenReturn(Single.error(NotFoundException::new));
        when(functionReservationChecker.checkFindAllByReservationId(1L))
            .thenReturn(Single.just(new JsonArray(List.of(fr1))));

        deploymentHandler.deployResources(reservation, accountId, dockerCredentials, kubeConfig, List.of(vpc))
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
        String kubeConfig = TestDTOProvider.createKubeConfigValue();
        ResourceProvider rp = TestResourceProviderProvider.createResourceProvider(1L, "aws");
        Region region = TestResourceProviderProvider.createRegion(1L, "us-east-1", rp);
        VPC vpc = TestResourceProviderProvider.createVPC(1L, region);

        when(credentialsChecker.checkFindAll(accountId)).thenReturn(Single.error(NotFoundException::new));

        deploymentHandler.deployResources(reservation, accountId, dockerCredentials, kubeConfig, List.of(vpc))
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
        JsonObject sr1 = JsonObject.mapFrom(TestReservationProvider
            .createServiceReservation(2L, reservation));
        JsonObject sr2 = JsonObject.mapFrom(TestReservationProvider
            .createServiceReservation(3L, reservation));
        JsonObject fr1 = JsonObject.mapFrom(TestReservationProvider.createFunctionReservation(3L, reservation));

        when(credentialsChecker.checkFindAll(accountId)).thenReturn(Single.just(new JsonArray(List.of(credentials))));
        when(functionReservationChecker.checkFindAllByReservationId(1L))
            .thenReturn(Single.just(new JsonArray(List.of(fr1))));
        when(serviceReservationChecker.checkFindAllByReservationId(1L))
            .thenReturn(Single.just(new JsonArray(List.of(sr1, sr2))));
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
        JsonObject sr1 = JsonObject.mapFrom(TestReservationProvider
            .createServiceReservation(2L, reservation));
        JsonObject sr2 = JsonObject.mapFrom(TestReservationProvider
            .createServiceReservation(3L, reservation));
        JsonObject fr1 = JsonObject.mapFrom(TestReservationProvider.createFunctionReservation(3L, reservation));

        when(credentialsChecker.checkFindAll(accountId)).thenReturn(Single.just(new JsonArray(List.of(credentials))));
        when(functionReservationChecker.checkFindAllByReservationId(1L))
            .thenReturn(Single.just(new JsonArray(List.of(fr1))));
        when(serviceReservationChecker.checkFindAllByReservationId(1L))
            .thenReturn(Single.just(new JsonArray(List.of(sr1, sr2))));
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
    void terminateResourcesFunctionReservationsNotFound(VertxTestContext testContext) {
        long accountId = 1L;
        Reservation reservation = TestReservationProvider.createReservation(1L);
        ResourceProvider rp = TestResourceProviderProvider.createResourceProvider(1L, "aws");
        JsonObject credentials = JsonObject.mapFrom(TestAccountProvider.createCredentials(1L, rp));

        when(credentialsChecker.checkFindAll(accountId)).thenReturn(Single.just(new JsonArray(List.of(credentials))));
        when(functionReservationChecker.checkFindAllByReservationId(1L))
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
    void terminateResourcesServiceReservationsNotFound(VertxTestContext testContext) {
        long accountId = 1L;
        Reservation reservation = TestReservationProvider.createReservation(1L);
        ResourceProvider rp = TestResourceProviderProvider.createResourceProvider(1L, "aws");
        JsonObject credentials = JsonObject.mapFrom(TestAccountProvider.createCredentials(1L, rp));
        JsonObject fr1 = JsonObject.mapFrom(TestReservationProvider.createFunctionReservation(3L, reservation));

        when(credentialsChecker.checkFindAll(accountId)).thenReturn(Single.just(new JsonArray(List.of(credentials))));
        when(serviceReservationChecker.checkFindAllByReservationId(1L))
            .thenReturn(Single.error(NotFoundException::new));
        when(functionReservationChecker.checkFindAllByReservationId(1L))
            .thenReturn(Single.just(new JsonArray(List.of(fr1))));

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
