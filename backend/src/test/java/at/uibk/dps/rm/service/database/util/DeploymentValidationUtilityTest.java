package at.uibk.dps.rm.service.database.util;

import at.uibk.dps.rm.entity.dto.DeployResourcesRequest;
import at.uibk.dps.rm.entity.dto.credentials.DockerCredentials;
import at.uibk.dps.rm.entity.dto.deployment.DeployResourcesDTO;
import at.uibk.dps.rm.entity.dto.deployment.FunctionResourceIds;
import at.uibk.dps.rm.entity.dto.deployment.ServiceResourceIds;
import at.uibk.dps.rm.entity.dto.resource.PlatformEnum;
import at.uibk.dps.rm.entity.dto.resource.ResourceProviderEnum;
import at.uibk.dps.rm.entity.dto.resource.ResourceTypeEnum;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.exception.BadInputException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.exception.UnauthorizedException;
import at.uibk.dps.rm.testutil.mockprovider.DeploymentRepositoryProviderMock;
import at.uibk.dps.rm.testutil.objectprovider.*;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * Implements tests for the {@link DeploymentValidationUtility} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class DeploymentValidationUtilityTest {

    private DeploymentValidationUtility utility;

    private final DeploymentRepositoryProviderMock repositoryMock = new DeploymentRepositoryProviderMock();

    private final long accountId = 10L;

    @Mock
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
        utility = new DeploymentValidationUtility(accountId,
            repositoryMock.getRepositoryProvider());
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
    }

    private void setupMocks(String testCase) {
        when(repositoryMock.getFunctionRepository()
            .findAllByIds(sessionManager, Set.of(f1.getFunctionId(),f2.getFunctionId())))
            .thenReturn(Single.just(testCase.equals("fNotFound") ? List.of(f1) : List.of(f1, f2)));
        when(repositoryMock.getServiceRepository()
            .findAllByIds(sessionManager, Set.of(s1.getServiceId(), s2.getServiceId())))
            .thenReturn(Single.just(testCase.equals("sNotFound") ? List.of(s1) : List.of(s1, s2)));
        when(repositoryMock.getResourceRepository().findAllByResourceIdsAndResourceTypes(sessionManager,
            Set.of(r3.getResourceId(), r4.getResourceId()), List.of(ResourceTypeEnum.CONTAINER.getValue())))
            .thenReturn(Single.just(testCase.equals("srNotFound") ? List.of(r3) : List.of(r3, r4)));
        when(repositoryMock.getResourceRepository().findAllByResourceIdsAndResourceTypes(sessionManager,
            Set.of(r1.getResourceId(), r2.getResourceId()), List.of(ResourceTypeEnum.FAAS.getValue())))
            .thenReturn(Single.just(testCase.equals("frNotFound") ? List.of(r3) : List.of(r3, r4)));
        if (testCase.equals("resourceLocked")) {
            r2.setLockedByDeployment(new Deployment());
        }
        when(repositoryMock.getResourceRepository().findAllByResourceIdsAndFetch(sessionManager,
            List.of(r1.getResourceId(), r2.getResourceId(), r3.getResourceId(), r4.getResourceId())))
            .thenReturn(Single.just(List.of(r1, r2, r3, r4)));
        when(repositoryMock.getCredentialsRepository()
            .findByAccountIdAndProviderId(sessionManager, accountId, rpAWS.getProviderId()))
            .thenReturn(testCase.equals("missingCloudCreds") ? Maybe.empty() :
                Maybe.just(TestAccountProvider.createCredentials(1L, rpAWS)));
        when(repositoryMock.getVpcRepository()
            .findByRegionIdAndAccountId(sessionManager, regionAWS.getRegionId(), accountId))
            .thenReturn(testCase.equals("missingVPC") ? Maybe.empty() : Maybe.just(vpc));
        if (testCase.equals("missingMetrics")) {
            when(repositoryMock.getPlatformMetricRepository()
                .countMissingRequiredMetricValuesByResourceId(eq(sessionManager), anyLong(), anyBoolean()))
                .thenReturn(Single.just(0L))
                .thenReturn(Single.just(10L));
        } else {
            when(repositoryMock.getPlatformMetricRepository()
                .countMissingRequiredMetricValuesByResourceId(eq(sessionManager), anyLong(), anyBoolean()))
                .thenReturn(Single.just(0L));
        }
    }

    @Test
    void checkDeploymentIsValidTrue(VertxTestContext testContext) {
        setupMocks("valid");
        utility.checkDeploymentIsValid(sessionManager, requestDTO, deployResourcesDTO)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.size()).isEqualTo(4);
                    assertThat(deployResourcesDTO.getVpcList().contains(vpc)).isEqualTo(true);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void checkDeploymentMissingMetrics(VertxTestContext testContext) {
        setupMocks("missingMetrics");

        utility.checkDeploymentIsValid(sessionManager, requestDTO, deployResourcesDTO)
            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    assertThat(throwable.getMessage()).isEqualTo("missing required metrics for resource (2)");
                    testContext.completeNow();
                })
            );
    }

    @Test
    void checkDeploymentMissingVPC(VertxTestContext testContext) {
        setupMocks("missingVPC");

        utility.checkDeploymentIsValid(sessionManager, requestDTO, deployResourcesDTO)
            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void checkDeploymentResourceLocked(VertxTestContext testContext) {
        setupMocks("resourceLocked");

        utility.checkDeploymentIsValid(sessionManager, requestDTO, deployResourcesDTO)
            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(BadInputException.class);
                    assertThat(throwable.getMessage()).isEqualTo("resource r2(2) is locked");
                    testContext.completeNow();
                })
            );
    }

    @ParameterizedTest
    @ValueSource(strings = {"ec2", "openfaas"})
    void checkDeploymentMissingDockerCredentials(String platform, VertxTestContext testContext) {
        deployResourcesDTO.getDeploymentCredentials().setDockerCredentials(null);
        if (platform.equals("ec2")) {
            r2.getMain().setPlatform(ec2);
        } else {
            r2.getMain().setPlatform(openFaas);
        }
        setupMocks("missingDockerCreds");

        utility.checkDeploymentIsValid(sessionManager, requestDTO, deployResourcesDTO)
            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(UnauthorizedException.class);
                    assertThat(throwable.getMessage()).isEqualTo("missing docker credentials for " + platform);
                    testContext.completeNow();
                })
            );
    }

    @ParameterizedTest
    @ValueSource(strings = {"ec2", "lambda"})
    void checkDeploymentMissingCloudCredentials(String platform, VertxTestContext testContext) {
        if (platform.equals("ec2")) {
            r1.getMain().setPlatform(ec2);
        } else {
            r1.getMain().setPlatform(lambda);
        }
        setupMocks("missingCloudCreds");

        utility.checkDeploymentIsValid(sessionManager, requestDTO, deployResourcesDTO)
            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(UnauthorizedException.class);
                    assertThat(throwable.getMessage()).isEqualTo("missing credentials for " + platform);
                    testContext.completeNow();
                })
            );
    }

    @ParameterizedTest
    @ValueSource(strings = {"frNotFound", "srNotFound", "sNotFound", "fNotFound"})
    void checkDeploymentEntityNotFound(String testCase, VertxTestContext testContext) {
        setupMocks(testCase);

        utility.checkDeploymentIsValid(sessionManager, requestDTO, deployResourcesDTO)
            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }
}
