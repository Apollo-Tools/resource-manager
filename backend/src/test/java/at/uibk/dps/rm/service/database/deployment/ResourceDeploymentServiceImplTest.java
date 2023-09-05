package at.uibk.dps.rm.service.database.deployment;

import at.uibk.dps.rm.entity.deployment.DeploymentStatusValue;
import at.uibk.dps.rm.repository.deployment.ResourceDeploymentRepository;
import at.uibk.dps.rm.testutil.SessionMockHelper;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.reactivex.rxjava3.core.Completable;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import at.uibk.dps.rm.service.database.util.SessionManager;
import org.hibernate.reactive.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    
    private final SessionManager sessionManager = new SessionManager(session);

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        service = new ResourceDeploymentServiceImpl(repository, sessionFactory);
    }

    @Test
    void updateSetStatusByDeploymentId(VertxTestContext testContext) {
        long deploymentId = 1L;
        DeploymentStatusValue statusValue = DeploymentStatusValue.NEW;

        SessionMockHelper.mockTransaction(sessionFactory, sessionManager);
        when(repository.updateDeploymentStatusByDeploymentId(sessionManager, deploymentId, statusValue))
            .thenReturn(Completable.complete());

        service.updateStatusByDeploymentId(deploymentId, statusValue,
            testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isNull();
                testContext.completeNow();
            })));
    }
}
