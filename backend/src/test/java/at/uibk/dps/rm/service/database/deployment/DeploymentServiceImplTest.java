package at.uibk.dps.rm.service.database.deployment;

import at.uibk.dps.rm.entity.deployment.DeploymentStatusValue;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.exception.BadInputException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.repository.deployment.DeploymentRepository;
import at.uibk.dps.rm.repository.deployment.ResourceDeploymentRepository;
import at.uibk.dps.rm.repository.deployment.ResourceDeploymentStatusRepository;
import at.uibk.dps.rm.testutil.SessionMockHelper;
import at.uibk.dps.rm.testutil.objectprovider.TestAccountProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestDeploymentProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestResourceProvider;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.hibernate.reactive.stage.Stage;
import org.hibernate.reactive.util.impl.CompletionStages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.stream.Stream;

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
    void cancelDeployment(VertxTestContext testContext) {
        long deploymentId = 1L, accountId = 2L;
        Account account = TestAccountProvider.createAccount(accountId);
        Deployment deployment = TestDeploymentProvider.createDeployment(deploymentId, true, account);
        Resource r = TestResourceProvider.createResource(1L);
        ResourceDeploymentStatus rdsDeployed = TestDeploymentProvider.createResourceDeploymentStatusDeployed();
        ResourceDeploymentStatus rdsTerminating = TestDeploymentProvider.createResourceDeploymentStatusTerminating();
        ResourceDeployment rd = TestDeploymentProvider.createResourceDeployment(12L, deployment, r, rdsDeployed);

        SessionMockHelper.mockTransaction(sessionFactory, session);
        when(deploymentRepository.findByIdAndAccountId(session, deploymentId, accountId))
            .thenReturn(CompletionStages.completedFuture(deployment));
        when(resourceDeploymentRepository.findAllByDeploymentIdAndFetch(session, deploymentId))
            .thenReturn(CompletionStages.completedFuture(List.of(rd)));
        when(statusRepository.findOneByStatusValue(session, DeploymentStatusValue.TERMINATING.getValue()))
            .thenReturn(CompletionStages.completedFuture(rdsTerminating));

        deploymentService.cancelDeployment(deploymentId, accountId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.getLong("deployment_id")).isEqualTo(1L);
                assertThat(result.getJsonObject("created_by")).isNull();
                assertThat(rd.getStatus()).isEqualTo(rdsTerminating);
                testContext.completeNow();
            })));
    }

    private static Stream<Arguments> provideResourceDeployments() {
        long deploymentId = 1L, accountId = 2L;
        ResourceDeploymentStatus rdsDeployed = TestDeploymentProvider.createResourceDeploymentStatusDeployed();
        ResourceDeploymentStatus rdsNew = TestDeploymentProvider.createResourceDeploymentStatusNew();
        Account account = TestAccountProvider.createAccount(accountId);
        Deployment deployment = TestDeploymentProvider.createDeployment(deploymentId, true, account);
        Resource r = TestResourceProvider.createResource(1L);
        ResourceDeployment rd1 = TestDeploymentProvider.createResourceDeployment(12L, deployment, r, rdsNew);
        ResourceDeployment rd2 = TestDeploymentProvider.createResourceDeployment(12L, deployment, r, rdsDeployed);

        return Stream.of(
            Arguments.of(List.of(rd1)),
            Arguments.of(List.of(rd1, rd2))
        );
    }

    @ParameterizedTest
    @MethodSource("provideResourceDeployments")
    void cancelDeploymentBadInput(List<ResourceDeployment> resourceDeployments, VertxTestContext testContext) {
        long deploymentId = 1L, accountId = 2L;
        Account account = TestAccountProvider.createAccount(accountId);
        Deployment deployment = TestDeploymentProvider.createDeployment(deploymentId, true, account);

        SessionMockHelper.mockTransaction(sessionFactory, session);
        when(deploymentRepository.findByIdAndAccountId(session, deploymentId, accountId))
            .thenReturn(CompletionStages.completedFuture(deployment));
        when(resourceDeploymentRepository.findAllByDeploymentIdAndFetch(session, deploymentId))
            .thenReturn(CompletionStages.completedFuture(resourceDeployments));

        deploymentService.cancelDeployment(deploymentId, accountId)
            .onComplete(testContext.failing(throwable -> testContext.verify(() -> {
                assertThat(throwable).isInstanceOf(BadInputException.class);
                assertThat(throwable.getMessage()).isEqualTo("invalid deployment state");
                testContext.completeNow();
            })));
    }

    @Test
    void cancelDeploymentNotFound(VertxTestContext testContext) {
        long deploymentId = 1L, accountId = 2L;

        SessionMockHelper.mockTransaction(sessionFactory, session);
        when(deploymentRepository.findByIdAndAccountId(session, deploymentId, accountId))
            .thenReturn(CompletionStages.completedFuture(null));

        deploymentService.cancelDeployment(deploymentId, accountId)
            .onComplete(testContext.failing(throwable -> testContext.verify(() -> {
                assertThat(throwable).isInstanceOf(NotFoundException.class);
                assertThat(throwable.getMessage()).isEqualTo("Deployment not found");
                testContext.completeNow();
            })));
    }

    @Test
    void findAllByAccountId(VertxTestContext testContext) {
        long accountId = 1L;
        Account account = TestAccountProvider.createAccount(accountId);
        Deployment d1 = TestDeploymentProvider.createDeployment(1L, true, account);
        Deployment d2 = TestDeploymentProvider.createDeployment(2L, true, account);
        Deployment d3 = TestDeploymentProvider.createDeployment(3L, true, account);

        SessionMockHelper.mockSession(sessionFactory, session);
        when(deploymentRepository.findAllByAccountId(session, accountId))
            .thenReturn(CompletionStages.completedFuture(List.of(d1, d2, d3)));

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

        SessionMockHelper.mockSession(sessionFactory, session);
        when(deploymentRepository.findByIdAndAccountId(session, deploymentId, accountId))
            .thenReturn(CompletionStages.completedFuture(entity));

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

        SessionMockHelper.mockSession(sessionFactory, session);
        when(deploymentRepository.findByIdAndAccountId(session, deploymentId, accountId))
            .thenReturn(CompletionStages.completedFuture(null));

        deploymentService.findOneByIdAndAccountId(deploymentId, accountId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isNull();
                testContext.completeNow();
            })));
    }
}
