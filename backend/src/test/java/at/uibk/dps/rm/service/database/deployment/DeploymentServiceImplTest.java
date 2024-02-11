package at.uibk.dps.rm.service.database.deployment;

import at.uibk.dps.rm.entity.deployment.DeploymentStatusValue;
import at.uibk.dps.rm.entity.deployment.output.DeploymentOutput;
import at.uibk.dps.rm.entity.dto.DeployResourcesRequest;
import at.uibk.dps.rm.entity.dto.deployment.DeployResourcesDTO;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.exception.BadInputException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.exception.UnauthorizedException;
import at.uibk.dps.rm.service.database.util.*;
import at.uibk.dps.rm.testutil.SessionMockHelper;
import at.uibk.dps.rm.testutil.mockprovider.DatabaseUtilMockprovider;
import at.uibk.dps.rm.testutil.mockprovider.DeploymentRepositoryProviderMock;
import at.uibk.dps.rm.testutil.objectprovider.*;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

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
    private SessionManagerProvider smProvider;

    @Mock
    private SessionManager sessionManager;

    private long accountId, deploymentId;
    private Account account;
    private Deployment deployment;
    private Resource r1, r2;
    private Credentials c1;
    private FunctionDeployment fd1, fd2;
    private ServiceDeployment sd1, sd2;
    private ResourceDeploymentStatus rdsNew;
    private K8sNamespace n1;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        repositoryMock.mock();
        deploymentService = new DeploymentServiceImpl(repositoryMock.getRepositoryProvider(), smProvider);
        deploymentId = 1L;
        accountId = 2L;
        account = TestAccountProvider.createAccount(accountId);
        deployment = TestDeploymentProvider.createDeployment(deploymentId, account);
        r1 = TestResourceProvider.createResourceLambda(1L);
        r2 = TestResourceProvider.createResourceContainer(2L, "localhost", true);
        c1 = TestAccountProvider.createCredentials(1L,
            r1.getMain().getRegion().getResourceProvider());
        ResourceDeploymentStatus rdsDeployed = TestDeploymentProvider.createResourceDeploymentStatusDeployed();
        ResourceDeploymentStatus rdsTerminating = TestDeploymentProvider.createResourceDeploymentStatusTerminating();
        rdsNew = TestDeploymentProvider.createResourceDeploymentStatusNew();
        fd1 = TestFunctionProvider.createFunctionDeployment(1L, r1, deployment, rdsDeployed);
        fd2 = TestFunctionProvider.createFunctionDeployment(2L, r1, deployment, rdsTerminating);
        sd1 = TestServiceProvider.createServiceDeployment(3L, r2, deployment, rdsDeployed);
        sd2 = TestServiceProvider.createServiceDeployment(4L, r2, deployment, rdsTerminating);
        n1 = TestResourceProviderProvider.createNamespace(1L);
    }

    @Test
    void cancelDeployment(VertxTestContext testContext) {
        long deploymentId = deployment.getDeploymentId(), accountId = account.getAccountId();
        ResourceDeploymentStatus rdsTerminating = TestDeploymentProvider.createResourceDeploymentStatusTerminating();
        List<ResourceDeployment> resourceDeployments = new ArrayList<>();
        resourceDeployments.add(fd1);
        resourceDeployments.add(sd1);

        SessionMockHelper.mockSingle(smProvider, sessionManager);
        when(repositoryMock.getDeploymentRepository().findByIdAndAccountId(sessionManager, deploymentId, accountId))
            .thenReturn(Maybe.just(deployment));
        when(repositoryMock.getResourceDeploymentRepository().findAllByDeploymentIdAndFetch(sessionManager, deploymentId))
            .thenReturn(Single.just(resourceDeployments));
        when(repositoryMock.getStatusRepository().findOneByStatusValue(sessionManager,
            DeploymentStatusValue.TERMINATING.getValue())).thenReturn(Maybe.just(rdsTerminating));
        when(repositoryMock.getCredentialsRepository().findAllByAccountId(sessionManager, accountId))
            .thenReturn(Single.just(List.of(c1)));

        try(MockedConstruction<DeploymentUtility> ignored = DatabaseUtilMockprovider.mockDeploymentUtility(sessionManager)) {
            deploymentService.cancelDeployment(deploymentId, accountId, testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.getJsonObject("deployment").getLong("deployment_id")).isEqualTo(1L);
                assertThat(result.getJsonArray("credentials_list").size()).isEqualTo(1);
                assertThat(result.getJsonArray("function_deployments").size()).isEqualTo(0);
                assertThat(result.getJsonArray("service_deployments").size()).isEqualTo(0);
                assertThat(fd1.getStatus()).isEqualTo(rdsTerminating);
                assertThat(sd1.getStatus()).isEqualTo(rdsTerminating);
                testContext.completeNow();
            })));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"badStatus", "empty"})
    void cancelDeploymentBadInput(String type, VertxTestContext testContext) {
        long deploymentId = deployment.getDeploymentId(), accountId = account.getAccountId();
        List<ResourceDeployment> resourceDeployments = new ArrayList<>();
        if (type.equals("badStatus")) {
            resourceDeployments.add(fd2);
            resourceDeployments.add(sd2);
        }

        SessionMockHelper.mockSingle(smProvider, sessionManager);
        when(repositoryMock.getDeploymentRepository().findByIdAndAccountId(sessionManager, deploymentId, accountId))
            .thenReturn(Maybe.just(deployment));
        when(repositoryMock.getResourceDeploymentRepository().findAllByDeploymentIdAndFetch(sessionManager, deploymentId))
            .thenReturn(Single.just(resourceDeployments));
        when(repositoryMock.getCredentialsRepository().findAllByAccountId(sessionManager, accountId))
            .thenReturn(Single.just(List.of()));

        deploymentService.cancelDeployment(deploymentId, accountId, testContext.failing(throwable -> testContext.verify(() -> {
                assertThat(throwable).isInstanceOf(BadInputException.class);
                assertThat(throwable.getMessage()).isEqualTo("invalid deployment state");
                testContext.completeNow();
            })));
    }

    @Test
    void cancelDeploymentNotFound(VertxTestContext testContext) {
        SessionMockHelper.mockSingle(smProvider, sessionManager);
        when(repositoryMock.getDeploymentRepository().findByIdAndAccountId(sessionManager, deploymentId, accountId))
            .thenReturn(Maybe.empty());

        deploymentService.cancelDeployment(deploymentId, accountId, testContext.failing(throwable -> testContext.verify(() -> {
                assertThat(throwable).isInstanceOf(NotFoundException.class);
                assertThat(throwable.getMessage()).isEqualTo("Deployment not found");
                testContext.completeNow();
            })));
    }

    @Test
    void findAllByAccountId(VertxTestContext testContext) {
        Deployment d2 = TestDeploymentProvider.createDeployment(2L, account);

        SessionMockHelper.mockSingle(smProvider, sessionManager);
        when(repositoryMock.getDeploymentRepository().findAllByAccountId(sessionManager, accountId))
            .thenReturn(Single.just(List.of(deployment, d2)));
        when(repositoryMock.getResourceDeploymentRepository().findAllByDeploymentIdAndFetch(sessionManager, 1L))
            .thenReturn(Single.just(List.of(fd1, sd1, sd2)));
        when(repositoryMock.getResourceDeploymentRepository().findAllByDeploymentIdAndFetch(sessionManager, 2L))
            .thenReturn(Single.just(List.of(fd2)));

        deploymentService.findAllByAccountId(accountId, testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(2);
                assertThat(result.getJsonObject(0).getLong("deployment_id")).isEqualTo(1L);
                assertThat(result.getJsonObject(0).getString("status_value")).isEqualTo("TERMINATING");
                assertThat(result.getJsonObject(1).getLong("deployment_id")).isEqualTo(2L);
                assertThat(result.getJsonObject(1).getString("status_value")).isEqualTo("TERMINATING");
                testContext.completeNow();
            })));
    }

    @Test
    void findOneByIdAndAccountExists(VertxTestContext testContext) {
        Deployment deploymentSpy = spy(deployment);

        SessionMockHelper.mockSingle(smProvider, sessionManager);
        when(repositoryMock.getDeploymentRepository().findByIdAndAccountId(sessionManager, deploymentId, accountId))
            .thenReturn(Maybe.just(deploymentSpy));
        when(repositoryMock.getFunctionDeploymentRepository().findAllByDeploymentId(sessionManager, deploymentId))
            .thenReturn(Single.just(List.of(fd1)));
        when(repositoryMock.getServiceDeploymentRepository().findAllByDeploymentId(sessionManager, deploymentId))
            .thenReturn(Single.just(List.of(sd1, sd2)));
        when(deploymentSpy.getCreatedAt()).thenReturn(new Timestamp(1692667639304L));
        when(deploymentSpy.getFinishedAt()).thenReturn(new Timestamp(1692667639999L));

        deploymentService.findOneByIdAndAccountId(deploymentId, accountId, testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.getLong("deployment_id")).isEqualTo(1L);
                assertThat(result.getJsonArray("function_resources").size()).isEqualTo(1);
                assertThat(result.getJsonArray("service_resources").size()).isEqualTo(2);
                assertThat(result.getLong("created_at")).isEqualTo(1692667639304L);
                assertThat(result.getLong("finished_at")).isEqualTo(1692667639999L);
                testContext.completeNow();
            })));
    }

    @Test
    void findOneByIdAndAccountNotExists(VertxTestContext testContext) {
        SessionMockHelper.mockSingle(smProvider, sessionManager);
        when(repositoryMock.getDeploymentRepository().findByIdAndAccountId(sessionManager, deploymentId, accountId))
            .thenReturn(Maybe.empty());

        deploymentService.findOneByIdAndAccountId(deploymentId, accountId, testContext.failing(throwable -> testContext.verify(() -> {
                assertThat(throwable).isInstanceOf(NotFoundException.class);
                testContext.completeNow();
            })));
    }

    @Test
    void saveToAccount(VertxTestContext testContext) {
        DeployResourcesRequest request = TestRequestProvider.createDeployResourcesRequest();
        SessionMockHelper.mockSingle(smProvider, sessionManager);
        when(sessionManager.find(Account.class, accountId)).thenReturn(Maybe.just(account));
        when(sessionManager.persist(argThat((Deployment depl) ->
            depl.getCreatedBy().equals(account)))).thenReturn(Single.just(deployment));
        when(sessionManager.flush()).thenReturn(Completable.complete());
        when(repositoryMock.getStatusRepository().findOneByStatusValue(sessionManager,
            DeploymentStatusValue.NEW.name())).thenReturn(Maybe.just(rdsNew));
        when(repositoryMock.getCredentialsRepository().findAllByAccountId(sessionManager, accountId))
            .thenReturn(Single.just(List.of(c1)));
        when(repositoryMock.getNamespaceRepository().findAllByAccountIdAndFetch(sessionManager, accountId))
            .thenReturn(Single.just(List.of(n1)));

        try(MockedConstruction<DeploymentValidationUtility> ignoreValidation =
                    DatabaseUtilMockprovider.mockDeploymentValidationUtilityValid(sessionManager, List.of(r1, r2));
            MockedConstruction<SaveResourceDeploymentUtility> ignoreSave = DatabaseUtilMockprovider
                    .mockSaveResourceDeploymentUtility(sessionManager, rdsNew, List.of(n1), List.of(r1, r2));
            MockedConstruction<DeploymentUtility> ignoreDeployment = DatabaseUtilMockprovider
                    .mockDeploymentUtility(sessionManager);
            MockedConstruction<LockedResourcesUtility> ignoreLock = DatabaseUtilMockprovider
                .mockLockUtilityLockResources(sessionManager, List.of())) {
            deploymentService.saveToAccount(accountId, JsonObject.mapFrom(request),
                testContext.succeeding(result -> testContext.verify(() -> {
                    DeployResourcesDTO resultDTO = result.mapTo(DeployResourcesDTO.class);
                    assertThat(resultDTO.getDeployment()).isNotNull();
                    assertThat(resultDTO.getDeployment().getCreatedBy()).isNull();
                    assertThat(resultDTO.getCredentialsList()).isEqualTo(List.of(c1));
                    assertThat(resultDTO.getDeploymentCredentials().getDockerCredentials()).isEqualTo(TestDTOProvider.createDockerCredentials());
                    testContext.completeNow();
                })));
        }
    }

    @Test
    void saveToAccountDeploymentStatusNotFound(VertxTestContext testContext) {
        DeployResourcesRequest request = TestRequestProvider.createDeployResourcesRequest();
        SessionMockHelper.mockSingle(smProvider, sessionManager);
        when(sessionManager.find(Account.class, accountId)).thenReturn(Maybe.just(account));
        when(sessionManager.persist(argThat((Deployment depl) ->
            depl.getCreatedBy().equals(account)))).thenReturn(Single.just(deployment));
        when(sessionManager.flush()).thenReturn(Completable.complete());
        when(repositoryMock.getStatusRepository().findOneByStatusValue(sessionManager,
            DeploymentStatusValue.NEW.name())).thenReturn(Maybe.empty());

        try(MockedConstruction<DeploymentValidationUtility> ignoreValidation =
                    DatabaseUtilMockprovider.mockDeploymentValidationUtilityValid(sessionManager, List.of(r1, r2))) {
            deploymentService.saveToAccount(accountId, JsonObject.mapFrom(request),
                testContext.failing(throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })));
        }
    }

    @Test
    void saveToAccountDeploymentAccountNotFound(VertxTestContext testContext) {
        DeployResourcesRequest request = TestRequestProvider.createDeployResourcesRequest();
        SessionMockHelper.mockSingle(smProvider, sessionManager);
        when(sessionManager.find(Account.class, accountId)).thenReturn(Maybe.empty());

        try(MockedConstruction<DeploymentValidationUtility> ignoreValidation =
                    DatabaseUtilMockprovider.mockDeploymentValidationUtilityValid(sessionManager, List.of(r1, r2))) {
            deploymentService.saveToAccount(accountId, JsonObject.mapFrom(request),
                testContext.failing(throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(UnauthorizedException.class);
                    testContext.completeNow();
                })));
        }
    }

    @Test
    void handleDeploymentError(VertxTestContext testContext) {
        String errorMessage = "deployment error";
        SessionMockHelper.mockCompletable(smProvider, sessionManager);
        when(repositoryMock.getResourceDeploymentRepository().updateDeploymentStatusByDeploymentId(sessionManager,
            fd1.getResourceDeploymentId(), DeploymentStatusValue.ERROR)).thenReturn(Completable.complete());
        when(sessionManager.persist(argThat((Object object) -> {
            if (!(object instanceof Log)) {
                return false;
            }
            Log log = (Log) object;
            return log.getLogValue().equals(errorMessage);
        }))).thenReturn(Single.just(new Log()));
        when(sessionManager.persist(argThat((Object object) -> {
            if (!(object instanceof DeploymentLog)) {
                return false;
            }
            DeploymentLog deploymentLog = (DeploymentLog) object;
            return deploymentLog.getDeployment().equals(deployment);
        }))).thenReturn(Single.just(new DeploymentLog()));

        try(MockedConstruction<LockedResourcesUtility> ignore =
                DatabaseUtilMockprovider.mockLockUtilityUnlockResources(sessionManager, deploymentId)) {
            deploymentService.handleDeploymentError(deploymentId, errorMessage,
                testContext.succeeding(result -> testContext.verify(testContext::completeNow)));
        }
    }

    @Test
    void handleDeploymentSuccessful(VertxTestContext testContext) {
        DeploymentOutput deploymentOutput = TestDeploymentProvider.createDeploymentOutput("java");
        DeployResourcesDTO deployResourcesDTO = TestRequestProvider.createDeployRequest();

        SessionMockHelper.mockCompletable(smProvider, sessionManager);
        when(repositoryMock.getResourceDeploymentRepository().updateDeploymentStatusByDeploymentId(sessionManager,
            deploymentId, DeploymentStatusValue.DEPLOYED)).thenReturn(Completable.complete());
        when(repositoryMock.getDeploymentRepository().setDeploymentFinishedTime(sessionManager, deploymentId))
            .thenReturn(Completable.complete());
        when(sessionManager.flush()).thenReturn(Completable.complete());

        try(MockedConstruction<TriggerUrlUtility> ignored = DatabaseUtilMockprovider.mockTriggerUrlUtility(sessionManager)) {
            deploymentService.handleDeploymentSuccessful(JsonObject.mapFrom(deploymentOutput), deployResourcesDTO,
                testContext.succeeding(result -> testContext.verify(testContext::completeNow)));
        }
    }
}
