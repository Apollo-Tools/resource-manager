package at.uibk.dps.rm.service.database.deployment;

import at.uibk.dps.rm.entity.deployment.DeploymentStatusValue;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.exception.BadInputException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.testutil.SessionMockHelper;
import at.uibk.dps.rm.testutil.mockprovider.DeploymentRepositoryProviderMock;
import at.uibk.dps.rm.testutil.objectprovider.*;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.hibernate.reactive.stage.Stage.Session;
import org.hibernate.reactive.stage.Stage.SessionFactory;
import org.hibernate.reactive.util.impl.CompletionStages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
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

    private DeploymentServiceImpl deploymentService;

    private final DeploymentRepositoryProviderMock repositoryMock = new DeploymentRepositoryProviderMock();

    @Mock
    private Session session;

    @Mock
    private SessionFactory sessionFactory;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        repositoryMock.mock();
        deploymentService = new DeploymentServiceImpl(repositoryMock.getRepositoryProvider(), sessionFactory);
    }

    private static Stream<Arguments> provideResourceDeployments(ResourceDeploymentStatus rds1,
            ResourceDeploymentStatus rds2) {
        long deploymentId = 1L, accountId = 2L;
        Account account = TestAccountProvider.createAccount(accountId);
        Deployment deployment = TestDeploymentProvider.createDeployment(deploymentId, true, account);
        Resource r1 = TestResourceProvider.createResourceLambda(1L);
        Resource r2 = TestResourceProvider.createResourceContainer(2L, "localhost", true);
        Credentials c1 = TestAccountProvider.createCredentials(1L,
            r1.getMain().getRegion().getResourceProvider());
        FunctionDeployment fd1 = TestFunctionProvider.createFunctionDeployment(1L, r1, deployment, rds1);
        ServiceDeployment sd1 = TestServiceProvider.createServiceDeployment(2L, r2, deployment, rds2);

        return Stream.of(
            Arguments.of(deployment, List.of(fd1), List.of(sd1), c1, account)
        );
    }

    private static Stream<Arguments> provideEmptyResourceDeployments() {
        long deploymentId = 1L, accountId = 2L;
        Account account = TestAccountProvider.createAccount(accountId);
        Deployment deployment = TestDeploymentProvider.createDeployment(deploymentId, true, account);
        Resource r1 = TestResourceProvider.createResourceLambda(1L);
        Credentials c1 = TestAccountProvider.createCredentials(1L,
            r1.getMain().getRegion().getResourceProvider());

        return Stream.of(
            Arguments.of(deployment, List.of(), List.of(), c1, account)
        );
    }

    private static Stream<Arguments> provideValidResourceDeployment() {
        ResourceDeploymentStatus rdsDeployed = TestDeploymentProvider.createResourceDeploymentStatusDeployed();
        return provideResourceDeployments(rdsDeployed, rdsDeployed);
    }

    @ParameterizedTest
    @MethodSource("provideValidResourceDeployment")
    void cancelDeployment(Deployment deployment, List<FunctionDeployment> fdList, List<ServiceDeployment> sdList,
            Credentials c1, Account account, VertxTestContext testContext) {
        long deploymentId = deployment.getDeploymentId(), accountId = account.getAccountId();
        ResourceDeploymentStatus rdsTerminating = TestDeploymentProvider.createResourceDeploymentStatusTerminating();
        List<ResourceDeployment> resourceDeployments = new ArrayList<>();
        resourceDeployments.addAll(fdList);
        resourceDeployments.addAll(sdList);

        SessionMockHelper.mockTransaction(sessionFactory, session);
        when(repositoryMock.getDeploymentRepository().findByIdAndAccountId(session, deploymentId, accountId))
            .thenReturn(CompletionStages.completedFuture(deployment));
        when(repositoryMock.getResourceDeploymentRepository().findAllByDeploymentIdAndFetch(session, deploymentId))
            .thenReturn(CompletionStages.completedFuture(resourceDeployments));
        when(repositoryMock.getStatusRepository().findOneByStatusValue(session,
            DeploymentStatusValue.TERMINATING.getValue())).thenReturn(CompletionStages.completedFuture(rdsTerminating));
        when(repositoryMock.getCredentialsRepository().findAllByAccountId(session, accountId))
            .thenReturn(CompletionStages.completedFuture(List.of(c1)));
        when(repositoryMock.getFunctionDeploymentRepository().findAllByDeploymentId(session, deploymentId))
            .thenReturn(CompletionStages.completedFuture(fdList));
        when(repositoryMock.getServiceDeploymentRepository().findAllByDeploymentId(session, deploymentId))
            .thenReturn(CompletionStages.completedFuture(sdList));
        when(session.fetch(anyList())).thenReturn(CompletionStages.completedFuture(List.of()));

        deploymentService.cancelDeployment(deploymentId, accountId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.getJsonObject("deployment").getLong("deployment_id")).isEqualTo(1L);
                assertThat(result.getJsonArray("function_deployments").size()).isEqualTo(1);
                assertThat(result.getJsonArray("service_deployments").size()).isEqualTo(1);
                assertThat(fdList.get(0).getStatus()).isEqualTo(rdsTerminating);
                assertThat(sdList.get(0).getStatus()).isEqualTo(rdsTerminating);
                testContext.completeNow();
            })));
    }

    private static Stream<Arguments> provideBadInputResourceDeployment() {
        ResourceDeploymentStatus rdsDeployed = TestDeploymentProvider.createResourceDeploymentStatusDeployed();
        ResourceDeploymentStatus rdsNew = TestDeploymentProvider.createResourceDeploymentStatusNew();
        return Stream.concat(
            provideResourceDeployments(rdsDeployed, rdsNew),
            provideEmptyResourceDeployments()
        );
    }

    @ParameterizedTest
    @MethodSource("provideBadInputResourceDeployment")
    void cancelDeploymentBadInput(Deployment deployment, List<FunctionDeployment> fdList,
            List<ServiceDeployment> sdList, Credentials c1, Account account, VertxTestContext testContext) {
        long deploymentId = deployment.getDeploymentId(), accountId = account.getAccountId();
        List<ResourceDeployment> resourceDeployments = new ArrayList<>();
        resourceDeployments.addAll(fdList);
        resourceDeployments.addAll(sdList);

        SessionMockHelper.mockTransaction(sessionFactory, session);
        when(repositoryMock.getDeploymentRepository().findByIdAndAccountId(session, deploymentId, accountId))
            .thenReturn(CompletionStages.completedFuture(deployment));
        when(repositoryMock.getResourceDeploymentRepository().findAllByDeploymentIdAndFetch(session, deploymentId))
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
        when(repositoryMock.getDeploymentRepository().findByIdAndAccountId(session, deploymentId, accountId))
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
        ResourceDeployment rd1 = TestDeploymentProvider.createResourceDeployment(1L, d1);
        ResourceDeployment rd2 = TestDeploymentProvider.createResourceDeployment(2L, d1);
        ResourceDeployment rd3 = TestDeploymentProvider.createResourceDeployment(3L, d2);

        SessionMockHelper.mockSession(sessionFactory, session);
        when(repositoryMock.getDeploymentRepository().findAllByAccountId(session, accountId))
            .thenReturn(CompletionStages.completedFuture(List.of(d1, d2)));
        when(repositoryMock.getResourceDeploymentRepository().findAllByDeploymentIdAndFetch(session, 1L))
            .thenReturn(CompletionStages.completedFuture(List.of(rd1, rd2)));
        when(repositoryMock.getResourceDeploymentRepository().findAllByDeploymentIdAndFetch(session, 2L))
            .thenReturn(CompletionStages.completedFuture(List.of(rd3)));

        deploymentService.findAllByAccountId(accountId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(2);
                assertThat(result.getJsonObject(0).getLong("deployment_id")).isEqualTo(1L);
                assertThat(result.getJsonObject(0).getString("status_value")).isEqualTo("NEW");
                assertThat(result.getJsonObject(1).getLong("deployment_id")).isEqualTo(2L);
                assertThat(result.getJsonObject(1).getString("status_value")).isEqualTo("NEW");
                testContext.completeNow();
            })));
    }


    private static Stream<Arguments> provideStatusValue() {
        final ResourceDeploymentStatus statusNew = TestDeploymentProvider.createResourceDeploymentStatusNew();
        final ResourceDeploymentStatus statusDeployed = TestDeploymentProvider
            .createResourceDeploymentStatusDeployed();
        final ResourceDeploymentStatus statusTerminating = TestDeploymentProvider
            .createResourceDeploymentStatusTerminating();
        final ResourceDeploymentStatus statusTerminated = TestDeploymentProvider
            .createResourceDeploymentStatusTerminated();
        final ResourceDeploymentStatus statusError = TestDeploymentProvider
            .createResourceDeploymentStatusError();
        return Stream.of(
            Arguments.of(statusNew),
            Arguments.of(statusDeployed),
            Arguments.of(statusTerminating),
            Arguments.of(statusTerminated),
            Arguments.of(statusError)
        );
    }

    @ParameterizedTest
    @MethodSource("provideStatusValue")
    void checkCrucialDeploymentStatus(ResourceDeploymentStatus expectedStatus) {
        Deployment deployment = TestDeploymentProvider.createDeployment(1L);
        ResourceDeployment rd1 = TestDeploymentProvider.createResourceDeployment(1L, deployment,
            new MainResource(), TestDeploymentProvider.createResourceDeploymentStatusTerminated());
        ResourceDeployment rd2 = TestDeploymentProvider.createResourceDeployment(2L, deployment, new MainResource(),
            expectedStatus);

        DeploymentStatusValue result = deploymentService.checkCrucialResourceDeploymentStatus(List.of(rd1, rd2));

        assertThat(result.name()).isEqualTo(expectedStatus.getStatusValue());
    }

    @Test
    void findOneByIdAndAccountExists(VertxTestContext testContext) {
        long deploymentId = 1L;
        long accountId = 2L;
        Account account = TestAccountProvider.createAccount(accountId);
        Deployment deployment = TestDeploymentProvider.createDeployment(deploymentId, true, account);
        FunctionDeployment fd1 = TestFunctionProvider.createFunctionDeployment(1L, deployment);
        ServiceDeployment sd1 = TestServiceProvider.createServiceDeployment(2L, deployment);
        ServiceDeployment sd2 = TestServiceProvider.createServiceDeployment(3L, deployment);

        SessionMockHelper.mockSession(sessionFactory, session);
        when(repositoryMock.getDeploymentRepository().findByIdAndAccountId(session, deploymentId, accountId))
            .thenReturn(CompletionStages.completedFuture(deployment));
        when(repositoryMock.getFunctionDeploymentRepository().findAllByDeploymentId(session, deploymentId))
            .thenReturn(CompletionStages.completedFuture(List.of(fd1)));
        when(repositoryMock.getServiceDeploymentRepository().findAllByDeploymentId(session, deploymentId))
            .thenReturn(CompletionStages.completedFuture(List.of(sd1, sd2)));

        deploymentService.findOneByIdAndAccountId(deploymentId, accountId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.getLong("deployment_id")).isEqualTo(1L);
                assertThat(result.getJsonArray("function_resources").size()).isEqualTo(1);
                assertThat(result.getJsonArray("service_resources").size()).isEqualTo(2);
                testContext.completeNow();
            })));
    }

    @Test
    void findOneByIdAndAccountNotExists(VertxTestContext testContext) {
        long deploymentId = 1L;
        long accountId = 2L;

        SessionMockHelper.mockSession(sessionFactory, session);
        when(repositoryMock.getDeploymentRepository().findByIdAndAccountId(session, deploymentId, accountId))
            .thenReturn(CompletionStages.completedFuture(null));

        deploymentService.findOneByIdAndAccountId(deploymentId, accountId)
            .onComplete(testContext.failing(throwable -> testContext.verify(() -> {
                assertThat(throwable).isInstanceOf(NotFoundException.class);
                assertThat(throwable.getMessage()).isEqualTo("Deployment not found");
                testContext.completeNow();
            })));
    }
}
