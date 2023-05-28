package at.uibk.dps.rm.service.database.reservation;

import at.uibk.dps.rm.entity.deployment.ReservationStatusValue;
import at.uibk.dps.rm.entity.model.Reservation;
import at.uibk.dps.rm.entity.model.ServiceReservation;
import at.uibk.dps.rm.repository.reservation.ServiceReservationRepository;
import at.uibk.dps.rm.testutil.objectprovider.TestReservationProvider;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.hibernate.reactive.util.impl.CompletionStages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Implements tests for the {@link ServiceReservationServiceImpl} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class ServiceReservationImplTest {

    private ServiceReservationService service;

    @Mock
    ServiceReservationRepository repository;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        service = new ServiceReservationServiceImpl(repository);
    }

    @Test
    void findAllByReservationId(VertxTestContext testContext) {
        long reservationId = 1L;
        ServiceReservation entity1 = TestReservationProvider
            .createServiceReservation(4L, new Reservation());
        ServiceReservation entity2 = TestReservationProvider
            .createServiceReservation(5L, new Reservation());
        List<ServiceReservation> resultList = new ArrayList<>();
        resultList.add(entity1);
        resultList.add(entity2);
        CompletionStage<List<ServiceReservation>> completionStage = CompletionStages.completedFuture(resultList);
        when(repository.findAllByReservationId(reservationId)).thenReturn(completionStage);

        service.findAllByReservationId(reservationId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(2);

                for (int i = 0; i < 2; i++) {
                    JsonObject resultJson = result.getJsonObject(i);
                    assertThat(resultJson.getLong("resource_reservation_id")).isEqualTo(i + 4);
                    assertThat(resultJson.getJsonObject("reservation")).isNull();
                    assertThat(resultJson.getJsonObject("resource"))
                        .isNull();
                }
                testContext.completeNow();
            })));
    }

    @Test
    void findAllByReservationIdEmpty(VertxTestContext testContext) {
        long resourceId = 1L;
        List<ServiceReservation> resultList = new ArrayList<>();
        CompletionStage<List<ServiceReservation>> completionStage = CompletionStages.completedFuture(resultList);
        when(repository.findAllByReservationId(resourceId)).thenReturn(completionStage);

        service.findAllByReservationId(resourceId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(0);
                testContext.completeNow();
            })));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void existsReadyForContainerStartupAndTermination(boolean exists, VertxTestContext testContext) {
        long reservationId = 1L, resourceReservationId = 2L, accountId = 3L;
        CompletionStage<ServiceReservation> completionStage = CompletionStages.completedFuture(exists ?
            new ServiceReservation() : null);

        when(repository.findOneByReservationStatus(reservationId, resourceReservationId, accountId,
            ReservationStatusValue.DEPLOYED)).thenReturn(completionStage);

        service.existsReadyForContainerStartupAndTermination(reservationId, resourceReservationId, accountId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isEqualTo(exists);
                testContext.completeNow();
            })));
    }
}
