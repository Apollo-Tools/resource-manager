package at.uibk.dps.rm.handler.reservation;

import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.service.rxjava3.database.reservation.ReservationService;
import at.uibk.dps.rm.testutil.SingleHelper;
import at.uibk.dps.rm.testutil.objectprovider.TestAccountProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestReservationProvider;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
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
import static org.mockito.Mockito.when;

/**
 * Implements tests for the {@link ReservationChecker} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class ReservationCheckerTest {

    private ReservationChecker reservationChecker;

    @Mock
    private ReservationService reservationService;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        reservationChecker = new ReservationChecker(reservationService);
    }

    @Test
    void checkFindAll(VertxTestContext testContext) {
        Account account = TestAccountProvider.createAccount(1L);
        Reservation r1 = TestReservationProvider.createReservation(1L, true, account);
        Reservation r2 = TestReservationProvider.createReservation(2L, true, account);
        Reservation r3 = TestReservationProvider.createReservation(3L, true, account);
        JsonArray reservations = new JsonArray(List.of(JsonObject.mapFrom(r1),
            JsonObject.mapFrom(r2), JsonObject.mapFrom(r3)));

        when(reservationService.findAllByAccountId(account.getAccountId())).thenReturn(Single.just(reservations));

        reservationChecker.checkFindAll(account.getAccountId())
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.size()).isEqualTo(3);
                    assertThat(result.getJsonObject(0).getLong("reservation_id")).isEqualTo(1L);
                    assertThat(result.getJsonObject(1).getLong("reservation_id")).isEqualTo(2L);
                    assertThat(result.getJsonObject(2).getLong("reservation_id")).isEqualTo(3L);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void checkFindAllNotFound(VertxTestContext testContext) {
        Account account = TestAccountProvider.createAccount(1L);
        Single<JsonArray> handler = SingleHelper.getEmptySingle();

        when(reservationService.findAllByAccountId(account.getAccountId())).thenReturn(handler);

        reservationChecker.checkFindAll(account.getAccountId())
            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
            }));
    }

    @Test
    void checkFindOne(VertxTestContext testContext) {
        long accountId = 2L, reservationId = 1L;
        Reservation r1 = TestReservationProvider.createReservation(reservationId);

        when(reservationService.findOneByIdAndAccountId(reservationId, accountId))
            .thenReturn(Single.just(JsonObject.mapFrom(r1)));

        reservationChecker.checkFindOne(reservationId, accountId)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.getLong("reservation_id")).isEqualTo(1L);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void checkFindOneNotFound(VertxTestContext testContext) {
        long accountId = 2L, reservationId = 1L;
        Single<JsonObject> handler = SingleHelper.getEmptySingle();

        when(reservationService.findOneByIdAndAccountId(reservationId, accountId))
            .thenReturn(handler);

        reservationChecker.checkFindOne(reservationId, accountId)
            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                }));
    }

    @Test
    void submitCreateReservation(VertxTestContext testContext) {
        long accountId = 1L;
        Reservation reservation = new Reservation();
        reservation.setIsActive(true);
        Account account = new Account();
        account.setAccountId(accountId);
        reservation.setCreatedBy(account);
        JsonObject persistedReservation = JsonObject.mapFrom(TestReservationProvider.createReservation(1L));

        when(reservationService.save(JsonObject.mapFrom(reservation)))
            .thenReturn(Single.just(persistedReservation));

        reservationChecker.submitCreateReservation(accountId)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.getLong("reservation_id")).isEqualTo(1L);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }
}
