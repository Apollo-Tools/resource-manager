package at.uibk.dps.rm.handler.reservation;

import at.uibk.dps.rm.entity.deployment.ReservationStatusValue;
import at.uibk.dps.rm.entity.dto.ReserveResourcesRequest;
import at.uibk.dps.rm.entity.dto.credentials.DockerCredentials;
import at.uibk.dps.rm.entity.dto.reservation.FunctionResourceIds;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.exception.UnauthorizedException;
import at.uibk.dps.rm.handler.deployment.DeploymentHandler;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import at.uibk.dps.rm.service.rxjava3.database.account.CredentialsService;
import at.uibk.dps.rm.service.rxjava3.database.function.FunctionResourceService;
import at.uibk.dps.rm.service.rxjava3.database.metric.ResourceTypeMetricService;
import at.uibk.dps.rm.service.rxjava3.database.reservation.ReservationService;
import at.uibk.dps.rm.service.rxjava3.database.reservation.ResourceReservationService;
import at.uibk.dps.rm.service.rxjava3.database.reservation.ResourceReservationStatusService;
import at.uibk.dps.rm.service.rxjava3.database.resourceprovider.VPCService;
import at.uibk.dps.rm.testutil.RoutingContextMockHelper;
import at.uibk.dps.rm.testutil.SingleHelper;
import at.uibk.dps.rm.testutil.TestObjectProvider;
import at.uibk.dps.rm.util.JsonMapperConfig;
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
import static org.mockito.AdditionalMatchers.or;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class ReservationHandlerTest {

    private  ReservationHandler reservationHandler;

    @Mock
    private ReservationService reservationService;

    @Mock
    private FunctionResourceService functionResourceService;

    @Mock
    private ResourceReservationService resourceReservationService;

    @Mock
    private CredentialsService credentialsService;

    @Mock
    private ResourceTypeMetricService resourceTypeMetricService;

    @Mock
    private ResourceReservationStatusService resourceReservationStatusService;

    @Mock
    private VPCService vpcService;

    @Mock
    private RoutingContext rc;

    @Mock
    private ServiceProxyProvider serviceProxyProvider;

    @Mock
    private DeploymentHandler deploymentHandler;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        when(serviceProxyProvider.getReservationService()).thenReturn(reservationService);
        when(serviceProxyProvider.getResourceReservationService()).thenReturn(resourceReservationService);
        when(serviceProxyProvider.getFunctionResourceService()).thenReturn(functionResourceService);
        when(serviceProxyProvider.getCredentialsService()).thenReturn(credentialsService);
        when(serviceProxyProvider.getResourceTypeMetricService()).thenReturn(resourceTypeMetricService);
        when(serviceProxyProvider.getVpcService()).thenReturn(vpcService);
        when(serviceProxyProvider.getResourceReservationStatusService()).thenReturn(resourceReservationStatusService);
        reservationHandler = new ReservationHandler(serviceProxyProvider, deploymentHandler);
    }

    @Test
    void getOneExistsAndIsActive(VertxTestContext testContext) {
        long reservationId = 1L;
        Account account = TestObjectProvider.createAccount(1L);
        Reservation reservation = TestObjectProvider.createReservation(1L, true, account);
        JsonArray resourceReservations = new JsonArray(TestObjectProvider.createResourceReservationsJson(reservation));

        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        when(rc.pathParam("id")).thenReturn(String.valueOf(reservationId));
        when(reservationService.findOneByIdAndAccountId(reservationId, account.getAccountId()))
            .thenReturn(Single.just(JsonObject.mapFrom(reservation)));
        when(resourceReservationService.findAllByReservationId(reservationId))
            .thenReturn(Single.just(resourceReservations));

        reservationHandler.getOne(rc)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.getJsonArray("resource_reservations").size()).isEqualTo(3);
                    JsonObject functionResourceJson1 = result.getJsonArray("resource_reservations").getJsonObject(0)
                        .getJsonObject("function_resource");
                    JsonObject functionResourceJson2 = result.getJsonArray("resource_reservations").getJsonObject(1)
                        .getJsonObject("function_resource");
                    JsonObject functionResourceJson3 = result.getJsonArray("resource_reservations").getJsonObject(2)
                        .getJsonObject("function_resource");
                    assertThat(functionResourceJson1.getLong("function_resource_id")).isEqualTo(1L);
                    assertThat(functionResourceJson2.getLong("function_resource_id")).isEqualTo(2L);
                    assertThat(functionResourceJson3.getLong("function_resource_id")).isEqualTo(3L);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method did throw exception"))
            );
    }

    @Test
    void getOneExistsResourceReservationEmpty(VertxTestContext testContext) {
        long reservationId = 1L;
        Account account = TestObjectProvider.createAccount(1L);
        Reservation reservation = TestObjectProvider.createReservation(1L, true, account);
        JsonArray resourceReservations = new JsonArray(new ArrayList<JsonObject>());

        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        when(rc.pathParam("id")).thenReturn(String.valueOf(reservationId));
        when(reservationService.findOneByIdAndAccountId(reservationId, account.getAccountId()))
            .thenReturn(Single.just(JsonObject.mapFrom(reservation)));
        when(resourceReservationService.findAllByReservationId(reservationId))
            .thenReturn(Single.just(resourceReservations));

        reservationHandler.getOne(rc)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.getJsonArray("resource_reservations").size()).isEqualTo(0);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method did throw exception"))
            );
    }

    @Test
    void getOneExistsNotActive(VertxTestContext testContext) {
        long reservationId = 1L;
        Account account = TestObjectProvider.createAccount(1L);
        Reservation reservation = TestObjectProvider.createReservation(1L, false, account);

        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        when(rc.pathParam("id")).thenReturn(String.valueOf(reservationId));
        when(reservationService.findOneByIdAndAccountId(reservationId, account.getAccountId()))
            .thenReturn(Single.just(JsonObject.mapFrom(reservation)));

        reservationHandler.getOne(rc)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.containsKey("resource_reservations")).isFalse();
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method did throw exception"))
            );
    }



    @Test
    void getOneNotFound(VertxTestContext testContext) {
        long reservationId = 1L;
        Single<JsonObject> handler = new SingleHelper<JsonObject>().getEmptySingle();
        Account account = TestObjectProvider.createAccount(1L);

        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        when(rc.pathParam("id")).thenReturn(String.valueOf(reservationId));
        when(reservationService.findOneByIdAndAccountId(reservationId, account.getAccountId())).thenReturn(handler);

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
        Account account = TestObjectProvider.createAccount(1L);
        Reservation r1 = TestObjectProvider.createReservation(1L, true, account);
        Reservation r2 = TestObjectProvider.createReservation(2L, true, account);
        Reservation r3 = TestObjectProvider.createReservation(3L, true, account);
        ResourceReservationStatus rrsNew = TestObjectProvider.createResourceReservationStatusNew();
        ResourceReservationStatus rrsError = TestObjectProvider.createResourceReservationStatusError();
        ResourceReservationStatus rrsDeployed = TestObjectProvider.createResourceReservationStatusDeployed();
        FunctionResource fr1 = TestObjectProvider.createFunctionResource(1L);
        FunctionResource fr2 = TestObjectProvider.createFunctionResource(2L);
        ResourceReservation rr1 = TestObjectProvider.createResourceReservation(1L, fr1, r1, rrsNew);
        ResourceReservation rr2 = TestObjectProvider.createResourceReservation(2L, fr2, r1, rrsNew);
        FunctionResource fr3 = TestObjectProvider.createFunctionResource(3L);
        ResourceReservation rr3 = TestObjectProvider.createResourceReservation(3L, fr3, r2, rrsError);
        FunctionResource fr4 = TestObjectProvider.createFunctionResource(4L);
        ResourceReservation rr4 = TestObjectProvider.createResourceReservation(4L, fr4, r3, rrsDeployed);
        JsonArray reservations = new JsonArray(List.of(JsonObject.mapFrom(r1),
            JsonObject.mapFrom(r2), JsonObject.mapFrom(r3)));

        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        when(reservationService.findAllByAccountId(account.getAccountId())).thenReturn(Single.just(reservations));
        when(resourceReservationService.findAllByReservationId(r1.getReservationId()))
            .thenReturn(Single.just(new JsonArray(List.of(JsonObject.mapFrom(rr1),JsonObject.mapFrom(rr2)))));
        when(resourceReservationService.findAllByReservationId(r2.getReservationId()))
            .thenReturn(Single.just(new JsonArray(List.of(JsonObject.mapFrom(rr3)))));
        when(resourceReservationService.findAllByReservationId(r3.getReservationId()))
            .thenReturn(Single.just(new JsonArray(List.of(JsonObject.mapFrom(rr4)))));

        reservationHandler.getAll(rc)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.size()).isEqualTo(3);
                    assertThat(result.getJsonObject(0).getLong("reservation_id")).isEqualTo(1L);
                    assertThat(result.getJsonObject(0).getString("status_value")).isEqualTo("NEW");
                    assertThat(result.getJsonObject(1).getLong("reservation_id")).isEqualTo(2L);
                    assertThat(result.getJsonObject(1).getString("status_value")).isEqualTo("ERROR");
                    assertThat(result.getJsonObject(2).getLong("reservation_id")).isEqualTo(3L);
                    assertThat(result.getJsonObject(2).getString("status_value")).isEqualTo("DEPLOYED");
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method did throw exception"))
            );
    }

    @Test
    void getAllEmptyValid(VertxTestContext testContext) {
        Account account = TestObjectProvider.createAccount(1L);
        JsonArray reservations = new JsonArray(List.of());

        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        when(reservationService.findAllByAccountId(account.getAccountId())).thenReturn(Single.just(reservations));

        reservationHandler.getAll(rc)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.size()).isEqualTo(0);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method did throw exception"))
            );
    }

    @Test
    void getAllNotFound(VertxTestContext testContext) {
        Account account = TestObjectProvider.createAccount(1L);
        Single<JsonArray> handler = new SingleHelper<JsonArray>().getEmptySingle();

        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        when(reservationService.findAllByAccountId(account.getAccountId())).thenReturn(handler);

        reservationHandler.getAll(rc)
            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void postOneValid(VertxTestContext testContext) {
        ResourceProvider aws = TestObjectProvider.createResourceProvider(1L, "aws");
        ResourceProvider edge = TestObjectProvider.createResourceProvider(2L, "edge");
        Region reg1 = TestObjectProvider.createRegion(1L, "us-east-1", aws);
        Region reg2 = TestObjectProvider.createRegion(2L, "us-west-1", aws);
        Region reg3 = TestObjectProvider.createRegion(3L, "edge", edge);
        ResourceType rtFaas = TestObjectProvider.createResourceType(1L, "faas");
        ResourceType rtVm = TestObjectProvider.createResourceType(2L, "vm");
        ResourceType rtEdge = TestObjectProvider.createResourceType(3L, "edge");

        Resource r1 = TestObjectProvider.createResource(1L, rtFaas, reg1, false);
        Resource r2 = TestObjectProvider.createResource(2L, rtVm, reg2, false);
        Resource r3 = TestObjectProvider.createResource(3L, rtEdge, reg3, true);
        Function f1 = TestObjectProvider.createFunction(1L);
        Function f2 = TestObjectProvider.createFunction(2L);
        FunctionResource fr1 = TestObjectProvider.createFunctionResource(1L, f1, r1);
        FunctionResource fr2 = TestObjectProvider.createFunctionResource(2L, f1, r2);
        FunctionResource fr3 = TestObjectProvider.createFunctionResource(3L, f2, r2);
        FunctionResource fr4 = TestObjectProvider.createFunctionResource(4L, f2, r3);
        FunctionResourceIds ids1 = TestObjectProvider.createFunctionResourceIds(fr1);
        FunctionResourceIds ids2 = TestObjectProvider.createFunctionResourceIds(fr2);
        FunctionResourceIds ids3 = TestObjectProvider.createFunctionResourceIds(fr3);
        FunctionResourceIds ids4 = TestObjectProvider.createFunctionResourceIds(fr4);
        List<FunctionResourceIds> ids = List.of(ids1, ids2, ids3, ids4);
        DockerCredentials dockerCredentials = TestObjectProvider.createDockerCredentials();
        ReserveResourcesRequest request = TestObjectProvider.createReserveResourcesRequest(ids, dockerCredentials);
        JsonObject requestBody = JsonObject.mapFrom(request);
        Account account = TestObjectProvider.createAccount(1L);
        Reservation reservation = TestObjectProvider.createReservation(1L, true, account);
        JsonObject reservationJson = JsonObject.mapFrom(reservation);
        VPC vpc1 = TestObjectProvider.createVPC(1L, reg1, account);
        VPC vpc2 = TestObjectProvider.createVPC(2L, reg2, account);
        ResourceReservationStatus statusNew = TestObjectProvider.createResourceReservationStatusNew();


        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        RoutingContextMockHelper.mockBody(rc, requestBody);
        when(functionResourceService.findOneByFunctionAndResource(ids1.getFunctionId(), ids1.getResourceId()))
            .thenReturn(Single.just(JsonObject.mapFrom(fr1)));
        when(functionResourceService.findOneByFunctionAndResource(ids2.getFunctionId(), ids2.getResourceId()))
            .thenReturn(Single.just(JsonObject.mapFrom(fr2)));
        when(functionResourceService.findOneByFunctionAndResource(ids3.getFunctionId(), ids3.getResourceId()))
            .thenReturn(Single.just(JsonObject.mapFrom(fr3)));
        when(functionResourceService.findOneByFunctionAndResource(ids4.getFunctionId(), ids4.getResourceId()))
            .thenReturn(Single.just(JsonObject.mapFrom(fr4)));
        when(credentialsService.existsOnyByAccountIdAndProviderId(account.getAccountId(), aws.getProviderId()))
            .thenReturn(Single.just(true));
        when(resourceTypeMetricService.missingRequiredResourceTypeMetricsByResourceId(or(eq(ids1.getResourceId()),
                or(eq(ids2.getResourceId()), or(eq(ids3.getResourceId()), eq(ids4.getResourceId()))))))
            .thenReturn(Single.just(false));
        when(vpcService.findOneByRegionIdAndAccountId(r1.getRegion().getRegionId(), account.getAccountId()))
            .thenReturn(Single.just(JsonObject.mapFrom(vpc1)));
        when(vpcService.findOneByRegionIdAndAccountId(r2.getRegion().getRegionId(), account.getAccountId()))
            .thenReturn(Single.just(JsonObject.mapFrom(vpc2)));
        when(reservationService.save(any())).thenReturn(Single.just(reservationJson));
        when(resourceReservationStatusService.findOneByStatusValue(ReservationStatusValue.NEW.name()))
            .thenReturn(Single.just(JsonObject.mapFrom(statusNew)));
        when(resourceReservationService.saveAll(any())).thenReturn(Completable.complete());
        when(deploymentHandler.deployResources(reservation, account.getAccountId(), dockerCredentials,
            List.of(vpc1, vpc2)))
            .thenReturn(Completable.complete());
        when(resourceReservationService.updateSetStatusByReservationId(reservation.getReservationId(), ReservationStatusValue.DEPLOYED))
            .thenReturn(Completable.complete());

        reservationHandler.postOne(rc)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.getLong("reservation_id")).isEqualTo(1L);
                    assertThat(result.getBoolean("is_active")).isTrue();
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method did throw exception" + " " + throwable.getMessage()))
            );
    }

    @Test
    void postOneNoCredentialsExist(VertxTestContext testContext) {
        Account account = TestObjectProvider.createAccount(1L);
        FunctionResource functionResource = TestObjectProvider.createFunctionResource(1L);
        VPC vpc = TestObjectProvider.createVPC(1L, account);
        FunctionResourceIds ids = TestObjectProvider.createFunctionResourceIds(1L,
            functionResource.getResource().getResourceId());
        List<FunctionResourceIds> functionResourceIds = List.of(ids);
        ReserveResourcesRequest request = TestObjectProvider.createReserveResourcesRequest(functionResourceIds);
        JsonObject requestBody = JsonObject.mapFrom(request);

        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        RoutingContextMockHelper.mockBody(rc, requestBody);
        when(functionResourceService.findOneByFunctionAndResource(ids.getFunctionId(), ids.getResourceId()))
            .thenReturn(Single.just(JsonObject.mapFrom(functionResource)));
        when(credentialsService.existsOnyByAccountIdAndProviderId(account.getAccountId(), 1L))
            .thenReturn(Single.just(false));
        when(resourceTypeMetricService.missingRequiredResourceTypeMetricsByResourceId(ids.getResourceId()))
            .thenReturn(Single.just(false));
        when(vpcService.findOneByRegionIdAndAccountId(functionResource.getResource().getRegion().getRegionId(),
            account.getAccountId())).thenReturn(Single.just(JsonObject.mapFrom(vpc)));

        reservationHandler.postOne(rc)
            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(UnauthorizedException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void postOneFunctionResourceNotExists(VertxTestContext testContext) {
        Account account = TestObjectProvider.createAccount(1L);
        FunctionResourceIds ids = TestObjectProvider.createFunctionResourceIds(1L, 2L);
        List<FunctionResourceIds> functionResourceIds = List.of(ids);
        ReserveResourcesRequest request = TestObjectProvider.createReserveResourcesRequest(functionResourceIds);
        JsonObject requestBody = JsonObject.mapFrom(request);
        Single<JsonObject> handler = new SingleHelper<JsonObject>().getEmptySingle();

        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        RoutingContextMockHelper.mockBody(rc, requestBody);
        when(functionResourceService.findOneByFunctionAndResource(1L, 2L))
            .thenReturn(handler);

        reservationHandler.postOne(rc)
            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void updateOneValid(VertxTestContext testContext) {
        long reservationId = 1L;
        Account account = TestObjectProvider.createAccount(1L);
        Reservation reservation = TestObjectProvider.createReservation(1L, true, account);
        JsonObject reservationJson = JsonObject.mapFrom(reservation);

        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        when(rc.pathParam("id")).thenReturn(String.valueOf(reservationId));
        when(reservationService.findOneByIdAndAccountId(reservationId, account.getAccountId()))
            .thenReturn(Single.just(reservationJson));
        when(resourceReservationService.updateSetStatusByReservationId(reservationId, ReservationStatusValue.TERMINATING))
            .thenReturn(Completable.complete());
        when(deploymentHandler.terminateResources(reservation, account.getAccountId()))
            .thenReturn(Completable.complete());
        when(resourceReservationService.updateSetStatusByReservationId(reservationId, ReservationStatusValue.TERMINATED))
            .thenReturn(Completable.complete());

        reservationHandler.updateOne(rc)
            .blockingSubscribe(() -> {},
                throwable -> testContext.verify(() -> fail("method did throw an exception"))
            );

        testContext.completeNow();
    }

    @Test
    void updateOneNotFound(VertxTestContext testContext) {
        long reservationId = 1L;
        Account account = TestObjectProvider.createAccount(1L);
        Single<JsonObject> handler = new SingleHelper<JsonObject>().getEmptySingle();

        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        when(rc.pathParam("id")).thenReturn(String.valueOf(reservationId));
        when(reservationService.findOneByIdAndAccountId(reservationId, account.getAccountId())).thenReturn(handler);

        reservationHandler.updateOne(rc)
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }
}
