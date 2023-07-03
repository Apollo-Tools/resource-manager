package at.uibk.dps.rm.service.database.deployment;

import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.repository.deployment.DeploymentRepository;
import at.uibk.dps.rm.repository.deployment.ResourceDeploymentRepository;
import at.uibk.dps.rm.repository.deployment.ResourceDeploymentStatusRepository;
import at.uibk.dps.rm.testutil.objectprovider.TestAccountProvider;
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

import java.util.List;
import java.util.concurrent.CompletionStage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Implements tests for the {@link DeploymentServiceImpl} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class DeploymentServiceImplTest {

    private DeploymentService deploymentService;

    @Mock
    private DeploymentRepository deploymentRepository;

    @Mock
    private ResourceDeploymentRepository resourceDeploymentRepository;

    @Mock
    private ResourceDeploymentStatusRepository statusRepository;

    @Mock
    private Stage.SessionFactory sessionFactory;

    @Mock
    private Stage.Session session;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        deploymentService = new DeploymentServiceImpl(deploymentRepository, resourceDeploymentRepository,
            statusRepository, sessionFactory);
    }

    @Test
    void findAllByAccountId(VertxTestContext testContext) {
        long accountId = 1L;
        Account account = TestAccountProvider.createAccount(accountId);
        Deployment r1 = TestDeploymentProvider.createDeployment(1L, true, account);
        Deployment r2 = TestDeploymentProvider.createDeployment(2L, true, account);
        Deployment r3 = TestDeploymentProvider.createDeployment(3L, true, account);
        CompletionStage<List<Deployment>> completionStage = CompletionStages.completedFuture(List.of(r1, r2, r3));

        when(deploymentRepository.findAllByAccountId(session, accountId)).thenReturn(completionStage);

        deploymentService.findAllByAccountId(accountId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(3);
                assertThat(result.getJsonObject(0).getLong("deployment_id")).isEqualTo(1L);
                assertThat(result.getJsonObject(1).getLong("deployment_id")).isEqualTo(2L);
                assertThat(result.getJsonObject(2).getLong("deployment_id")).isEqualTo(3L);
                testContext.completeNow();
            })));
    }

    @Test
    void findOneByIdAndAccountExists(VertxTestContext testContext) {
        long deploymentId = 1L;
        long accountId = 2L;
        Account account = TestAccountProvider.createAccount(accountId);
        Deployment entity = TestDeploymentProvider.createDeployment(deploymentId, true, account);

        CompletionStage<Deployment> completionStage = CompletionStages.completedFuture(entity);
        when(deploymentRepository.findByIdAndAccountId(session, deploymentId, accountId)).thenReturn(completionStage);

        deploymentService.findOneByIdAndAccountId(deploymentId, accountId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.getLong("deployment_id")).isEqualTo(1L);
                testContext.completeNow();
            })));
    }

    @Test
    void findOneByIdAndAccountNotExists(VertxTestContext testContext) {
        long deploymentId = 1L;
        long accountId = 2L;
        CompletionStage<Deployment> completionStage = CompletionStages.completedFuture(null);

        when(deploymentRepository.findByIdAndAccountId(session, deploymentId, accountId)).thenReturn(completionStage);

        deploymentService.findOneByIdAndAccountId(deploymentId, accountId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isNull();
                testContext.completeNow();
            })));
    }
}
