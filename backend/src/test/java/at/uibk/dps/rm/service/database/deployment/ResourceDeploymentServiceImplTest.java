package at.uibk.dps.rm.service.database.deployment;

import at.uibk.dps.rm.entity.deployment.DeploymentStatusValue;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.repository.deployment.ResourceDeploymentRepository;
import at.uibk.dps.rm.testutil.objectprovider.TestFunctionProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestServiceProvider;
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
public class ResourceDeploymentServiceImplTest {

    private ResourceDeploymentService service;

    @Mock
    ResourceDeploymentRepository repository;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        service = new ResourceDeploymentServiceImpl(repository);
    }

    @Test
    void findAllByDeploymentId(VertxTestContext testContext) {
        long deploymentId = 1L;
        ResourceDeployment entity1 = TestFunctionProvider.createFunctionDeployment(4L, new Deployment());
        ResourceDeployment entity2 = TestServiceProvider.createServiceDeployment(5L, new Deployment());
        List<ResourceDeployment> resultList = new ArrayList<>();
        resultList.add(entity1);
        resultList.add(entity2);
        CompletionStage<List<ResourceDeployment>> completionStage = CompletionStages.completedFuture(resultList);
        when(repository.findAllByDeploymentId(deploymentId)).thenReturn(completionStage);

        service.findAllByDeploymentId(deploymentId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(2);

                for (int i = 0; i < 2; i++) {
                    JsonObject resultJson = result.getJsonObject(i);
                    assertThat(resultJson.getLong("resource_deployment_id")).isEqualTo(i + 4);
                    assertThat(resultJson.getJsonObject("deployment")).isNull();
                    assertThat(resultJson.getJsonObject("resource"))
                        .isNull();
                }
                testContext.completeNow();
        })));
    }

    @Test
    void findAllByDeploymentIdEmpty(VertxTestContext testContext) {
        long deploymentId = 1L;
        List<ResourceDeployment> resultList = new ArrayList<>();
        CompletionStage<List<ResourceDeployment>> completionStage = CompletionStages.completedFuture(resultList);
        when(repository.findAllByDeploymentId(deploymentId)).thenReturn(completionStage);

        service.findAllByDeploymentId(deploymentId)
                .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                    assertThat(result.size()).isEqualTo(0);
                    verify(repository).findAllByDeploymentId(deploymentId);
                    testContext.completeNow();
                })));
    }

    @Test
    void updateTriggerUrl(VertxTestContext testContext) {
        long functionResourceId = 1L;
        String triggerUrl = "url";
        CompletionStage<Integer> completionStage = CompletionStages.completedFuture(1);

        when(repository.updateTriggerUrl(functionResourceId, triggerUrl))
            .thenReturn(completionStage);

        service.updateTriggerUrl(functionResourceId, triggerUrl)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isNull();
                testContext.completeNow();
            })));
    }

    @Test
    void updateSetStatusByDeploymentId(VertxTestContext testContext) {
        long deploymentId = 1L;
        DeploymentStatusValue statusValue = DeploymentStatusValue.NEW;
        CompletionStage<Integer> completionStage = CompletionStages.completedFuture(1);

        when(repository.updateDeploymentStatusByDeploymentId(deploymentId, statusValue))
            .thenReturn(completionStage);

        service.updateSetStatusByDeploymentId(deploymentId, statusValue)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isNull();
                testContext.completeNow();
            })));
    }
}
