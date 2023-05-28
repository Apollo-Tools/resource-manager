package at.uibk.dps.rm.handler.reservation;

import at.uibk.dps.rm.entity.model.FunctionReservation;
import at.uibk.dps.rm.entity.model.Reservation;
import at.uibk.dps.rm.service.rxjava3.database.reservation.FunctionReservationService;
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
 * Implements tests for the {@link FunctionReservationChecker} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class FunctionReservationCheckerTest {

    private FunctionReservationChecker checker;

    @Mock
    private FunctionReservationService service;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        checker = new FunctionReservationChecker(service);
    }



    @Test
    void checkFindAll(VertxTestContext testContext) {
        Reservation reservation = TestReservationProvider.createReservation(1L);
        FunctionReservation fr1 = TestReservationProvider.createFunctionReservation(1L, reservation);
        FunctionReservation fr2 = TestReservationProvider.createFunctionReservation(2L, reservation);
        JsonArray reservations = new JsonArray(List.of(JsonObject.mapFrom(fr1), JsonObject.mapFrom(fr2)));

        when(service.findAllByReservationId(reservation.getReservationId())).thenReturn(Single.just(reservations));

        checker.checkFindAllByReservationId(reservation.getReservationId())
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.size()).isEqualTo(2);
                    assertThat(result.getJsonObject(0).getLong("resource_reservation_id")).isEqualTo(1L);
                    assertThat(result.getJsonObject(1).getLong("resource_reservation_id")).isEqualTo(2L);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void checkFindAllEmptyList(VertxTestContext testContext) {
        Reservation reservation = TestReservationProvider.createReservation(1L);
        JsonArray reservations = new JsonArray();

        when(service.findAllByReservationId(reservation.getReservationId())).thenReturn(Single.just(reservations));

        checker.checkFindAllByReservationId(reservation.getReservationId())
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.size()).isEqualTo(0);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }
}
