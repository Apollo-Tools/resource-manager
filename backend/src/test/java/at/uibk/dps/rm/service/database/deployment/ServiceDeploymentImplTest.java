package at.uibk.dps.rm.service.database.deployment;

import at.uibk.dps.rm.entity.deployment.DeploymentStatusValue;
import at.uibk.dps.rm.entity.model.Deployment;
import at.uibk.dps.rm.entity.model.ServiceDeployment;
import at.uibk.dps.rm.repository.deployment.ServiceDeploymentRepository;
import at.uibk.dps.rm.testutil.objectprovider.TestServiceProvider;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.hibernate.reactive.stage.Stage;
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
 * Implements tests for the {@link ServiceDeploymentServiceImpl} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class ServiceDeploymentImplTest {

    private ServiceDeploymentService service;

    @Mock
    ServiceDeploymentRepository repository;

    @Mock
    private Stage.SessionFactory sessionFactory;

    @Mock
    private Stage.Session session;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        service = new ServiceDeploymentServiceImpl(repository, sessionFactory);
    }

    @Test
    void findAllByDeploymentId(VertxTestContext testContext) {
        long deploymentId = 1L;
        ServiceDeployment entity1 = TestServiceProvider.createServiceDeployment(4L, new Deployment());
        ServiceDeployment entity2 = TestServiceProvider.createServiceDeployment(5L, new Deployment());
        List<ServiceDeployment> resultList = new ArrayList<>();
        resultList.add(entity1);
        resultList.add(entity2);
        CompletionStage<List<ServiceDeployment>> completionStage = CompletionStages.completedFuture(resultList);
        when(repository.findAllByDeploymentId(session, deploymentId)).thenReturn(completionStage);

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
        long deploymentId = 1L;
        List<ServiceDeployment> resultList = new ArrayList<>();
        CompletionStage<List<ServiceDeployment>> completionStage = CompletionStages.completedFuture(resultList);
        when(repository.findAllByDeploymentId(session, deploymentId)).thenReturn(completionStage);

        service.findAllByDeploymentId(deploymentId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(0);
                testContext.completeNow();
            })));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void existsReadyForContainerStartupAndTermination(boolean exists, VertxTestContext testContext) {
        long deploymentId = 1L, resourceDeploymentId = 2L, accountId = 3L;
        CompletionStage<ServiceDeployment> completionStage = CompletionStages.completedFuture(exists ?
            new ServiceDeployment() : null);

        when(repository.findOneByDeploymentStatus(session, deploymentId, resourceDeploymentId, accountId,
            DeploymentStatusValue.DEPLOYED)).thenReturn(completionStage);

        service.existsReadyForContainerStartupAndTermination(deploymentId, resourceDeploymentId, accountId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isEqualTo(exists);
                testContext.completeNow();
            })));
    }
}
