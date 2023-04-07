package at.uibk.dps.rm.handler.log;

import at.uibk.dps.rm.entity.model.Account;
import at.uibk.dps.rm.entity.model.Reservation;
import at.uibk.dps.rm.entity.model.ReservationLog;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.handler.reservation.ReservationChecker;
import at.uibk.dps.rm.testutil.RoutingContextMockHelper;
import at.uibk.dps.rm.testutil.objectprovider.TestAccountProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestLogProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestReservationProvider;
import at.uibk.dps.rm.util.JsonMapperConfig;
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
import static org.mockito.Mockito.when;

@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class ReservationLogHandlerTest {

    private ReservationLogHandler handler;

    @Mock
    private ReservationLogChecker reservationLogChecker;

    @Mock
    private LogChecker logChecker;

    @Mock
    private ReservationChecker reservationChecker;

    @Mock
    private RoutingContext rc;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        handler = new ReservationLogHandler(reservationLogChecker, logChecker, reservationChecker);
    }

    @Test
    void getAll(VertxTestContext testContext) {
        Account account = TestAccountProvider.createAccount(22L);
        Reservation reservation = TestReservationProvider.createReservation(11L);
        ReservationLog rl1 = TestLogProvider.createReservationLog(1L, reservation);
        ReservationLog rl2 = TestLogProvider.createReservationLog(2L, reservation);
        JsonArray reservationLogs = new JsonArray(List.of(JsonObject.mapFrom(rl1), JsonObject.mapFrom(rl2)));

        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        when(rc.pathParam("id")).thenReturn(String.valueOf(reservation.getReservationId()));
        when(reservationChecker.checkFindOne(11L, 22L)).thenReturn(Single.just(JsonObject.mapFrom(reservation)));
        when(logChecker.checkFindAllByReservationId(11L, 22L))
            .thenReturn(Single.just(reservationLogs));

        handler.getAll(rc)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.size()).isEqualTo(2);
                    assertThat(result.getJsonObject(0).getLong("reservation_log_id")).isEqualTo(1L);
                    assertThat(result.getJsonObject(1).getLong("reservation_log_id")).isEqualTo(2L);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void getAllEmpty(VertxTestContext testContext) {
        Account account = TestAccountProvider.createAccount(22L);
        Reservation reservation = TestReservationProvider.createReservation(11L);
        JsonArray reservationLogs = new JsonArray(new ArrayList<>());

        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        when(rc.pathParam("id")).thenReturn(String.valueOf(reservation.getReservationId()));
        when(reservationChecker.checkFindOne(11L, 22L)).thenReturn(Single.just(JsonObject.mapFrom(reservation)));
        when(logChecker.checkFindAllByReservationId(11L, 22L))
            .thenReturn(Single.just(reservationLogs));

        handler.getAll(rc)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.size()).isEqualTo(0);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void getAllReservationNotFound(VertxTestContext testContext) {
        Account account = TestAccountProvider.createAccount(22L);
        Reservation reservation = TestReservationProvider.createReservation(11L);

        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        when(rc.pathParam("id")).thenReturn(String.valueOf(reservation.getReservationId()));
        when(reservationChecker.checkFindOne(11L, 22L)).thenReturn(Single.error(NotFoundException::new));

        handler.getAll(rc)
            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                }));
    }
}