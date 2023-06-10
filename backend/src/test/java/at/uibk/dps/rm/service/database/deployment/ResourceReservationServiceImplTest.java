package at.uibk.dps.rm.service.database.deployment;

import at.uibk.dps.rm.entity.deployment.DeploymentStatusValue;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.repository.deployment.ResourceDeploymentRepository;
import at.uibk.dps.rm.testutil.objectprovider.TestReservationProvider;
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

/**
 * Implements tests for the {@link ResourceDeploymentServiceImpl} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class ResourceReservationServiceImplTest {

    private ResourceDeploymentService resourceReservationService;

    @Mock
    ResourceDeploymentRepository resourceReservationRepository;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        resourceReservationService = new ResourceDeploymentServiceImpl(resourceReservationRepository);
    }

    @Test
    void findAllByReservationId(VertxTestContext testContext) {
        long reservationId = 1L;
        ResourceDeployment entity1 = TestReservationProvider
            .createFunctionReservation(4L, new Deployment());
        ResourceDeployment entity2 = TestReservationProvider
            .createServiceReservation(5L, new Deployment());
        List<ResourceDeployment> resultList = new ArrayList<>();
        resultList.add(entity1);
        resultList.add(entity2);
        CompletionStage<List<ResourceDeployment>> completionStage = CompletionStages.completedFuture(resultList);
        when(resourceReservationRepository.findAllByDeploymentId(reservationId)).thenReturn(completionStage);

        resourceReservationService.findAllByDeploymentId(reservationId)
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
        List<ResourceDeployment> resultList = new ArrayList<>();
        CompletionStage<List<ResourceDeployment>> completionStage = CompletionStages.completedFuture(resultList);
        when(resourceReservationRepository.findAllByDeploymentId(resourceId)).thenReturn(completionStage);

        resourceReservationService.findAllByDeploymentId(resourceId)
                .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                    assertThat(result.size()).isEqualTo(0);
                    verify(resourceReservationRepository).findAllByDeploymentId(resourceId);
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
        DeploymentStatusValue statusValue = DeploymentStatusValue.NEW;
        CompletionStage<Integer> completionStage = CompletionStages.completedFuture(1);

        when(resourceReservationRepository.updateDeploymentStatusByDeploymentId(reservationId, statusValue))
            .thenReturn(completionStage);

        resourceReservationService.updateSetStatusByDeploymentId(reservationId, statusValue)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isNull();
                testContext.completeNow();
            })));
    }
}
