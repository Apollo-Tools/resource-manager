package at.uibk.dps.rm.service.reservation;

import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.repository.ResourceReservationRepository;
import at.uibk.dps.rm.service.database.reservation.ResourceReservationService;
import at.uibk.dps.rm.service.database.reservation.ResourceReservationServiceImpl;
import at.uibk.dps.rm.util.JsonMapperConfig;
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
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CompletionStage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

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
        long resourceId = 1L;
        Resource resource = new Resource();
        resource.setResourceId(resourceId);
        resource.setResourceType(new ResourceType());
        resource.setMetricValues(new HashSet<>());
        ResourceReservation entity1 = new ResourceReservation();
        entity1.setResourceReservationId(2L);
        entity1.setReservation(new Reservation());
        entity1.setResource(resource);
        ResourceReservation entity2 = new ResourceReservation();
        entity2.setResourceReservationId(3L);
        entity2.setReservation(new Reservation());
        entity2.setResource(resource);
        List<ResourceReservation> resultList = new ArrayList<>();
        resultList.add(entity1);
        resultList.add(entity2);
        CompletionStage<List<ResourceReservation>> completionStage = CompletionStages.completedFuture(resultList);
        doReturn(completionStage).when(resourceReservationRepository).findAllByResourceId(resourceId);

        resourceReservationService.findAllByResourceId(resourceId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(2);

                for (int i = 0; i < 2; i++) {
                    JsonObject resultJson = result.getJsonObject(i);
                    assertThat(resultJson.getLong("resource_reservation_id")).isEqualTo(i + 2);
                    assertThat(resultJson.getJsonObject("reservation")).isNull();
                    assertThat(resultJson.getJsonObject("resource").getLong("resource_id")).isEqualTo(resourceId);
                    assertThat(resultJson.getJsonObject("resource").getJsonObject("resource_type")).isNull();
                    assertThat(resultJson.getJsonObject("resource").getJsonObject("metric_values")).isNull();
                }

                verify(resourceReservationRepository).findAllByResourceId(resourceId);
                testContext.completeNow();
        })));
    }

    @Test
    void findAllByResourceIdEmpty(VertxTestContext testContext) {
        long resourceId = 1L;
        List<ResourceReservation> resultList = new ArrayList<>();
        CompletionStage<List<ResourceReservation>> completionStage = CompletionStages.completedFuture(resultList);
        doReturn(completionStage).when(resourceReservationRepository).findAllByResourceId(resourceId);

        resourceReservationService.findAllByResourceId(resourceId)
                .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                    assertThat(result.size()).isEqualTo(0);
                    verify(resourceReservationRepository).findAllByResourceId(resourceId);
                    testContext.completeNow();
                })));
    }
}
