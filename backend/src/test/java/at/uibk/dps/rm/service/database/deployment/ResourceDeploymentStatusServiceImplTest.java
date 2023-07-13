package at.uibk.dps.rm.service.database.deployment;

import at.uibk.dps.rm.entity.deployment.DeploymentStatusValue;
import at.uibk.dps.rm.entity.model.ResourceDeploymentStatus;
import at.uibk.dps.rm.repository.deployment.ResourceDeploymentStatusRepository;
import at.uibk.dps.rm.testutil.SessionMockHelper;
import at.uibk.dps.rm.testutil.objectprovider.TestDeploymentProvider;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.hibernate.reactive.stage.Stage;
import org.hibernate.reactive.util.impl.CompletionStages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Implements tests for the {@link ResourceDeploymentStatusServiceImpl} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class ResourceDeploymentStatusServiceImplTest {

    private ResourceDeploymentStatusService service;

    @Mock
    private ResourceDeploymentStatusRepository repository;

    @Mock
    private Stage.SessionFactory sessionFactory;

    @Mock
    private Stage.Session session;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        service = new ResourceDeploymentStatusServiceImpl(repository, sessionFactory);
    }

    @Test
    void findOneByStatusValue(VertxTestContext testContext) {
        String statusValue = DeploymentStatusValue.NEW.name();
        ResourceDeploymentStatus status = TestDeploymentProvider.createResourceDeploymentStatusNew();

        SessionMockHelper.mockSession(sessionFactory, session);
        when(repository.findOneByStatusValue(session, statusValue))
            .thenReturn(CompletionStages.completedFuture(status));

        service.findOneByStatusValue(statusValue)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.getString("status_value")).isEqualTo("NEW");
                testContext.completeNow();
            })));
    }


}
