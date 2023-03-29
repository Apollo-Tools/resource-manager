package at.uibk.dps.rm.service.database.reservation;

import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.repository.reservation.ResourceReservationRepository;
import at.uibk.dps.rm.testutil.TestObjectProvider;
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
        long reservationId = 1L, functionResourceId = 3L;
        Resource resource = TestObjectProvider.createResource(1L);
        Function function = TestObjectProvider.createFunction(2L, "func", "false");
        FunctionResource functionResource = TestObjectProvider
            .createFunctionResource(functionResourceId, function, resource, false);
        ResourceReservation entity1 = TestObjectProvider
            .createResourceReservation(4L, functionResource, new Reservation(), new ResourceReservationStatus());
        ResourceReservation entity2 = TestObjectProvider
            .createResourceReservation(5L, functionResource, new Reservation(), new ResourceReservationStatus());
        List<ResourceReservation> resultList = new ArrayList<>();
        resultList.add(entity1);
        resultList.add(entity2);
        CompletionStage<List<ResourceReservation>> completionStage = CompletionStages.completedFuture(resultList);
        doReturn(completionStage).when(resourceReservationRepository).findAllByReservationId(reservationId);

        resourceReservationService.findAllByReservationId(reservationId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(2);

                for (int i = 0; i < 2; i++) {
                    JsonObject resultJson = result.getJsonObject(i);
                    assertThat(resultJson.getLong("resource_reservation_id")).isEqualTo(i + 4);
                    assertThat(resultJson.getJsonObject("reservation")).isNull();
                    JsonObject functionResourceResult = resultJson.getJsonObject("function_resource");
                    assertThat(functionResourceResult.getLong("function_resource_id")).isEqualTo(functionResourceId);
                    assertThat(functionResourceResult.getJsonObject("resource").getJsonObject("resource_type"))
                        .isNull();
                    assertThat(functionResourceResult.getJsonObject("resource").getJsonObject("metric_values"))
                        .isNull();
                    assertThat(functionResourceResult.getJsonObject("function").getJsonObject("runtime"))
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
        doReturn(completionStage).when(resourceReservationRepository).findAllByReservationId(resourceId);

        resourceReservationService.findAllByReservationId(resourceId)
                .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                    assertThat(result.size()).isEqualTo(0);
                    verify(resourceReservationRepository).findAllByReservationId(resourceId);
                    testContext.completeNow();
                })));
    }
}
