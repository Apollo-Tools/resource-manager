package at.uibk.dps.rm.handler.reservation;

import at.uibk.dps.rm.entity.deployment.ReservationStatusValue;
import at.uibk.dps.rm.entity.dto.ReserveResourcesRequest;
import at.uibk.dps.rm.entity.dto.credentials.DockerCredentials;
import at.uibk.dps.rm.entity.dto.reservation.FunctionResourceIds;
import at.uibk.dps.rm.entity.dto.reservation.ServiceResourceIds;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.exception.DeploymentTerminationFailedException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.exception.UnauthorizedException;
import at.uibk.dps.rm.handler.deployment.DeploymentHandler;
import at.uibk.dps.rm.testutil.RoutingContextMockHelper;
import at.uibk.dps.rm.testutil.objectprovider.*;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.ext.web.RoutingContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * Implements tests for the {@link ReservationHandler} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class ReservationHandlerTest {

    private  ReservationHandler reservationHandler;

    @Mock
    private ReservationChecker reservationChecker;

    @Mock
    private ResourceReservationChecker resourceReservationChecker;

    @Mock
    private FunctionReservationChecker functionReservationChecker;

    @Mock
    private ServiceReservationChecker serviceReservationChecker;

    @Mock
    private ResourceReservationStatusChecker statusChecker;

    @Mock
    private DeploymentHandler deploymentHandler;

    @Mock
    private ReservationErrorHandler reservationErrorHandler;

    @Mock
    private ReservationPreconditionHandler preconditionChecker;

    @Mock
    private RoutingContext rc;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        reservationHandler = new ReservationHandler(reservationChecker, resourceReservationChecker,
            functionReservationChecker, serviceReservationChecker, statusChecker, deploymentHandler,
            reservationErrorHandler, preconditionChecker);
    }

    @Test
    void getOneExists(VertxTestContext testContext) {
        long reservationId = 1L;
        Account account = TestAccountProvider.createAccount(1L);
        Reservation reservation = TestReservationProvider.createReservation(1L, true, account);
        JsonArray functionReservations = new JsonArray(TestReservationProvider
            .createFunctionReservationsJson(reservation));
        JsonArray serviceReservations = new JsonArray(TestReservationProvider
            .createServiceReservationsJson(reservation));

        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        when(rc.pathParam("id")).thenReturn(String.valueOf(reservationId));
        when(reservationChecker.checkFindOne(reservationId, account.getAccountId()))
            .thenReturn(Single.just(JsonObject.mapFrom(reservation)));
        when(functionReservationChecker.checkFindAllByReservationId(reservationId))
            .thenReturn(Single.just(functionReservations));
        when(serviceReservationChecker.checkFindAllByReservationId(reservationId))
            .thenReturn(Single.just(serviceReservations));

        reservationHandler.getOne(rc)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.getJsonArray("function_resources").size()).isEqualTo(3);
                    assertThat(result.getJsonArray("service_resources").size()).isEqualTo(3);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void getOneExistsResourceReservationEmpty(VertxTestContext testContext) {
        long reservationId = 1L;
        Account account = TestAccountProvider.createAccount(1L);
        Reservation reservation = TestReservationProvider.createReservation(1L, true, account);
        JsonArray functionReservations = new JsonArray(new ArrayList<JsonObject>());
        JsonArray serviceReservations = new JsonArray(new ArrayList<JsonObject>());

        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        when(rc.pathParam("id")).thenReturn(String.valueOf(reservationId));
        when(reservationChecker.checkFindOne(reservationId, account.getAccountId()))
            .thenReturn(Single.just(JsonObject.mapFrom(reservation)));
        when(functionReservationChecker.checkFindAllByReservationId(reservationId))
            .thenReturn(Single.just(functionReservations));
        when(serviceReservationChecker.checkFindAllByReservationId(reservationId))
            .thenReturn(Single.just(serviceReservations));


        reservationHandler.getOne(rc)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.getJsonArray("function_resources").size()).isEqualTo(0);
                    assertThat(result.getJsonArray("service_resources").size()).isEqualTo(0);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }



    @Test
    void getOneNotFound(VertxTestContext testContext) {
        long reservationId = 1L;
        Account account = TestAccountProvider.createAccount(1L);

        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        when(rc.pathParam("id")).thenReturn(String.valueOf(reservationId));
        when(reservationChecker.checkFindOne(reservationId, account.getAccountId()))
            .thenReturn(Single.error(NotFoundException::new));

        reservationHandler.getOne(rc)
            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void getAllValid(VertxTestContext testContext) {
        Account account = TestAccountProvider.createAccount(1L);
        Reservation r1 = TestReservationProvider.createReservation(1L, true, account);
        Reservation r2 = TestReservationProvider.createReservation(2L, true, account);
        Reservation r3 = TestReservationProvider.createReservation(3L, true, account);
        ResourceReservationStatus rrsNew = TestReservationProvider.createResourceReservationStatusNew();
        ResourceReservationStatus rrsError = TestReservationProvider.createResourceReservationStatusError();
        ResourceReservationStatus rrsDeployed = TestReservationProvider.createResourceReservationStatusDeployed();
        Resource resource1 = TestResourceProvider.createResource(1L);
        Resource resource2 = TestResourceProvider.createResource(2L);
        ResourceReservation rr1 = TestReservationProvider.createResourceReservation(1L, r1, resource1, rrsNew);
        ResourceReservation rr2 = TestReservationProvider.createResourceReservation(2L, r1, resource2, rrsNew);
        Resource resource3 = TestResourceProvider.createResource(3L);
        ResourceReservation rr3 = TestReservationProvider.createResourceReservation(3L, r2, resource3, rrsError);
        Resource resource4 = TestResourceProvider.createResource(4L);
        ResourceReservation rr4 = TestReservationProvider.createResourceReservation(4L, r3, resource4, rrsDeployed);
        JsonArray reservations = new JsonArray(List.of(JsonObject.mapFrom(r1),
            JsonObject.mapFrom(r2), JsonObject.mapFrom(r3)));
        JsonArray rr12Json = new JsonArray(List.of(JsonObject.mapFrom(rr1), JsonObject.mapFrom(rr2)));
        JsonArray rr3Json = new JsonArray(List.of(JsonObject.mapFrom(rr3)));
        JsonArray rr4Json = new JsonArray(List.of(JsonObject.mapFrom(rr4)));

        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        when(reservationChecker.checkFindAll(account.getAccountId())).thenReturn(Single.just(reservations));
        when(resourceReservationChecker.checkFindAllByReservationId(r1.getReservationId()))
            .thenReturn(Single.just(rr12Json));
        when(resourceReservationChecker.checkFindAllByReservationId(r2.getReservationId()))
            .thenReturn(Single.just(rr3Json));
        when(resourceReservationChecker.checkFindAllByReservationId(r3.getReservationId()))
            .thenReturn(Single.just(rr4Json));
        when(resourceReservationChecker.checkCrucialResourceReservationStatus(rr12Json))
            .thenReturn(ReservationStatusValue.NEW);
        when(resourceReservationChecker.checkCrucialResourceReservationStatus(rr3Json))
            .thenReturn(ReservationStatusValue.TERMINATED);
        when(resourceReservationChecker.checkCrucialResourceReservationStatus(rr4Json))
            .thenReturn(ReservationStatusValue.ERROR);

        reservationHandler.getAll(rc)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.size()).isEqualTo(3);
                    assertThat(result.getJsonObject(0).getLong("reservation_id")).isEqualTo(1L);
                    assertThat(result.getJsonObject(0).getString("status_value")).isEqualTo("NEW");
                    assertThat(result.getJsonObject(1).getLong("reservation_id")).isEqualTo(2L);
                    assertThat(result.getJsonObject(1).getString("status_value")).isEqualTo("TERMINATED");
                    assertThat(result.getJsonObject(2).getLong("reservation_id")).isEqualTo(3L);
                    assertThat(result.getJsonObject(2).getString("status_value")).isEqualTo("ERROR");
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void getAllEmptyValid(VertxTestContext testContext) {
        Account account = TestAccountProvider.createAccount(1L);
        JsonArray reservations = new JsonArray(List.of());

        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        when(reservationChecker.checkFindAll(account.getAccountId())).thenReturn(Single.just(reservations));

        reservationHandler.getAll(rc)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.size()).isEqualTo(0);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void getAllNotFound(VertxTestContext testContext) {
        Account account = TestAccountProvider.createAccount(1L);

        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        when(reservationChecker.checkFindAll(account.getAccountId())).thenReturn(Single.error(NotFoundException::new));

        reservationHandler.getAll(rc)
            .subscribe(result -> testContext.verify(() -> fail("method has thrown exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void postOneValid(VertxTestContext testContext) {
        ResourceProvider aws = TestResourceProviderProvider.createResourceProvider(1L, "aws");
        Region reg1 = TestResourceProviderProvider.createRegion(1L, "us-east-1", aws);
        Region reg2 = TestResourceProviderProvider.createRegion(2L, "us-west-1", aws);

        Resource r1 = TestResourceProvider.createResourceFaaS(1L, reg1, 512.0, 200.0);
        Resource r2 = TestResourceProvider.createResourceVM(2L, reg2, "t2.micro");
        Resource r3 = TestResourceProvider.createResourceEdge(3L, "http://localhost:8080",
            "user", "pw");
        Resource r4 = TestResourceProvider.createResourceContainer(4L, "https://localhost");
        JsonArray resources = new JsonArray(List.of(JsonObject.mapFrom(r1), JsonObject.mapFrom(r2),
            JsonObject.mapFrom(r3), JsonObject.mapFrom(r4)));
        List<FunctionResourceIds> fids = TestFunctionProvider.createFunctionResourceIdsList(r1.getResourceId(),
            r2.getResourceId(), r3.getResourceId());
        List<ServiceResourceIds> sids = TestServiceProvider.createServiceResourceIdsList(r4.getResourceId());
        DockerCredentials dockerCredentials = TestDTOProvider.createDockerCredentials();
        String kubeconfig = TestDTOProvider.createKubeConfigValue();
        ReserveResourcesRequest request = TestRequestProvider.createReserveResourcesRequest(fids, sids,
            dockerCredentials);
        JsonObject requestBody = JsonObject.mapFrom(request);
        Account account = TestAccountProvider.createAccount(1L);
        Reservation reservation = TestReservationProvider.createReservation(1L, true, account);
        JsonObject reservationJson = JsonObject.mapFrom(reservation);
        ResourceReservationStatus statusNew = TestReservationProvider.createResourceReservationStatusNew();


        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        RoutingContextMockHelper.mockBody(rc, requestBody);
        when(preconditionChecker.checkReservationIsValid(request, account.getAccountId(), new ArrayList<>()))
            .thenReturn(Single.just(resources));
        when(reservationChecker.submitCreateReservation(account.getAccountId()))
            .thenReturn(Single.just(reservationJson));
        when(statusChecker.checkFindOneByStatusValue(ReservationStatusValue.NEW.name()))
            .thenReturn(Single.just(JsonObject.mapFrom(statusNew)));
        when(functionReservationChecker.submitCreateAll(any())).thenReturn(Completable.complete());
        when(serviceReservationChecker.submitCreateAll(any())).thenReturn(Completable.complete());
        when(deploymentHandler.deployResources(reservation, account.getAccountId(), dockerCredentials, kubeconfig,
            new ArrayList<>()))
            .thenReturn(Completable.complete());
        when(resourceReservationChecker.submitUpdateStatus(reservation.getReservationId(), ReservationStatusValue.DEPLOYED))
            .thenReturn(Completable.complete());

        reservationHandler.postOne(rc)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.getLong("reservation_id")).isEqualTo(1L);
                    assertThat(result.getBoolean("is_active")).isTrue();
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void postOneErrorInDeployment(VertxTestContext testContext) {
        ResourceProvider aws = TestResourceProviderProvider.createResourceProvider(1L, "aws");
        Region reg1 = TestResourceProviderProvider.createRegion(1L, "us-east-1", aws);
        Region reg2 = TestResourceProviderProvider.createRegion(2L, "us-west-1", aws);

        Resource r1 = TestResourceProvider.createResourceFaaS(1L, reg1, 512.0, 200.0);
        Resource r2 = TestResourceProvider.createResourceVM(2L, reg2, "t2.micro");
        Resource r3 = TestResourceProvider.createResourceEdge(3L, "http://localhost:8080",
            "user", "pw");
        Resource r4 = TestResourceProvider.createResourceContainer(4L, "https://localhost");
        JsonArray resources = new JsonArray(List.of(JsonObject.mapFrom(r1), JsonObject.mapFrom(r2),
            JsonObject.mapFrom(r3), JsonObject.mapFrom(r4)));
        List<FunctionResourceIds> fids = TestFunctionProvider.createFunctionResourceIdsList(r1.getResourceId(),
            r2.getResourceId(), r3.getResourceId());
        List<ServiceResourceIds> sids = TestServiceProvider.createServiceResourceIdsList(r4.getResourceId());
        DockerCredentials dockerCredentials = TestDTOProvider.createDockerCredentials();
        String kubeconfig = TestDTOProvider.createKubeConfigValue();
        ReserveResourcesRequest request = TestRequestProvider.createReserveResourcesRequest(fids, sids,
            dockerCredentials);
        JsonObject requestBody = JsonObject.mapFrom(request);
        Account account = TestAccountProvider.createAccount(1L);
        Reservation reservation = TestReservationProvider.createReservation(1L, true, account);
        JsonObject reservationJson = JsonObject.mapFrom(reservation);
        ResourceReservationStatus statusNew = TestReservationProvider.createResourceReservationStatusNew();


        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        RoutingContextMockHelper.mockBody(rc, requestBody);
        when(preconditionChecker.checkReservationIsValid(request, account.getAccountId(), new ArrayList<>()))
            .thenReturn(Single.just(resources));
        when(reservationChecker.submitCreateReservation(account.getAccountId()))
            .thenReturn(Single.just(reservationJson));
        when(statusChecker.checkFindOneByStatusValue(ReservationStatusValue.NEW.name()))
            .thenReturn(Single.just(JsonObject.mapFrom(statusNew)));
        when(functionReservationChecker.submitCreateAll(any())).thenReturn(Completable.complete());
        when(serviceReservationChecker.submitCreateAll(any())).thenReturn(Completable.complete());
        when(deploymentHandler.deployResources(reservation, account.getAccountId(), dockerCredentials, kubeconfig,
            new ArrayList<>()))
            .thenReturn(Completable.error(DeploymentTerminationFailedException::new));
        when(reservationErrorHandler.onDeploymentError(eq(account.getAccountId()), eq(reservation),
            any())).thenReturn(Completable.complete());

        reservationHandler.postOne(rc)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.getLong("reservation_id")).isEqualTo(1L);
                    assertThat(result.getBoolean("is_active")).isTrue();
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void postOnePreconditionsNotMet(VertxTestContext testContext) {
        Account account = TestAccountProvider.createAccount(1L);
        FunctionResource functionResource = TestFunctionProvider.createFunctionResource(1L);
        FunctionResourceIds ids = TestFunctionProvider.createFunctionResourceIds(1L,
            functionResource.getResource().getResourceId());
        List<FunctionResourceIds> fids = List.of(ids);
        List<ServiceResourceIds> sids = new ArrayList<>();
        ReserveResourcesRequest request = TestRequestProvider.createReserveResourcesRequest(fids, sids);
        JsonObject requestBody = JsonObject.mapFrom(request);

        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        RoutingContextMockHelper.mockBody(rc, requestBody);
        when(preconditionChecker.checkReservationIsValid(request, account.getAccountId(), new ArrayList<>()))
            .thenReturn(Single.error(UnauthorizedException::new));

        reservationHandler.postOne(rc)
            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(UnauthorizedException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void updateOneValid(VertxTestContext testContext) {
        long reservationId = 1L;
        Account account = TestAccountProvider.createAccount(1L);
        Reservation reservation = TestReservationProvider.createReservation(1L, true, account);
        JsonObject reservationJson = JsonObject.mapFrom(reservation);

        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        when(rc.pathParam("id")).thenReturn(String.valueOf(reservationId));
        when(reservationChecker.checkFindOne(reservationId, account.getAccountId()))
            .thenReturn(Single.just(reservationJson));
        when(resourceReservationChecker.submitUpdateStatus(reservationId, ReservationStatusValue.TERMINATING))
            .thenReturn(Completable.complete());
        when(deploymentHandler.terminateResources(reservation, account.getAccountId()))
            .thenReturn(Completable.error(DeploymentTerminationFailedException::new));
        when(reservationErrorHandler.onTerminationError(eq(reservation), any())).thenReturn(Completable.complete());

        reservationHandler.updateOne(rc)
            .blockingSubscribe(() -> {},
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );

        testContext.completeNow();
    }

    @Test
    void updateOneTerminationFailed(VertxTestContext testContext) {
        long reservationId = 1L;
        Account account = TestAccountProvider.createAccount(1L);
        Reservation reservation = TestReservationProvider.createReservation(1L, true, account);
        JsonObject reservationJson = JsonObject.mapFrom(reservation);

        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        when(rc.pathParam("id")).thenReturn(String.valueOf(reservationId));
        when(reservationChecker.checkFindOne(reservationId, account.getAccountId()))
            .thenReturn(Single.just(reservationJson));
        when(resourceReservationChecker.submitUpdateStatus(reservationId, ReservationStatusValue.TERMINATING))
            .thenReturn(Completable.complete());
        when(deploymentHandler.terminateResources(reservation, account.getAccountId()))
            .thenReturn(Completable.complete());
        when(resourceReservationChecker.submitUpdateStatus(reservationId, ReservationStatusValue.TERMINATED))
            .thenReturn(Completable.complete());

        reservationHandler.updateOne(rc)
            .blockingSubscribe(() -> {},
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );

        testContext.completeNow();
    }

    @Test
    void updateOneNotFound(VertxTestContext testContext) {
        long reservationId = 1L;
        Account account = TestAccountProvider.createAccount(1L);

        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        when(rc.pathParam("id")).thenReturn(String.valueOf(reservationId));
        when(reservationChecker.checkFindOne(reservationId, account.getAccountId()))
            .thenReturn(Single.error(NotFoundException::new));

        reservationHandler.updateOne(rc)
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }
}
