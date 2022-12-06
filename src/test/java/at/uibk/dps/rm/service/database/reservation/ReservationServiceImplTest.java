package at.uibk.dps.rm.service.database.reservation;

import at.uibk.dps.rm.entity.model.Reservation;
import at.uibk.dps.rm.repository.ReservationRepository;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.hibernate.reactive.util.impl.CompletionStages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CompletionStage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class ReservationServiceImplTest {

    private ReservationService reservationService;

    @Mock
    ReservationRepository reservationRepository;

    @BeforeEach
    void initTest() {
        reservationService = new ReservationServiceImpl(reservationRepository);
    }

    @Test
    void cancelReservationById(VertxTestContext testContext) {
        long reservationId = 1L;
        CompletionStage<Reservation> completionStage = CompletionStages.completedFuture(null);
        doReturn(completionStage).when(reservationRepository).cancelReservation(reservationId);

        reservationService.cancelReservationById(reservationId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isNull();
                verify(reservationRepository).cancelReservation(reservationId);
                testContext.completeNow();
        })));
    }
}
