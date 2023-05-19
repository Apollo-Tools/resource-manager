package at.uibk.dps.rm.service.database.reservation;

import at.uibk.dps.rm.entity.deployment.ReservationStatusValue;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.repository.reservation.ResourceReservationRepository;
import at.uibk.dps.rm.testutil.objectprovider.TestReservationProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestResourceProvider;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.hibernate.reactive.util.impl.CompletionStages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class ResourceReservationServiceImplTest {

    private ResourceReservationService resourceReservationService;

    @Mock
    ResourceReservationRepository resourceReservationRepository;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        resourceReservationService = new ResourceReservationServiceImpl(resourceReservationRepository);
    }

    @Test
    void findAllByResourceId(VertxTestContext testContext) {
        long reservationId = 1L;
        Resource resource = TestResourceProvider.createResource(1L);
        ResourceReservation entity1 = TestReservationProvider
            .createResourceReservation(4L, new Reservation(), resource, new ResourceReservationStatus());
        ResourceReservation entity2 = TestReservationProvider
            .createResourceReservation(5L, new Reservation(), resource, new ResourceReservationStatus());
        List<ResourceReservation> resultList = new ArrayList<>();
        resultList.add(entity1);
        resultList.add(entity2);
        CompletionStage<List<ResourceReservation>> completionStage = CompletionStages.completedFuture(resultList);
        when(resourceReservationRepository.findAllByReservationId(reservationId)).thenReturn(completionStage);

        resourceReservationService.findAllByReservationId(reservationId)
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
    void findAllByResourceIdEmpty(VertxTestContext testContext) {
        long resourceId = 1L;
        List<ResourceReservation> resultList = new ArrayList<>();
        CompletionStage<List<ResourceReservation>> completionStage = CompletionStages.completedFuture(resultList);
        when(resourceReservationRepository.findAllByReservationId(resourceId)).thenReturn(completionStage);

        resourceReservationService.findAllByReservationId(resourceId)
                .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                    assertThat(result.size()).isEqualTo(0);
                    verify(resourceReservationRepository).findAllByReservationId(resourceId);
                    testContext.completeNow();
                })));
    }

    @Test
    void updateTriggerUrl(VertxTestContext testContext) {
        long functionResourceId = 1L;
        String triggerUrl = "url";
        CompletionStage<Integer> completionStage = CompletionStages.completedFuture(1);

        when(resourceReservationRepository.updateTriggerUrl(functionResourceId, triggerUrl))
            .thenReturn(completionStage);

        resourceReservationService.updateTriggerUrl(functionResourceId, triggerUrl)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isNull();
                testContext.completeNow();
            })));
    }

    @Test
    void updateSetStatusByReservationId(VertxTestContext testContext) {
        long reservationId = 1L;
        ReservationStatusValue statusValue = ReservationStatusValue.NEW;
        CompletionStage<Integer> completionStage = CompletionStages.completedFuture(1);

        when(resourceReservationRepository.updateReservationStatusByReservationId(reservationId, statusValue))
            .thenReturn(completionStage);

        resourceReservationService.updateSetStatusByReservationId(reservationId, statusValue)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isNull();
                testContext.completeNow();
            })));
    }
}
