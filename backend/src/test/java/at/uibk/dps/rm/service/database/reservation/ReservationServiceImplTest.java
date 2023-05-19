package at.uibk.dps.rm.service.database.reservation;

import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.repository.reservation.ReservationRepository;
import at.uibk.dps.rm.testutil.objectprovider.TestAccountProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestReservationProvider;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.hibernate.reactive.util.impl.CompletionStages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.concurrent.CompletionStage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Implements tests for the {@link ReservationServiceImpl} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class ReservationServiceImplTest {

    private ReservationService reservationService;

    @Mock
    ReservationRepository reservationRepository;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        reservationService = new ReservationServiceImpl(reservationRepository);
    }

    @Test
    void findAllByAccountId(VertxTestContext testContext) {
        long accountId = 1L;
        Account account = TestAccountProvider.createAccount(accountId);
        Reservation r1 = TestReservationProvider.createReservation(1L, true, account);
        Reservation r2 = TestReservationProvider.createReservation(2L, true, account);
        Reservation r3 = TestReservationProvider.createReservation(3L, true, account);
        CompletionStage<List<Reservation>> completionStage = CompletionStages.completedFuture(List.of(r1, r2, r3));

        when(reservationRepository.findAllByAccountId(accountId)).thenReturn(completionStage);

        reservationService.findAllByAccountId(accountId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(3);
                assertThat(result.getJsonObject(0).getLong("reservation_id")).isEqualTo(1L);
                assertThat(result.getJsonObject(1).getLong("reservation_id")).isEqualTo(2L);
                assertThat(result.getJsonObject(2).getLong("reservation_id")).isEqualTo(3L);
                verify(reservationRepository).findAllByAccountId(accountId);
                testContext.completeNow();
            })));
    }

    @Test
    void findOneByIdAndAccountExists(VertxTestContext testContext) {
        long reservationId = 1L;
        long accountId = 2L;
        Account account = TestAccountProvider.createAccount(accountId);
        Reservation entity = TestReservationProvider.createReservation(reservationId, true, account);

        CompletionStage<Reservation> completionStage = CompletionStages.completedFuture(entity);
        when(reservationRepository.findByIdAndAccountId(reservationId, accountId)).thenReturn(completionStage);

        reservationService.findOneByIdAndAccountId(reservationId, accountId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.getLong("reservation_id")).isEqualTo(1L);
                verify(reservationRepository).findByIdAndAccountId(reservationId, accountId);
                testContext.completeNow();
            })));
    }

    @Test
    void findOneByIdAndAccountNotExists(VertxTestContext testContext) {
        long reservationId = 1L;
        long accountId = 2L;
        CompletionStage<Reservation> completionStage = CompletionStages.completedFuture(null);

        when(reservationRepository.findByIdAndAccountId(reservationId, accountId)).thenReturn(completionStage);

        reservationService.findOneByIdAndAccountId(reservationId, accountId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isNull();
                verify(reservationRepository).findByIdAndAccountId(reservationId, accountId);
                testContext.completeNow();
            })));
    }
}
