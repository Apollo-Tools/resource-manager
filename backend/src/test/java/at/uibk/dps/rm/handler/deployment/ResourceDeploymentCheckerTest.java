package at.uibk.dps.rm.handler.deployment;

import at.uibk.dps.rm.entity.deployment.DeploymentStatusValue;
import at.uibk.dps.rm.entity.deployment.ProcessOutput;
import at.uibk.dps.rm.entity.deployment.output.DeploymentOutput;
import at.uibk.dps.rm.entity.dto.deployment.DeployResourcesDTO;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.service.rxjava3.database.deployment.ResourceDeploymentService;
import at.uibk.dps.rm.testutil.SingleHelper;
import at.uibk.dps.rm.testutil.objectprovider.*;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
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
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.when;

/**
 * Implements tests for the {@link ResourceDeploymentChecker} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class ResourceDeploymentCheckerTest {

    private ResourceDeploymentChecker resourceDeploymentChecker;

    @Mock
    ResourceDeploymentService resourceDeploymentService;

    @Mock
    ProcessOutput processOutput;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        resourceDeploymentChecker = new ResourceDeploymentChecker(resourceDeploymentService);
    }

    @Test
    void checkFindAllByDeploymentIdValid(VertxTestContext testContext) {
        long deploymentId = 1L;
        Account account = TestAccountProvider.createAccount(1L);
        Deployment deployment = TestDeploymentProvider.createDeployment(deploymentId, false, account);
        Resource r1 = TestResourceProvider.createResource(1L);
        Resource r2 = TestResourceProvider.createResource(2L);
        Resource r3 = TestResourceProvider.createResource(3L);
        ResourceDeployment rd1 = TestDeploymentProvider.createResourceDeployment(1L, deployment,
            r1, new ResourceDeploymentStatus());
        ResourceDeployment rd2 = TestDeploymentProvider.createResourceDeployment(2L, deployment,
            r2, new ResourceDeploymentStatus());
        ResourceDeployment rd3 = TestDeploymentProvider.createResourceDeployment(3L, deployment,
            r3, new ResourceDeploymentStatus());
        JsonArray resourceDeployments = new JsonArray(List.of(JsonObject.mapFrom(rd1),
            JsonObject.mapFrom(rd2), JsonObject.mapFrom(rd3)));

        when(resourceDeploymentService.findAllByDeploymentId(deploymentId)).thenReturn(Single.just(resourceDeployments));

        resourceDeploymentChecker.checkFindAllByDeploymentId(deploymentId)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.size()).isEqualTo(3);
                    assertThat(result.getJsonObject(0).getLong("resource_deployment_id")).isEqualTo(1L);
                    assertThat(result.getJsonObject(1).getLong("resource_deployment_id")).isEqualTo(2L);
                    assertThat(result.getJsonObject(2).getLong("resource_deployment_id")).isEqualTo(3L);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void checkFindAllByDeploymentIdEmptyList(VertxTestContext testContext) {
        long deploymentId = 1L;
        JsonArray resourceDeployments = new JsonArray(new ArrayList<JsonObject>());

        when(resourceDeploymentService.findAllByDeploymentId(deploymentId))
            .thenReturn(Single.just(resourceDeployments));

        resourceDeploymentChecker.checkFindAllByDeploymentId(deploymentId)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.size()).isEqualTo(0);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void checkFindAllByDeploymentIdNotFound(VertxTestContext testContext) {
        long deploymentId = 1L;
        Single<JsonArray> handler = SingleHelper.getEmptySingle();

        when(resourceDeploymentService.findAllByDeploymentId(deploymentId)).thenReturn(handler);

        resourceDeploymentChecker.checkFindAllByDeploymentId(deploymentId)
            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }



    @Test
    void storeOutputToFunctionResources(VertxTestContext testContext) {
        DeployResourcesDTO request = TestRequestProvider.createDeployRequest();
        DeploymentOutput deploymentOutput = TestDTOProvider.createDeploymentOutput();

        when(processOutput.getOutput()).thenReturn(JsonObject.mapFrom(deploymentOutput).encode());
        when(resourceDeploymentService.updateTriggerUrl(1L, "http://localhostlambda/foo1"))
            .thenReturn(Completable.complete());
        when(resourceDeploymentService.updateTriggerUrl(2L, "http://localhostec2/foo1"))
            .thenReturn(Completable.complete());
        when(resourceDeploymentService.updateTriggerUrl(3L, "http://localhostec2/foo2"))
            .thenReturn(Completable.complete());
        when(resourceDeploymentService.updateTriggerUrl(4L, "http://localhostopenfaas/foo1"))
            .thenReturn(Completable.complete());
        when(resourceDeploymentService.updateTriggerUrl(5L, "/deployments/1/5/startup"))
            .thenReturn(Completable.complete());
        when(resourceDeploymentService.updateTriggerUrl(6L, "/deployments/1/6/startup"))
            .thenReturn(Completable.complete());

        resourceDeploymentChecker.storeOutputToResourceDeployments(processOutput, request)
            .blockingSubscribe(() -> {},
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
        testContext.completeNow();
    }

    @Test
    void storeOutputToFunctionResourcesRuntimeNotMatching(VertxTestContext testContext) {
        DeployResourcesDTO request = TestRequestProvider.createDeployRequest();
        DeploymentOutput deploymentOutput = TestDTOProvider.createDeploymentOutputUnknownFunction();

        when(processOutput.getOutput()).thenReturn(JsonObject.mapFrom(deploymentOutput).encode());
        when(resourceDeploymentService.updateTriggerUrl(5L, "/deployments/1/5/startup"))
            .thenReturn(Completable.complete());
        when(resourceDeploymentService.updateTriggerUrl(6L, "/deployments/1/6/startup"))
            .thenReturn(Completable.complete());

        resourceDeploymentChecker.storeOutputToResourceDeployments(processOutput, request)
            .blockingSubscribe(() -> {},
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
        testContext.completeNow();
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
            new Resource(), TestDeploymentProvider.createResourceDeploymentStatusTerminated());
        ResourceDeployment rd2 = TestDeploymentProvider.createResourceDeployment(2L, deployment, new Resource()
            , expectedStatus);
        JsonArray resourceDeployments = new JsonArray(List.of(JsonObject.mapFrom(rd1), JsonObject.mapFrom(rd2)));

        DeploymentStatusValue result = resourceDeploymentChecker
            .checkCrucialResourceDeploymentStatus(resourceDeployments);

        assertThat(result.name()).isEqualTo(expectedStatus.getStatusValue());
    }
}
