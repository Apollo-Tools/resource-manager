package at.uibk.dps.rm.handler.deployment;

import at.uibk.dps.rm.entity.dto.DeployResourcesRequest;
import at.uibk.dps.rm.entity.dto.deployment.FunctionResourceIds;
import at.uibk.dps.rm.entity.dto.deployment.ServiceResourceIds;
import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.entity.model.VPC;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.handler.account.CredentialsChecker;
import at.uibk.dps.rm.handler.function.FunctionChecker;
import at.uibk.dps.rm.handler.metric.PlatformMetricChecker;
import at.uibk.dps.rm.handler.resource.ResourceChecker;
import at.uibk.dps.rm.handler.resourceprovider.VPCChecker;
import at.uibk.dps.rm.handler.service.ServiceChecker;
import at.uibk.dps.rm.testutil.objectprovider.*;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.when;

/**
 * Implements tests for the {@link DeploymentPreconditionHandler} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class DeploymentPreconditionHandlerTest {

    private DeploymentPreconditionHandler handler;

    @Mock
    private FunctionChecker functionChecker;

    @Mock
    private ServiceChecker serviceChecker;

    @Mock
    private ResourceChecker resourceChecker;

    @Mock
    private PlatformMetricChecker resourceTypeMetricChecker;

    @Mock
    private VPCChecker vpcChecker;

    @Mock
    private CredentialsChecker credentialsChecker;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        handler = new DeploymentPreconditionHandler(functionChecker, serviceChecker,
            resourceChecker, resourceTypeMetricChecker, vpcChecker, credentialsChecker);
    }

    @ParameterizedTest
    @ValueSource(strings = {"valid", "vpcNotFound", "missingMetrics", "noCloudResources"})
    void checkDeploymentIsValid(String testCase, VertxTestContext testContext) {
        Resource r1 = TestResourceProvider.createResourceLambda(1L, 250.0, 612.0);
        Resource r2 = TestResourceProvider.createResourceEC2(2L, 150.0, 512.0,
            "t2.micro");
        Resource r3 = TestResourceProvider.createResourceOpenFaas(3L, 250.0, 512.0,
            "http://localhost:8080", "user", "pw");
        Resource r4 = TestResourceProvider.createResourceContainer(4L, "https://localhost", true);
        JsonArray resources = new JsonArray(List.of(JsonObject.mapFrom(r1), JsonObject.mapFrom(r2),
            JsonObject.mapFrom(r3), JsonObject.mapFrom(r4)));
        if (testCase.equals("noCloudResources")) {
            resources = new JsonArray(List.of(JsonObject.mapFrom(r3), JsonObject.mapFrom(r4)));
        }
        List<FunctionResourceIds> fids = TestFunctionProvider.createFunctionResourceIdsList(r1.getResourceId(),
            r2.getResourceId(), r3.getResourceId());
        List<ServiceResourceIds> sids = TestServiceProvider.createServiceResourceIdsList(r4.getResourceId());
        DeployResourcesRequest request = TestRequestProvider.createDeployResourcesRequest(fids, sids);
        long accountId = 1L;
        JsonObject vpc1 = JsonObject.mapFrom(TestResourceProviderProvider.createVPC(1L, r1.getRegion()));
        List<JsonObject> vpcList = List.of(vpc1);
        List<VPC> necessaryVPCs = new ArrayList<>();

        when(functionChecker.checkExistAllByIds(request.getFunctionResources()))
            .thenReturn(Completable.complete());
        when(serviceChecker.checkExistAllByIds(request.getServiceResources()))
            .thenReturn(Completable.complete());
        when(resourceChecker.checkExistAllByIdsAndResourceType(request.getServiceResources(),
            request.getFunctionResources())).thenReturn(Completable.complete());
        when(resourceChecker.checkFindAllByResourceIds(List.of(1L, 2L, 3L, 4L)))
            .thenReturn(Single.just(resources));
        if (!testCase.equals("noCloudResources")) {
            when(credentialsChecker.checkExistsOneByProviderId(accountId, 1L))
                .thenReturn(Completable.complete());
        }
        when(resourceTypeMetricChecker.checkMissingRequiredMetricsByResources(resources))
            .thenReturn(testCase.equals("missingMetrics") ? Completable.error(NotFoundException::new) :
                Completable.complete());
        when(vpcChecker.checkVPCForFunctionResources(accountId, resources))
            .thenReturn(testCase.equals("vpcNotFound") ? Single.error(NotFoundException::new) : Single.just(vpcList));

        handler.checkDeploymentIsValid(request, accountId, necessaryVPCs)
            .subscribe(result -> testContext.verify(() -> {
                    if (testCase.equals("valid")) {
                        assertThat(result.size()).isEqualTo(4);
                        assertThat(result.getJsonObject(0).getLong("resource_id")).isEqualTo(1L);
                        assertThat(result.getJsonObject(1).getLong("resource_id")).isEqualTo(2L);
                        assertThat(result.getJsonObject(2).getLong("resource_id")).isEqualTo(3L);
                        assertThat(result.getJsonObject(3).getLong("resource_id")).isEqualTo(4L);
                        assertThat(necessaryVPCs.size()).isEqualTo(1);
                        assertThat(necessaryVPCs.get(0).getVpcId()).isEqualTo(1L);
                    } else if (testCase.equals("noCloudResources")) {
                        assertThat(result.size()).isEqualTo(2);
                        assertThat(result.getJsonObject(0).getLong("resource_id")).isEqualTo(3L);
                        assertThat(result.getJsonObject(1).getLong("resource_id")).isEqualTo(4L);
                    } else {
                        fail("method did not throw exception");
                    }
                    testContext.completeNow();
                }), throwable -> testContext.verify(() -> testContext.verify(() -> {
                    if (testCase.equals("valid")) {
                        fail("method has thrown exception");
                    } else {
                        assertThat(throwable).isInstanceOf(NotFoundException.class);
                    }
                    testContext.completeNow();
                })));
    }
}
