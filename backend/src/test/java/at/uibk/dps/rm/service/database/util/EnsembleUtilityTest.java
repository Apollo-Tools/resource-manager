package at.uibk.dps.rm.service.database.util;

import at.uibk.dps.rm.entity.dto.DeployResourcesRequest;
import at.uibk.dps.rm.entity.dto.credentials.DockerCredentials;
import at.uibk.dps.rm.entity.dto.deployment.DeployResourcesDTO;
import at.uibk.dps.rm.entity.dto.deployment.FunctionResourceIds;
import at.uibk.dps.rm.entity.dto.deployment.ServiceResourceIds;
import at.uibk.dps.rm.entity.dto.resource.PlatformEnum;
import at.uibk.dps.rm.entity.dto.resource.ResourceProviderEnum;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.testutil.mockprovider.DeploymentRepositoryProviderMock;
import at.uibk.dps.rm.testutil.objectprovider.*;
import io.vertx.junit5.VertxExtension;
import org.hibernate.reactive.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

/**
 * Implements tests for the {@link EnsembleUtilityTest} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class EnsembleUtilityTest {

    private final DeploymentRepositoryProviderMock repositoryMock = new DeploymentRepositoryProviderMock();

    private final long accountId = 10L;

    @Mock
    private Stage.Session session;

    private SessionManager sessionManager;

    private Platform lambda, ec2, openFaas;
    private ResourceProvider rpAWS;
    private Region regionAWS;
    private Resource r1,r2, r3, r4;
    private Function f1, f2;
    private Service s1, s2;
    private VPC vpc;
    private DeployResourcesRequest requestDTO;
    private DeployResourcesDTO deployResourcesDTO;

    @BeforeEach
    void initTest() {
        repositoryMock.mock();
        lambda = TestPlatformProvider.createPlatformFaas(1L, PlatformEnum.LAMBDA.getValue());
        ec2 = TestPlatformProvider.createPlatformFaas(2L, PlatformEnum.EC2.getValue());
        openFaas = TestPlatformProvider.createPlatformFaas(2L, PlatformEnum.OPENFAAS.getValue());
        Platform k8s = TestPlatformProvider.createPlatformContainer(3L, PlatformEnum.K8S.getValue());
        rpAWS = TestResourceProviderProvider.createResourceProvider(1L,
            ResourceProviderEnum.AWS.getValue());
        ResourceProvider rpCustom = TestResourceProviderProvider.createResourceProvider(1L,
            ResourceProviderEnum.CUSTOM_CLOUD.getValue());
        regionAWS = TestResourceProviderProvider.createRegion(1L, "us-east-1", rpAWS);
        Region regionCustom = TestResourceProviderProvider.createRegion(1L, "custom", rpCustom);
        r1 = TestResourceProvider.createResource(1L, lambda, regionAWS);
        r2 = TestResourceProvider.createResource(2L, ec2, regionAWS);
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
        vpc = TestResourceProviderProvider.createVPC(1L, regionAWS);
        DockerCredentials dockerCredentials = TestDTOProvider.createDockerCredentials();
        requestDTO = TestRequestProvider.createDeployResourcesRequest(List.of(fri1, fri2,
            fri3), List.of(sri1, sri2, sri3), List.of(), dockerCredentials);
        deployResourcesDTO = TestRequestProvider
            .createBlankDeployRequest(dockerCredentials);
        sessionManager = new SessionManager(session);
    }
}
