package at.uibk.dps.rm.service.database.deployment;

import at.uibk.dps.rm.entity.model.FunctionDeployment;
import at.uibk.dps.rm.entity.model.Deployment;
import at.uibk.dps.rm.repository.deployment.FunctionDeploymentRepository;
import at.uibk.dps.rm.testutil.objectprovider.TestFunctionProvider;
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
import static org.mockito.Mockito.when;

/**
 * Implements tests for the {@link FunctionDeploymentServiceImpl} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class FunctionDeploymentServiceImplTest {

    private FunctionDeploymentService service;

    @Mock
    FunctionDeploymentRepository repository;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        service = new FunctionDeploymentServiceImpl(repository);
    }

    @Test
    void findAllByDeploymentId(VertxTestContext testContext) {
        long deploymentId = 1L;
        FunctionDeployment entity1 = TestFunctionProvider.createFunctionDeployment(4L, new Deployment());
        FunctionDeployment entity2 = TestFunctionProvider.createFunctionDeployment(5L, new Deployment());
        List<FunctionDeployment> resultList = new ArrayList<>();
        resultList.add(entity1);
        resultList.add(entity2);
        CompletionStage<List<FunctionDeployment>> completionStage = CompletionStages.completedFuture(resultList);
        when(repository.findAllByDeploymentId(deploymentId)).thenReturn(completionStage);

        service.findAllByDeploymentId(deploymentId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(2);

                for (int i = 0; i < 2; i++) {
                    JsonObject resultJson = result.getJsonObject(i);
                    assertThat(resultJson.getLong("resource_deployment_id")).isEqualTo(i + 4);
                    assertThat(resultJson.getJsonObject("deployment")).isNull();
                }
                testContext.completeNow();
            })));
    }

    @Test
    void findAllByDeploymentIdEmpty(VertxTestContext testContext) {
        long resourceId = 1L;
        List<FunctionDeployment> resultList = new ArrayList<>();
        CompletionStage<List<FunctionDeployment>> completionStage = CompletionStages.completedFuture(resultList);
        when(repository.findAllByDeploymentId(resourceId)).thenReturn(completionStage);

        service.findAllByDeploymentId(resourceId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(0);
                testContext.completeNow();
            })));
    }
}
