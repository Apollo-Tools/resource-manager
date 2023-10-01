package at.uibk.dps.rm.service.database.util;

import at.uibk.dps.rm.entity.deployment.DeploymentStatusValue;
import at.uibk.dps.rm.entity.dto.deployment.*;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.testutil.mockprovider.DeploymentRepositoryProviderMock;
import at.uibk.dps.rm.testutil.objectprovider.*;
import io.reactivex.rxjava3.core.Single;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

/**
 * Implements tests for the {@link DeploymentUtility} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class DeploymentUtilityTest {

    private DeploymentUtility utility;

    private final DeploymentRepositoryProviderMock repositoryMock = new DeploymentRepositoryProviderMock();

    @Mock
    private SessionManager sessionManager;

    private long deploymentId;
    private Deployment deployment;
    private FunctionDeployment fd1, fd2;
    private ServiceDeployment sd1, sd2;
    private ResourceDeployment rd1, rd2;

    @BeforeEach
    void initTest() {
        repositoryMock.mock();
        utility = new DeploymentUtility(repositoryMock.getRepositoryProvider());
        deploymentId = 1L;
        Account account = TestAccountProvider.createAccount(2L);
        deployment = TestDeploymentProvider.createDeployment(deploymentId, true, account);
        Resource r1 = TestResourceProvider.createResourceLambda(1L);
        Resource r2 = TestResourceProvider.createResourceContainer(2L, "localhost", true);
        ResourceDeploymentStatus rdsDeployed = TestDeploymentProvider.createResourceDeploymentStatusDeployed();
        ResourceDeploymentStatus rdsTerminating = TestDeploymentProvider.createResourceDeploymentStatusTerminating();
        fd1 = TestFunctionProvider.createFunctionDeployment(1L, r1, deployment, rdsDeployed);
        fd2 = TestFunctionProvider.createFunctionDeployment(2L, r1, deployment, rdsTerminating);
        sd1 = TestServiceProvider.createServiceDeployment(3L, r2, deployment, rdsDeployed);
        sd2 = TestServiceProvider.createServiceDeployment(4L, r2, deployment, rdsTerminating);
        rd1 = TestDeploymentProvider.createResourceDeployment(1L, deployment);
        rd2 = TestDeploymentProvider.createResourceDeployment(2L, deployment);
    }

    @ParameterizedTest
    @ValueSource(strings = {"new", "deployed", "terminating", "terminated", "error"})
    void composeDeploymentResponse(String type, VertxTestContext testContext) {
        DeploymentStatusValue status;
        switch (type) {
            case "new":
                status = DeploymentStatusValue.NEW;
                break;
            case "deployed":
                status = DeploymentStatusValue.DEPLOYED;
                break;
            case "terminating":
                status = DeploymentStatusValue.TERMINATING;
                break;
            case "terminated":
                status = DeploymentStatusValue.TERMINATED;
                break;
            case "error":
            default:
                status = DeploymentStatusValue.ERROR;
                break;
        }
        List<ResourceDeployment> resourceDeployments = List.of(rd1, rd2);
        when(repositoryMock.getResourceDeploymentRepository()
            .findAllByDeploymentIdAndFetch(sessionManager, deploymentId)).thenReturn(Single.just(resourceDeployments));
        try(MockedStatic<DeploymentStatusUtility> mock = Mockito.mockStatic(DeploymentStatusUtility.class)) {
            mock.when(() -> DeploymentStatusUtility.checkCrucialResourceDeploymentStatus(resourceDeployments))
                .thenReturn(status);
            utility.composeDeploymentResponse(sessionManager, deployment)
                .subscribe(result -> testContext.verify(() -> {
                        assertThat(result.getDeploymentId()).isEqualTo(deploymentId);
                        assertThat(result.getStatusValue()).isEqualTo(status);
                        testContext.completeNow();
                    }),
                    throwable -> testContext.verify(() -> fail("method has thrown exception"))
                );
        }
    }

    @Test
    void mapResourceDeploymentsToDTO(VertxTestContext testContext) {
        DeployTerminateDTO deployTerminateDTO = TestRequestProvider
            .createDeployTerminateDTOWithoutResourceDeployments(deployment);
        when(repositoryMock.getFunctionDeploymentRepository().findAllByDeploymentId(sessionManager, deploymentId))
            .thenReturn(Single.just(List.of(fd1, fd2)));
        when(repositoryMock.getServiceDeploymentRepository().findAllByDeploymentId(sessionManager, deploymentId))
            .thenReturn(Single.just(List.of(sd1, sd2)));
        doReturn(Single.just(sd1.getService().getEnvVars()))
            .doReturn(Single.just(sd1.getService().getVolumeMounts()))
            .doReturn(Single.just(sd2.getService().getEnvVars()))
            .doReturn(Single.just(sd2.getService().getVolumeMounts()))
            .when(sessionManager).fetch(any());
        utility.mapResourceDeploymentsToDTO(sessionManager, deployTerminateDTO)
            .subscribe(() -> testContext.verify(() -> {
                    assertThat(deployTerminateDTO.getFunctionDeployments().size()).isEqualTo(2);
                    assertThat(deployTerminateDTO.getFunctionDeployments().get(0)).isEqualTo(fd1);
                    assertThat(deployTerminateDTO.getFunctionDeployments().get(1)).isEqualTo(fd2);
                    assertThat(deployTerminateDTO.getServiceDeployments().size()).isEqualTo(2);
                    assertThat(deployTerminateDTO.getServiceDeployments().get(0)).isEqualTo(sd1);
                    assertThat(deployTerminateDTO.getServiceDeployments().get(1)).isEqualTo(sd2);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }
}
