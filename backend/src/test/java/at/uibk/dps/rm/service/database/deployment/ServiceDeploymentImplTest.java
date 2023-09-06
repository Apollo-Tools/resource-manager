package at.uibk.dps.rm.service.database.deployment;

import at.uibk.dps.rm.entity.deployment.DeploymentStatusValue;
import at.uibk.dps.rm.repository.deployment.ServiceDeploymentRepository;
import at.uibk.dps.rm.testutil.SessionMockHelper;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.reactivex.rxjava3.core.Single;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import at.uibk.dps.rm.service.database.util.SessionManager;
import org.hibernate.reactive.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    
    private SessionManager sessionManager;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        service = new ServiceDeploymentServiceImpl(repository, sessionFactory);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void existsReadyForContainerStartupAndTermination(boolean exists, VertxTestContext testContext) {
        long deploymentId = 1L, resourceDeploymentId = 2L, accountId = 3L;
        Single<Long> single = Single.just(exists ? 1L : 0L);

        sessionManager = SessionMockHelper.mockTransaction(sessionFactory, session);
        when(repository.countByDeploymentStatus(sessionManager, deploymentId, resourceDeploymentId, accountId,
            DeploymentStatusValue.DEPLOYED)).thenReturn(single);

        service.existsReadyForContainerStartupAndTermination(deploymentId, resourceDeploymentId, accountId, testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isEqualTo(exists);
                testContext.completeNow();
            })));
    }
}
