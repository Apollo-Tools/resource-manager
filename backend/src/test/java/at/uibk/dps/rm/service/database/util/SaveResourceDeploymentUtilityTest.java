package at.uibk.dps.rm.service.database.util;

import at.uibk.dps.rm.entity.dto.DeployResourcesRequest;
import at.uibk.dps.rm.entity.dto.credentials.DockerCredentials;
import at.uibk.dps.rm.entity.dto.deployment.FunctionResourceIds;
import at.uibk.dps.rm.entity.dto.deployment.ServiceResourceIds;
import at.uibk.dps.rm.entity.dto.resource.PlatformEnum;
import at.uibk.dps.rm.entity.dto.resource.ResourceProviderEnum;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.exception.UnauthorizedException;
import at.uibk.dps.rm.testutil.mockprovider.DeploymentRepositoryProviderMock;
import at.uibk.dps.rm.testutil.objectprovider.*;
import io.reactivex.rxjava3.core.Completable;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.hibernate.reactive.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Implements tests for the {@link SaveResourceDeploymentUtility} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class SaveResourceDeploymentUtilityTest {

    private SaveResourceDeploymentUtility utility;

    private final DeploymentRepositoryProviderMock repositoryMock = new DeploymentRepositoryProviderMock();

    @Mock
    private Stage.Session session;

    private SessionManager sessionManager;

    private Deployment deployment;

    private DeployResourcesRequest requestDTO;

    private ResourceDeploymentStatus status;

    private Resource r3, r4;

    List<Resource> resources;

    @BeforeEach
    void initTest() {
        repositoryMock.mock();
        utility = new SaveResourceDeploymentUtility(repositoryMock.getRepositoryProvider());
        deployment = TestDeploymentProvider.createDeployment(1L);
        Platform lambda = TestPlatformProvider.createPlatformFaas(1L, PlatformEnum.LAMBDA.getValue());
        Platform ec2 = TestPlatformProvider.createPlatformFaas(2L, PlatformEnum.EC2.getValue());
        Platform k8s = TestPlatformProvider.createPlatformContainer(3L, PlatformEnum.K8S.getValue());
        ResourceProvider rpAWS = TestResourceProviderProvider.createResourceProvider(1L,
            ResourceProviderEnum.AWS.getValue());
        ResourceProvider rpCustom = TestResourceProviderProvider.createResourceProvider(1L,
            ResourceProviderEnum.CUSTOM_CLOUD.getValue());
        Region regionAWS = TestResourceProviderProvider.createRegion(1L, "us-east-1", rpAWS);
        Region regionCustom = TestResourceProviderProvider.createRegion(1L, "custom", rpCustom);
        Resource r1 = TestResourceProvider.createResource(1L, lambda, regionAWS);
        Resource r2 = TestResourceProvider.createResource(2L, ec2, regionAWS);
        r3 = TestResourceProvider.createResource(3L, k8s, regionCustom);
        r4 = TestResourceProvider.createResource(4L, k8s, regionCustom);
        Function f1 = TestFunctionProvider.createFunction(1L);
        Function f2 = TestFunctionProvider.createFunction(2L);
        FunctionResourceIds fri1 = TestFunctionProvider.createFunctionResourceIds(f1.getFunctionId(), 1L);
        FunctionResourceIds fri2 = TestFunctionProvider.createFunctionResourceIds(f2.getFunctionId(), 1L);
        FunctionResourceIds fri3 = TestFunctionProvider.createFunctionResourceIds(f2.getFunctionId(), 2L);
        Service s1 = TestServiceProvider.createService(1L);
        Service s2 = TestServiceProvider.createService(2L);
        ServiceResourceIds sri1 = TestServiceProvider.createServiceResourceIds(s1.getServiceId(), 3L);
        ServiceResourceIds sri2 = TestServiceProvider.createServiceResourceIds(s2.getServiceId(), 3L);
        ServiceResourceIds sri3 = TestServiceProvider.createServiceResourceIds(s2.getServiceId(), 4L);
        DockerCredentials dockerCredentials = TestDTOProvider.createDockerCredentials();
        requestDTO = TestRequestProvider.createDeployResourcesRequest(List.of(fri1, fri2,
            fri3), List.of(sri1, sri2, sri3), List.of(), dockerCredentials);
        status = TestDeploymentProvider.createResourceDeploymentStatusNew();
        resources = List.of(r1, r2, r3, r4);
    }

    @Test
    void saveFunctionDeployments(VertxTestContext testContext) {
        sessionManager = new SessionManager(session);
        when(repositoryMock.getFunctionDeploymentRepository().createAll(eq(sessionManager), any()))
            .thenReturn(Completable.complete());

        utility.saveFunctionDeployments(sessionManager, deployment, requestDTO, status, resources)
            .blockingSubscribe(() -> testContext.verify(testContext::completeNow),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void saveFunctionDeploymentsEmpty(VertxTestContext testContext) {
        sessionManager = new SessionManager(session);
        requestDTO.setFunctionResources(List.of());

        utility.saveFunctionDeployments(sessionManager, deployment, requestDTO, status, resources)
            .blockingSubscribe(() -> testContext.verify(testContext::completeNow),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void saveServiceDeployments(VertxTestContext testContext) {
        sessionManager = new SessionManager(session);
        K8sNamespace n1 = TestResourceProviderProvider.createNamespace(1L, r3);
        K8sNamespace n2 = TestResourceProviderProvider.createNamespace(2L, r4);

        when(repositoryMock.getServiceDeploymentRepository().createAll(eq(sessionManager), any()))
            .thenReturn(Completable.complete());

        utility.saveServiceDeployments(sessionManager, deployment, requestDTO, status, List.of(n1, n2), resources)
            .blockingSubscribe(() -> testContext.verify(testContext::completeNow),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void saveServiceDeploymentsEmpty(VertxTestContext testContext) {
        sessionManager = new SessionManager(session);
        requestDTO.setServiceResources(List.of());

        utility.saveServiceDeployments(sessionManager, deployment, requestDTO, status, List.of(), resources)
            .blockingSubscribe(() -> testContext.verify(testContext::completeNow),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void saveServiceDeploymentsMissingNamespace(VertxTestContext testContext) {
        sessionManager = new SessionManager(session);
        K8sNamespace n1 = TestResourceProviderProvider.createNamespace(1L, r3);

        when(repositoryMock.getServiceRepository().createAll(eq(sessionManager), any()))
            .thenReturn(Completable.complete());

        utility.saveServiceDeployments(sessionManager, deployment, requestDTO, status, List.of(n1), resources)
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did not exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(UnauthorizedException.class);
                    assertThat(throwable.getMessage()).isEqualTo("missing namespace for resource r4 (4)");
                    testContext.completeNow();
                })
            );
    }

}
