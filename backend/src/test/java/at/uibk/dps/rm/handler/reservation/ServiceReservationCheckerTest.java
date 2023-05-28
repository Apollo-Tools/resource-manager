package at.uibk.dps.rm.handler.reservation;

import at.uibk.dps.rm.entity.model.Reservation;
import at.uibk.dps.rm.entity.model.ServiceReservation;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.service.rxjava3.database.reservation.ServiceReservationService;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.when;

/**
 * Implements tests for the {@link ServiceReservationChecker} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class ServiceReservationCheckerTest {

    private ServiceReservationChecker checker;

    @Mock
    private ServiceReservationService service;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        checker = new ServiceReservationChecker(service);
    }

    @Test
    void checkFindAll(VertxTestContext testContext) {
        Reservation reservation = TestReservationProvider.createReservation(1L);
        ServiceReservation sr1 = TestReservationProvider.createServiceReservation(1L, reservation);
        ServiceReservation sr2 = TestReservationProvider.createServiceReservation(2L, reservation);
        JsonArray reservations = new JsonArray(List.of(JsonObject.mapFrom(sr1), JsonObject.mapFrom(sr2)));

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

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void checkReadyForStartup(boolean isReady, VertxTestContext testContext) {
        long reservationId = 1L, resourceReservationId = 2L, accountId = 3L;

        when(service.existsReadyForContainerStartupAndTermination(reservationId, resourceReservationId, accountId))
            .thenReturn(Single.just(isReady));

        checker.checkReadyForStartup(reservationId, resourceReservationId, accountId)
            .blockingSubscribe(() -> testContext.verify(() -> {
                    if (!isReady) {
                        fail("method did not throw exception");
                    }
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> {
                    if (isReady) {
                        fail("method has thrown exception");
                    } else {
                        assertThat(throwable).isInstanceOf(NotFoundException.class);
                    }

                })
            );

        testContext.completeNow();
    }
}
