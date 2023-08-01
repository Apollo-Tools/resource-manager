package at.uibk.dps.rm.service.database.deployment;

import at.uibk.dps.rm.entity.deployment.DeploymentStatusValue;
import at.uibk.dps.rm.entity.model.ServiceDeployment;
import at.uibk.dps.rm.repository.deployment.ServiceDeploymentRepository;
import at.uibk.dps.rm.testutil.SessionMockHelper;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.hibernate.reactive.stage.Stage;
import org.hibernate.reactive.util.impl.CompletionStages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void existsReadyForContainerStartupAndTermination(boolean exists, VertxTestContext testContext) {
        long deploymentId = 1L, resourceDeploymentId = 2L, accountId = 3L;
        CompletionStage<ServiceDeployment> completionStage = CompletionStages.completedFuture(exists ?
            new ServiceDeployment() : null);

        SessionMockHelper.mockSession(sessionFactory, session);
        when(repository.findOneByDeploymentStatus(session, deploymentId, resourceDeploymentId, accountId,
            DeploymentStatusValue.DEPLOYED)).thenReturn(completionStage);

        service.existsReadyForContainerStartupAndTermination(deploymentId, resourceDeploymentId, accountId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isEqualTo(exists);
                testContext.completeNow();
            })));
    }
}
