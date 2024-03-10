package at.uibk.dps.rm.service.database.util;

import at.uibk.dps.rm.entity.dto.DeployResourcesRequest;
import at.uibk.dps.rm.entity.dto.credentials.DockerCredentials;
import at.uibk.dps.rm.entity.dto.deployment.FunctionResourceIds;
import at.uibk.dps.rm.entity.dto.deployment.ServiceResourceIds;
import at.uibk.dps.rm.entity.dto.resource.PlatformEnum;
import at.uibk.dps.rm.entity.dto.resource.ResourceProviderEnum;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.exception.UnauthorizedException;
import at.uibk.dps.rm.repository.function.FunctionRepository;
import at.uibk.dps.rm.repository.service.ServiceRepository;
import at.uibk.dps.rm.testutil.objectprovider.*;
import io.reactivex.rxjava3.core.Maybe;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
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

    @Mock
    private SessionManager sessionManager;

    @Mock
    private FunctionRepository functionRepository;

    @Mock
    private ServiceRepository serviceRepository;

    private long accountId;
    private Deployment deployment;
    private DeployResourcesRequest requestDTO;
    private ResourceDeploymentStatus status;
    private Resource r3, r4;
    private Function f1, f2;
    private Service s1, s2;
    private List<Resource> resources;
    private K8sNamespace n1, n2;

    @BeforeEach
    void initTest() {
        accountId = 1L;
        utility = new SaveResourceDeploymentUtility(accountId, functionRepository, serviceRepository);
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
        f1 = TestFunctionProvider.createFunction(1L);
        f2 = TestFunctionProvider.createFunction(2L);
        FunctionResourceIds fri1 = TestFunctionProvider.createFunctionResourceIds(f1.getFunctionId(), 1L);
        FunctionResourceIds fri2 = TestFunctionProvider.createFunctionResourceIds(f2.getFunctionId(), 1L);
        FunctionResourceIds fri3 = TestFunctionProvider.createFunctionResourceIds(f2.getFunctionId(), 2L);
        s1 = TestServiceProvider.createService(1L);
        s2 = TestServiceProvider.createService(2L);
        ServiceResourceIds sri1 = TestServiceProvider.createServiceResourceIds(s1.getServiceId(), 3L);
        ServiceResourceIds sri2 = TestServiceProvider.createServiceResourceIds(s2.getServiceId(), 3L);
        ServiceResourceIds sri3 = TestServiceProvider.createServiceResourceIds(s2.getServiceId(), 4L);
        DockerCredentials dockerCredentials = TestDTOProvider.createDockerCredentials();
        requestDTO = TestRequestProvider.createDeployResourcesRequest(List.of(fri1, fri2,
            fri3), List.of(sri1, sri2, sri3), List.of(), dockerCredentials);
        status = TestDeploymentProvider.createResourceDeploymentStatusNew();
        resources = List.of(r1, r2, r3, r4);
        n1 = TestResourceProviderProvider.createNamespace(1L, r3);
        n2 = TestResourceProviderProvider.createNamespace(2L, r4);
    }

    @Test
    void saveFunctionDeployments(VertxTestContext testContext) {
        when(functionRepository.findByIdAndAccountId(sessionManager, f1.getFunctionId(), accountId, true))
            .thenReturn(Maybe.just(f1));
        when(functionRepository.findByIdAndAccountId(sessionManager, f2.getFunctionId(), accountId, true))
            .thenReturn(Maybe.just(f2));

        utility.saveFunctionDeployments(sessionManager, deployment, requestDTO, status, resources)
            .blockingSubscribe(testContext::completeNow,
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void saveFunctionDeploymentsFunctionNotFound(VertxTestContext testContext) {
        when(functionRepository.findByIdAndAccountId(sessionManager, f1.getFunctionId(), accountId, true))
            .thenReturn(Maybe.just(f1));
        when(functionRepository.findByIdAndAccountId(sessionManager, f2.getFunctionId(), accountId, true))
            .thenReturn(Maybe.empty());

        utility.saveFunctionDeployments(sessionManager, deployment, requestDTO, status, resources)
            .blockingSubscribe(() -> testContext.failNow("method did not throw exception"),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    assertThat(throwable.getMessage()).isEqualTo("Function not found");
                    testContext.completeNow();
                })
            );
    }

    @Test
    void saveFunctionDeploymentsEmpty(VertxTestContext testContext) {
        requestDTO.setFunctionResources(List.of());

        utility.saveFunctionDeployments(sessionManager, deployment, requestDTO, status, resources)
            .blockingSubscribe(testContext::completeNow,
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void saveServiceDeployments(VertxTestContext testContext) {
        when(serviceRepository.findByIdAndAccountId(sessionManager, s1.getServiceId(), accountId, true))
            .thenReturn(Maybe.just(s1));
        when(serviceRepository.findByIdAndAccountId(sessionManager, s2.getServiceId(), accountId, true))
            .thenReturn(Maybe.just(s2));

        utility.saveServiceDeployments(sessionManager, deployment, requestDTO, status, List.of(n1, n2), resources)
            .blockingSubscribe(testContext::completeNow,
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void saveServiceDeploymentsServiceNotFound(VertxTestContext testContext) {
        K8sNamespace n1 = TestResourceProviderProvider.createNamespace(1L, r3);
        K8sNamespace n2 = TestResourceProviderProvider.createNamespace(2L, r4);

        when(serviceRepository.findByIdAndAccountId(sessionManager, s1.getServiceId(), accountId, true))
            .thenReturn(Maybe.just(s1));
        when(serviceRepository.findByIdAndAccountId(sessionManager, s2.getServiceId(), accountId, true))
            .thenReturn(Maybe.empty());

        utility.saveServiceDeployments(sessionManager, deployment, requestDTO, status, List.of(n1, n2), resources)
            .blockingSubscribe(() -> testContext.failNow("method did not throw exception"),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    assertThat(throwable.getMessage()).isEqualTo("Service not found");
                    testContext.completeNow();
                })
            );
    }

    @Test
    void saveServiceDeploymentsEmpty(VertxTestContext testContext) {
        requestDTO.setServiceResources(List.of());

        utility.saveServiceDeployments(sessionManager, deployment, requestDTO, status, List.of(), resources)
            .blockingSubscribe(testContext::completeNow,
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void saveServiceDeploymentsMissingNamespace(VertxTestContext testContext) {
        when(serviceRepository.findByIdAndAccountId(sessionManager, s1.getServiceId(), accountId, true))
            .thenReturn(Maybe.just(s1));
        when(serviceRepository.findByIdAndAccountId(sessionManager, s2.getServiceId(), accountId, true))
            .thenReturn(Maybe.just(s2));

        utility.saveServiceDeployments(sessionManager, deployment, requestDTO, status, List.of(n1), resources)
            .blockingSubscribe(() -> testContext.failNow("method did not throw exception"),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(UnauthorizedException.class);
                    assertThat(throwable.getMessage()).isEqualTo("missing namespace for resource r4 (4)");
                    testContext.completeNow();
                })
            );
    }

}
