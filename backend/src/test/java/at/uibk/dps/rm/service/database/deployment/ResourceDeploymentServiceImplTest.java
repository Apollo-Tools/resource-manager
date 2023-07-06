package at.uibk.dps.rm.service.database.deployment;

import at.uibk.dps.rm.entity.deployment.DeploymentStatusValue;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.repository.deployment.ResourceDeploymentRepository;
import at.uibk.dps.rm.testutil.SessionMockHelper;
import at.uibk.dps.rm.testutil.objectprovider.TestFunctionProvider;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

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

    @Mock
    private Stage.SessionFactory sessionFactory;

    @Mock
    private Stage.Session session;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        service = new ResourceDeploymentServiceImpl(repository, sessionFactory);
    }

    @Test
    void findAllByDeploymentId(VertxTestContext testContext) {
        long deploymentId = 1L;
        ResourceDeployment entity1 = TestFunctionProvider.createFunctionDeployment(4L, new Deployment());
        ResourceDeployment entity2 = TestServiceProvider.createServiceDeployment(5L, new Deployment());

        SessionMockHelper.mockSession(sessionFactory, session);
        when(repository.findAllByDeploymentIdAndFetch(session, deploymentId))
            .thenReturn(CompletionStages.completedFuture(List.of(entity1, entity2)));

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
    void updateTriggerUrl(VertxTestContext testContext) {
        long functionResourceId = 1L;
        String triggerUrl = "url";

        SessionMockHelper.mockTransaction(sessionFactory, session);
        when(repository.updateTriggerUrl(session, functionResourceId, triggerUrl))
            .thenReturn(CompletionStages.completedFuture(1));

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

        SessionMockHelper.mockTransaction(sessionFactory, session);
        when(repository.updateDeploymentStatusByDeploymentId(session, deploymentId, statusValue))
            .thenReturn(CompletionStages.completedFuture(1));

        service.updateSetStatusByDeploymentId(deploymentId, statusValue)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isNull();
                testContext.completeNow();
            })));
    }
}
