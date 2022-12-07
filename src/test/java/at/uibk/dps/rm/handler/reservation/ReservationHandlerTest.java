package at.uibk.dps.rm.handler.reservation;

import at.uibk.dps.rm.entity.dto.ReserveResourcesRequest;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import at.uibk.dps.rm.service.rxjava3.database.account.CredentialsService;
import at.uibk.dps.rm.service.rxjava3.database.metric.MetricValueService;
import at.uibk.dps.rm.service.rxjava3.database.reservation.ReservationService;
import at.uibk.dps.rm.service.rxjava3.database.reservation.ResourceReservationService;
import at.uibk.dps.rm.service.rxjava3.database.resource.ResourceService;
import at.uibk.dps.rm.service.rxjava3.deployment.DeploymentService;
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
    private ResourceService resourceService;

    @Mock
    private ResourceReservationService resourceReservationService;

    @Mock
    private MetricValueService metricValueService;

    @Mock
    private DeploymentService deploymentService;

    @Mock
    private CredentialsService credentialsService;

    @Mock
    private RoutingContext rc;

    @Mock
    private ServiceProxyProvider serviceProxyProvider;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        when(serviceProxyProvider.getReservationService()).thenReturn(reservationService);
        when(serviceProxyProvider.getResourceService()).thenReturn(resourceService);
        when(serviceProxyProvider.getResourceReservationService()).thenReturn(resourceReservationService);
        when(serviceProxyProvider.getMetricValueService()).thenReturn(metricValueService);
        when(serviceProxyProvider.getDeploymentService()).thenReturn(deploymentService);
        when(serviceProxyProvider.getCredentialsService()).thenReturn(credentialsService);
        reservationHandler = new ReservationHandler(serviceProxyProvider);
    }

    @Test
    void getOneExistsAndIsActive(VertxTestContext testContext) {
        long reservationId = 1L;
        Account account = TestObjectProvider.createAccount(1L);
        Reservation reservation = TestObjectProvider.createReservation(1L, true, account);
        JsonArray resourceReservations = new JsonArray(TestObjectProvider.createResourceReservationsJson(reservation));
        MetricValue mv1 = TestObjectProvider.createMetricValue(1L, 1L, "latency", 25.0);
        MetricValue mv2 = TestObjectProvider.createMetricValue(2L, 2L, "availability", 0.995);
        MetricValue mv3 = TestObjectProvider.createMetricValue(3L, 3L, "bandwidth", 1000);
        MetricValue mv4 = TestObjectProvider.createMetricValue(3L, 3L, "bandwidth", 1000);
        MetricValue mv5 = TestObjectProvider.createMetricValue(3L, 3L, "bandwidth", 1000);
        JsonArray metricValues1 = new JsonArray(List.of(JsonObject.mapFrom(mv1), JsonObject.mapFrom(mv2)));
        JsonArray metricValues2 = new JsonArray(List.of(JsonObject.mapFrom(mv3)));
        JsonArray metricValues3 = new JsonArray(List.of(JsonObject.mapFrom(mv4), JsonObject.mapFrom(mv5)));

        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        when(rc.pathParam("id")).thenReturn(String.valueOf(reservationId));
        when(reservationService.findOneByByIdAndAccountId(reservationId, account.getAccountId()))
            .thenReturn(Single.just(JsonObject.mapFrom(reservation)));
        when(resourceReservationService.findAllByReservationId(reservationId))
            .thenReturn(Single.just(resourceReservations));
        when(metricValueService.findAllByResource(1L, true)).thenReturn(Single.just(metricValues1));
        when(metricValueService.findAllByResource(2L, true)).thenReturn(Single.just(metricValues2));
        when(metricValueService.findAllByResource(3L, true)).thenReturn(Single.just(metricValues3));

        reservationHandler.getOne(rc)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.getJsonArray("resource_reservations").size()).isEqualTo(3);
                    JsonObject resourceJson1 = result.getJsonArray("resource_reservations").getJsonObject(0)
                        .getJsonObject("resource");
                    JsonObject resourceJson2 = result.getJsonArray("resource_reservations").getJsonObject(1)
                        .getJsonObject("resource");
                    JsonObject resourceJson3 = result.getJsonArray("resource_reservations").getJsonObject(2)
                        .getJsonObject("resource");
                    assertThat(resourceJson1.getLong("resource_id")).isEqualTo(1L);
                    assertThat(resourceJson1.getJsonArray("metric_values").size()).isEqualTo(2);
                    assertThat(resourceJson2.getLong("resource_id")).isEqualTo(2L);
                    assertThat(resourceJson2.getJsonArray("metric_values").size()).isEqualTo(1);
                    assertThat(resourceJson3.getLong("resource_id")).isEqualTo(3L);
                    assertThat(resourceJson3.getJsonArray("metric_values").size()).isEqualTo(2);
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
        when(reservationService.findOneByByIdAndAccountId(reservationId, account.getAccountId()))
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
        when(reservationService.findOneByByIdAndAccountId(reservationId, account.getAccountId()))
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
        when(reservationService.findOneByByIdAndAccountId(reservationId, account.getAccountId()))
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
        when(reservationService.findOneByByIdAndAccountId(reservationId, account.getAccountId())).thenReturn(handler);

        reservationHandler.getOne(rc)
            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void postOneValid(VertxTestContext testContext) {
        List<Long> resources = List.of(1L, 2L, 3L, 4L);
        ReserveResourcesRequest request = TestObjectProvider.createReserveResourcesRequest(resources,
            false);
        JsonObject requestBody = JsonObject.mapFrom(request);
        Account account = TestObjectProvider.createAccount(1L);
        Reservation reservation = TestObjectProvider.createReservation(1L, true, account);
        JsonObject reservationJson = JsonObject.mapFrom(reservation);

        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        RoutingContextMockHelper.mockBody(rc, requestBody);
        when(resourceService.existsOneAndNotReserved(anyLong())).thenReturn(Single.just(true));
        when(reservationService.save(any())).thenReturn(Single.just(reservationJson));
        when(resourceReservationService.saveAll(any())).thenReturn(Completable.complete());

        reservationHandler.postOne(rc)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.getLong("reservation_id")).isEqualTo(1L);
                    assertThat(result.getBoolean("is_active")).isTrue();
                    verify(reservationService).save(new JsonObject("{\"reservation_id\":null," +
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
                throwable -> testContext.verify(() -> fail("method did throw exception"))
            );
    }

    @Test
    void postOneResourceNotExistsOrReserved(VertxTestContext testContext) {
        List<Long> resources = List.of(1L);
        Account account = TestObjectProvider.createAccount(1L);
        ReserveResourcesRequest request = TestObjectProvider.createReserveResourcesRequest(resources,
            false);
        JsonObject requestBody = JsonObject.mapFrom(request);

        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        RoutingContextMockHelper.mockBody(rc, requestBody);
        when(resourceService.existsOneAndNotReserved(1L)).thenReturn(Single.just(false));

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
        when(reservationService.findOneByByIdAndAccountId(reservationId, account.getAccountId()))
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
        when(reservationService.findOneByByIdAndAccountId(reservationId, account.getAccountId())).thenReturn(handler);

        reservationHandler.updateOne(rc)
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }
}
