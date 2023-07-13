package at.uibk.dps.rm.handler.deployment;

import at.uibk.dps.rm.entity.deployment.DeploymentStatusValue;
import at.uibk.dps.rm.entity.dto.DeployResourcesRequest;
import at.uibk.dps.rm.entity.dto.credentials.DockerCredentials;
import at.uibk.dps.rm.entity.dto.deployment.FunctionResourceIds;
import at.uibk.dps.rm.entity.dto.deployment.ServiceResourceIds;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.exception.BadInputException;
import at.uibk.dps.rm.exception.DeploymentTerminationFailedException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.exception.UnauthorizedException;
import at.uibk.dps.rm.handler.deploymentexecution.DeploymentExecutionHandler;
import at.uibk.dps.rm.testutil.RoutingContextMockHelper;
import at.uibk.dps.rm.testutil.objectprovider.*;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.ext.web.RoutingContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * Implements tests for the {@link DeploymentHandler} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class DeploymentHandlerTest {

    private DeploymentHandler deploymentHandler;

    @Mock
    private DeploymentChecker deploymentChecker;

    @Mock
    private ResourceDeploymentChecker resourceDeploymentChecker;

    @Mock
    private FunctionDeploymentChecker functionDeploymentChecker;

    @Mock
    private ServiceDeploymentChecker serviceDeploymentChecker;

    @Mock
    private ResourceDeploymentStatusChecker statusChecker;

    @Mock
    private DeploymentExecutionHandler deploymentExecutionHandler;

    @Mock
    private DeploymentErrorHandler deploymentErrorHandler;

    @Mock
    private DeploymentPreconditionHandler preconditionChecker;

    @Mock
    private RoutingContext rc;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        deploymentHandler = new DeploymentHandler(deploymentChecker, resourceDeploymentChecker,
            functionDeploymentChecker, serviceDeploymentChecker, statusChecker, deploymentExecutionHandler,
            deploymentErrorHandler, preconditionChecker);
    }

    @ParameterizedTest
    @ValueSource(strings = {"valid", "frEmpty", "srEmpty", "deploymentNotFound"})
    void getOneExists(String testCase, VertxTestContext testContext) {
        long deploymentId = 1L;
        Account account = TestAccountProvider.createAccount(1L);
        Deployment deployment = TestDeploymentProvider.createDeployment(1L, true, account);
        JsonArray functionDeployments = new JsonArray(TestDeploymentProvider
            .createFunctionDeploymentsJson(deployment));
        if (testCase.equals("frEmpty")) {
            functionDeployments = new JsonArray();
        }
        JsonArray serviceDeployments = new JsonArray(TestDeploymentProvider
            .createServiceDeploymentsJson(deployment));
        if (testCase.equals("srEmpty")) {
            serviceDeployments = new JsonArray();
        }

        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        when(rc.pathParam("id")).thenReturn(String.valueOf(deploymentId));
        when(deploymentChecker.checkFindOne(deploymentId, account.getAccountId()))
            .thenReturn(testCase.equals("deploymentNotFound") ? Single.error(NotFoundException::new) :
                Single.just(JsonObject.mapFrom(deployment)));
        if (!testCase.equals("deploymentNotFound")) {
            when(functionDeploymentChecker.checkFindAllByDeploymentId(deploymentId))
                .thenReturn(Single.just(functionDeployments));
            when(serviceDeploymentChecker.checkFindAllByDeploymentId(deploymentId))
                .thenReturn(Single.just(serviceDeployments));
        }

        deploymentHandler.getOne(rc)
            .subscribe(result -> testContext.verify(() -> {
                if (!testCase.equals("deploymentNotFound")) {
                    assertThat(result.getJsonArray("function_resources").size())
                        .isEqualTo(testCase.equals("frEmpty") ? 0 : 3);
                    assertThat(result.getJsonArray("service_resources").size())
                        .isEqualTo(testCase.equals("srEmpty") ? 0 : 3);
                } else {
                    fail("method did not throw exception");
                }
                    testContext.completeNow();
                }),throwable -> testContext.verify(() -> {
                    if (testCase.equals("deploymentNotFound")) {
                        assertThat(throwable).isInstanceOf(NotFoundException.class);
                    } else {
                        fail("method did not throw exception");
                    }
                    testContext.completeNow();
                })
            );
    }

    @ParameterizedTest
    @ValueSource(strings = {"valid", "empty"})
    void getAll(String testCase, VertxTestContext testContext) {
        Account account = TestAccountProvider.createAccount(1L);
        Deployment r1 = TestDeploymentProvider.createDeployment(1L, true, account);
        Deployment r2 = TestDeploymentProvider.createDeployment(2L, true, account);
        Deployment r3 = TestDeploymentProvider.createDeployment(3L, true, account);
        ResourceDeploymentStatus rrsNew = TestDeploymentProvider.createResourceDeploymentStatusNew();
        ResourceDeploymentStatus rrsError = TestDeploymentProvider.createResourceDeploymentStatusError();
        ResourceDeploymentStatus rrsDeployed = TestDeploymentProvider.createResourceDeploymentStatusDeployed();
        Resource resource1 = TestResourceProvider.createResource(1L);
        Resource resource2 = TestResourceProvider.createResource(2L);
        ResourceDeployment rr1 = TestDeploymentProvider.createResourceDeployment(1L, r1, resource1, rrsNew);
        ResourceDeployment rr2 = TestDeploymentProvider.createResourceDeployment(2L, r1, resource2, rrsNew);
        Resource resource3 = TestResourceProvider.createResource(3L);
        ResourceDeployment rr3 = TestDeploymentProvider.createResourceDeployment(3L, r2, resource3, rrsError);
        Resource resource4 = TestResourceProvider.createResource(4L);
        ResourceDeployment rr4 = TestDeploymentProvider.createResourceDeployment(4L, r3, resource4, rrsDeployed);
        JsonArray deployments = new JsonArray(List.of(JsonObject.mapFrom(r1),
            JsonObject.mapFrom(r2), JsonObject.mapFrom(r3)));
        JsonArray rr12Json = new JsonArray(List.of(JsonObject.mapFrom(rr1), JsonObject.mapFrom(rr2)));
        JsonArray rr3Json = new JsonArray(List.of(JsonObject.mapFrom(rr3)));
        JsonArray rr4Json = new JsonArray(List.of(JsonObject.mapFrom(rr4)));

        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        if (testCase.equals("valid")) {
            when(resourceDeploymentChecker.checkFindAllByDeploymentId(r1.getDeploymentId()))
                .thenReturn(Single.just(rr12Json));
            when(resourceDeploymentChecker.checkFindAllByDeploymentId(r2.getDeploymentId()))
                .thenReturn(Single.just(rr3Json));
            when(resourceDeploymentChecker.checkFindAllByDeploymentId(r3.getDeploymentId()))
                .thenReturn(Single.just(rr4Json));
            when(resourceDeploymentChecker.checkCrucialResourceDeploymentStatus(rr12Json))
                .thenReturn(DeploymentStatusValue.NEW);
            when(resourceDeploymentChecker.checkCrucialResourceDeploymentStatus(rr3Json))
                .thenReturn(DeploymentStatusValue.TERMINATED);
            when(resourceDeploymentChecker.checkCrucialResourceDeploymentStatus(rr4Json))
                .thenReturn(DeploymentStatusValue.ERROR);
        } else {
            deployments = new JsonArray();
        }
        when(deploymentChecker.checkFindAll(account.getAccountId())).thenReturn(Single.just(deployments));

        deploymentHandler.getAll(rc)
            .subscribe(result -> testContext.verify(() -> {
                if (testCase.equals("valid")) {
                    assertThat(result.size()).isEqualTo(3);
                    assertThat(result.getJsonObject(0).getLong("deployment_id")).isEqualTo(1L);
                    assertThat(result.getJsonObject(0).getString("status_value")).isEqualTo("NEW");
                    assertThat(result.getJsonObject(1).getLong("deployment_id")).isEqualTo(2L);
                    assertThat(result.getJsonObject(1).getString("status_value")).isEqualTo("TERMINATED");
                    assertThat(result.getJsonObject(2).getLong("deployment_id")).isEqualTo(3L);
                    assertThat(result.getJsonObject(2).getString("status_value")).isEqualTo("ERROR");
                } else {
                    assertThat(result.size()).isEqualTo(0);
                }
                testContext.completeNow();
            }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @ParameterizedTest
    @ValueSource(strings = {"valid", "deploymentFailed", "preConNotMet"})
    void postOneValid(String testCase, VertxTestContext testContext) {
        ResourceProvider aws = TestResourceProviderProvider.createResourceProvider(1L, "aws");
        Region reg1 = TestResourceProviderProvider.createRegion(1L, "us-east-1", aws);
        Region reg2 = TestResourceProviderProvider.createRegion(2L, "us-west-1", aws);

        Resource r1 = TestResourceProvider.createResourceLambda(1L, reg1,250.0, 612.0);
        Resource r2 = TestResourceProvider.createResourceEC2(2L, reg2, 150.0, 512.0,
            "t2.micro");
        Resource r3 = TestResourceProvider.createResourceOpenFaas(3L, reg2,250.0, 512.0,
            "http://localhost:8080", "user", "pw");
        Resource r4 = TestResourceProvider.createResourceContainer(4L, reg1,"https://localhost", true);
        JsonArray resources = new JsonArray(List.of(JsonObject.mapFrom(r1), JsonObject.mapFrom(r2),
            JsonObject.mapFrom(r3), JsonObject.mapFrom(r4)));
        List<FunctionResourceIds> fids = TestFunctionProvider.createFunctionResourceIdsList(r1.getResourceId(),
            r2.getResourceId(), r3.getResourceId());
        List<ServiceResourceIds> sids = TestServiceProvider.createServiceResourceIdsList(r4.getResourceId());
        DockerCredentials dockerCredentials = TestDTOProvider.createDockerCredentials();
        String kubeconfig = TestDTOProvider.createKubeConfigValue();
        DeployResourcesRequest request = TestRequestProvider.createDeployResourcesRequest(fids, sids,
            dockerCredentials);
        JsonObject requestBody = JsonObject.mapFrom(request);
        Account account = TestAccountProvider.createAccount(1L);
        Deployment deployment = TestDeploymentProvider.createDeployment(1L, true, account);
        JsonObject deploymentJson = JsonObject.mapFrom(deployment);
        ResourceDeploymentStatus statusNew = TestDeploymentProvider.createResourceDeploymentStatusNew();


        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        RoutingContextMockHelper.mockBody(rc, requestBody);
        when(preconditionChecker.checkDeploymentIsValid(request, account.getAccountId(), new ArrayList<>()))
            .thenReturn(testCase.equals("preConNotMet") ? Single.error(UnauthorizedException::new) :
                Single.just(resources));
        if (!testCase.equals("preConNotMet")) {
            when(deploymentChecker.submitCreateDeployment(account.getAccountId()))
                .thenReturn(Single.just(deploymentJson));
            when(statusChecker.checkFindOneByStatusValue(DeploymentStatusValue.NEW.name()))
                .thenReturn(Single.just(JsonObject.mapFrom(statusNew)));
            when(functionDeploymentChecker.submitCreateAll(any())).thenReturn(Completable.complete());
            when(serviceDeploymentChecker.submitCreateAll(any())).thenReturn(Completable.complete());
            when(deploymentExecutionHandler.deployResources(deployment, account.getAccountId(), dockerCredentials, kubeconfig,
                new ArrayList<>()))
                .thenReturn(testCase.equals("deploymentFailed") ?
                    Completable.error(DeploymentTerminationFailedException::new) : Completable.complete());
        }
        if (testCase.equals("deploymentFailed")) {
            when(deploymentErrorHandler.onDeploymentError(eq(account.getAccountId()), eq(deployment),
                any())).thenReturn(Completable.complete());
        } else if (testCase.equals("valid")){
            when(resourceDeploymentChecker.submitUpdateStatus(deployment.getDeploymentId(), DeploymentStatusValue.DEPLOYED))
                .thenReturn(Completable.complete());
        }

        deploymentHandler.postOne(rc)
            .subscribe(result -> testContext.verify(() -> {
                assertThat(result.getLong("deployment_id")).isEqualTo(1L);
                assertThat(result.getBoolean("is_active")).isTrue();
                testContext.completeNow();
            }), throwable -> testContext.verify(() -> {
                if (!testCase.equals("preConNotMet")) {
                    fail("method has thrown exception");
                } else {
                    assertThat(throwable).isInstanceOf(UnauthorizedException.class);
                }
                testContext.completeNow();
            }));
    }

    @ParameterizedTest
    @ValueSource(strings = {"noNamespace", "invalidClusterUrl", "invalidContext", "unsupportedSchema"})
    void postOneDifferentKubeConfigs(String testCase, VertxTestContext testContext) {
        Resource r1 = TestResourceProvider.createResourceContainer(4L, "https://localhost", true);
        JsonArray resources = new JsonArray(List.of(JsonObject.mapFrom(r1)));
        List<ServiceResourceIds> sids = TestServiceProvider.createServiceResourceIdsList(r1.getResourceId());
        String kubeconfig;
        switch (testCase) {
            case "noNamespace":
                kubeconfig = TestDTOProvider.createKubeConfigValueNoNamespace();
                break;
            case "invalidClusterUrl":
                kubeconfig = TestDTOProvider.createKubeConfigValue("x.x.x.x");
                break;
            case "invalidContext":
                kubeconfig = TestDTOProvider.createKubeConfigValueNoMatchingKubeContext();
                break;
            case "unsupportedSchema":
                kubeconfig = "kubeconfig";
                break;
            default:
                kubeconfig = "";
        }
        DeployResourcesRequest request = TestRequestProvider.createDeployResourcesRequest(List.of(), sids, null,
            kubeconfig);
        JsonObject requestBody = JsonObject.mapFrom(request);
        Account account = TestAccountProvider.createAccount(1L);
        Deployment deployment = TestDeploymentProvider.createDeployment(1L, true, account);
        JsonObject deploymentJson = JsonObject.mapFrom(deployment);
        ResourceDeploymentStatus statusNew = TestDeploymentProvider.createResourceDeploymentStatusNew();

        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        RoutingContextMockHelper.mockBody(rc, requestBody);
        when(preconditionChecker.checkDeploymentIsValid(request, account.getAccountId(), new ArrayList<>()))
            .thenReturn(Single.just(resources));
        when(deploymentChecker.submitCreateDeployment(account.getAccountId()))
            .thenReturn(Single.just(deploymentJson));
        when(statusChecker.checkFindOneByStatusValue(DeploymentStatusValue.NEW.name()))
            .thenReturn(Single.just(JsonObject.mapFrom(statusNew)));
        if (testCase.equals("noNamespace")) {
            when(functionDeploymentChecker.submitCreateAll(any()))
                .thenReturn(Completable.error(InputMismatchException::new));
            when(serviceDeploymentChecker.submitCreateAll(any()))
                .thenReturn(Completable.error(InputMismatchException::new));
        }

        deploymentHandler.postOne(rc)
            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")
            ),throwable -> testContext.verify(() -> {
                if (testCase.equals("noNamespace")) {
                    assertThat(throwable).isInstanceOf(InputMismatchException.class);
                } else if (testCase.equals("unsupportedSchema")) {
                    assertThat(throwable).isInstanceOf(BadInputException.class);
                    assertThat(throwable.getMessage()).isEqualTo("Unsupported schema of kube config");
                } else {
                    assertThat(throwable).isInstanceOf(UnauthorizedException.class);
                }
                testContext.completeNow();
            }));
    }

    @Test
    void updateOneValid(VertxTestContext testContext) {
        long deploymentId = 1L;
        Account account = TestAccountProvider.createAccount(1L);
        Deployment deployment = TestDeploymentProvider.createDeployment(1L, true, account);
        JsonObject deploymentJson = JsonObject.mapFrom(deployment);

        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        when(rc.pathParam("id")).thenReturn(String.valueOf(deploymentId));
        when(deploymentChecker.submitCancelDeployment(deploymentId, account.getAccountId()))
            .thenReturn(Single.just(deploymentJson));
        when(deploymentChecker.checkFindOne(deploymentId, account.getAccountId()))
            .thenReturn(Single.just(deploymentJson));
        when(deploymentExecutionHandler.terminateResources(deployment, account.getAccountId()))
            .thenReturn(Completable.complete());
        when(resourceDeploymentChecker.submitUpdateStatus(deploymentId, DeploymentStatusValue.TERMINATED))
            .thenReturn(Completable.complete());

        deploymentHandler.updateOne(rc)
            .blockingSubscribe(() -> {},
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
        testContext.completeNow();
    }

    @Disabled
    @Test
    void updateOneTerminationFailed(VertxTestContext testContext) {
        long deploymentId = 1L;
        Account account = TestAccountProvider.createAccount(1L);
        Deployment deployment = TestDeploymentProvider.createDeployment(1L, true, account);
        JsonObject deploymentJson = JsonObject.mapFrom(deployment);

        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        when(rc.pathParam("id")).thenReturn(String.valueOf(deploymentId));
        when(deploymentChecker.checkFindOne(deploymentId, account.getAccountId()))
            .thenReturn(Single.just(deploymentJson));
        when(resourceDeploymentChecker.submitUpdateStatus(deploymentId, DeploymentStatusValue.TERMINATING))
            .thenReturn(Completable.complete());
        when(deploymentExecutionHandler.terminateResources(deployment, account.getAccountId()))
            .thenReturn(Completable.complete());
        when(resourceDeploymentChecker.submitUpdateStatus(deploymentId, DeploymentStatusValue.TERMINATED))
            .thenReturn(Completable.complete());

        deploymentHandler.updateOne(rc)
            .blockingSubscribe(() -> {},
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );

        testContext.completeNow();
    }

    @Disabled
    @Test
    void updateOneNotFound(VertxTestContext testContext) {
        long deploymentId = 1L;
        Account account = TestAccountProvider.createAccount(1L);

        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        when(rc.pathParam("id")).thenReturn(String.valueOf(deploymentId));
        when(deploymentChecker.checkFindOne(deploymentId, account.getAccountId()))
            .thenReturn(Single.error(NotFoundException::new));

        deploymentHandler.updateOne(rc)
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }
}
