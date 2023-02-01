package at.uibk.dps.rm.handler.reservation;

import at.uibk.dps.rm.entity.dto.ReserveResourcesRequest;
import at.uibk.dps.rm.entity.dto.reservation.FunctionResourceIds;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.handler.deployment.DeploymentHandler;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import at.uibk.dps.rm.service.rxjava3.database.function.FunctionResourceService;
import at.uibk.dps.rm.service.rxjava3.database.metric.MetricValueService;
import at.uibk.dps.rm.service.rxjava3.database.reservation.ReservationService;
import at.uibk.dps.rm.service.rxjava3.database.reservation.ResourceReservationService;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
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
    private MetricValueService metricValueService;

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
        when(serviceProxyProvider.getMetricValueService()).thenReturn(metricValueService);
        when(serviceProxyProvider.getFunctionResourceService()).thenReturn(functionResourceService);
        reservationHandler = new ReservationHandler(serviceProxyProvider, deploymentHandler);
    }

    @Test
    void getOneExistsAndIsActive(VertxTestContext testContext) {
        long reservationId = 1L;
        Account account = TestObjectProvider.createAccount(1L);
        Reservation reservation = TestObjectProvider.createReservation(1L, true, account);
        JsonArray resourceReservations = new JsonArray(TestObjectProvider.createResourceReservationsJson(reservation));
        MetricValue mv1 = TestObjectProvider.createMetricValue(1L, 1L, "latency", 25.0);
        MetricValue mv2 = TestObjectProvider.createMetricValue(2L, 2L, "availability", 0.995);
        JsonArray metricValues = new JsonArray(List.of(JsonObject.mapFrom(mv1), JsonObject.mapFrom(mv2)));

        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        when(rc.pathParam("id")).thenReturn(String.valueOf(reservationId));
        when(reservationService.findOneByIdAndAccountId(reservationId, account.getAccountId()))
            .thenReturn(Single.just(JsonObject.mapFrom(reservation)));
        when(resourceReservationService.findAllByReservationId(reservationId))
            .thenReturn(Single.just(resourceReservations));
        when(metricValueService.findAllByResource(33L, true)).thenReturn(Single.just(metricValues));

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
    void getOneExistsMetricValuesNotFound(VertxTestContext testContext) {
        long reservationId = 1L;
        Account account = TestObjectProvider.createAccount(1L);
        Reservation reservation = TestObjectProvider.createReservation(1L, true, account);
        JsonArray resourceReservations = new JsonArray(TestObjectProvider.createResourceReservationsJson(reservation));
        Single<JsonArray> handler = new SingleHelper<JsonArray>().getEmptySingle();

        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        when(rc.pathParam("id")).thenReturn(String.valueOf(reservationId));
        when(reservationService.findOneByIdAndAccountId(reservationId, account.getAccountId()))
            .thenReturn(Single.just(JsonObject.mapFrom(reservation)));
        when(resourceReservationService.findAllByReservationId(reservationId))
            .thenReturn(Single.just(resourceReservations));
        when(metricValueService.findAllByResource(anyLong(),anyBoolean())).thenReturn(handler);

        reservationHandler.getOne(rc)
            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
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
        JsonArray reservations = new JsonArray(List.of(JsonObject.mapFrom(r1),
            JsonObject.mapFrom(r2), JsonObject.mapFrom(r3)));

        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        when(reservationService.findAllByAccountId(account.getAccountId())).thenReturn(Single.just(reservations));

        reservationHandler.getAll(rc)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.size()).isEqualTo(3);
                    assertThat(result.getJsonObject(0).getLong("reservation_id")).isEqualTo(1L);
                    assertThat(result.getJsonObject(1).getLong("reservation_id")).isEqualTo(2L);
                    assertThat(result.getJsonObject(2).getLong("reservation_id")).isEqualTo(3L);
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
        FunctionResourceIds ids1 = TestObjectProvider.createFunctionResourceIds(1L, 1L);
        FunctionResourceIds ids2 = TestObjectProvider.createFunctionResourceIds(2L, 6L);
        FunctionResourceIds ids3 = TestObjectProvider.createFunctionResourceIds(3L, 2L);
        FunctionResourceIds ids4 = TestObjectProvider.createFunctionResourceIds(4L, 1L);
        List<FunctionResourceIds> functionResourceIds = List.of(ids1, ids2, ids3, ids4);
        ReserveResourcesRequest request = TestObjectProvider.createReserveResourcesRequest(functionResourceIds);
        JsonObject requestBody = JsonObject.mapFrom(request);
        Account account = TestObjectProvider.createAccount(1L);
        Reservation reservation = TestObjectProvider.createReservation(1L, true, account);
        JsonObject reservationJson = JsonObject.mapFrom(reservation);

        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        RoutingContextMockHelper.mockBody(rc, requestBody);
        when(functionResourceService.findOneByFunctionAndResource(1L, 1L))
            .thenReturn(Single.just(JsonObject.mapFrom(ids1)));
        when(functionResourceService.findOneByFunctionAndResource(2L, 6L))
            .thenReturn(Single.just(JsonObject.mapFrom(ids2)));
        when(functionResourceService.findOneByFunctionAndResource(3L, 2L))
            .thenReturn(Single.just(JsonObject.mapFrom(ids3)));
        when(functionResourceService.findOneByFunctionAndResource(4L, 1L))
            .thenReturn(Single.just(JsonObject.mapFrom(ids4)));
        when(reservationService.save(any())).thenReturn(Single.just(reservationJson));
        when(resourceReservationService.saveAll(any())).thenReturn(Completable.complete());
        when(deploymentHandler.deployResources(1L, 1L)).thenReturn(Completable.complete());

        reservationHandler.postOne(rc)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.getLong("reservation_id")).isEqualTo(1L);
                    assertThat(result.getBoolean("is_active")).isTrue();
                    verify(reservationService).save(new JsonObject(
                        "{\"reservation_id\":null," +
                        "\"created_by\":null," +
                        "\"created_by\":{" +
                        "   \"account_id\":1," +
                        "   \"username\":null," +
                        "   \"password\":null," +
                        "   \"created_at\":null," +
                        "   \"is_active\":true" +
                        "}," +
                        "\"created_at\":null," +
                        "\"is_active\":true}"));
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method did throw exception" + " " + throwable.getMessage()))
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
        when(reservationService.cancelReservationById(reservationId)).thenReturn(Completable.complete());

        reservationHandler.updateOne(rc)
            .blockingSubscribe(() -> {},
                throwable -> testContext.verify(() -> fail("method did throw exception"))
            );

        verify(reservationService).cancelReservationById(reservationId);
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
