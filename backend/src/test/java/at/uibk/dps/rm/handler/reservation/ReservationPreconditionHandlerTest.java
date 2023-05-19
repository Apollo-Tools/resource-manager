package at.uibk.dps.rm.handler.reservation;

import at.uibk.dps.rm.entity.dto.ReserveResourcesRequest;
import at.uibk.dps.rm.entity.dto.reservation.FunctionResourceIds;
import at.uibk.dps.rm.entity.dto.reservation.ServiceResourceIds;
import at.uibk.dps.rm.entity.model.Region;
import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.entity.model.VPC;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.handler.account.CredentialsChecker;
import at.uibk.dps.rm.handler.function.FunctionChecker;
import at.uibk.dps.rm.handler.metric.ResourceTypeMetricChecker;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.when;

/**
 * Implements tests for the {@link ReservationPreconditionHandler} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class ReservationPreconditionHandlerTest {

    private ReservationPreconditionHandler handler;

    @Mock
    private FunctionChecker functionChecker;

    @Mock
    private ServiceChecker serviceChecker;

    @Mock
    private ResourceChecker resourceChecker;

    @Mock
    private ResourceTypeMetricChecker resourceTypeMetricChecker;

    @Mock
    private VPCChecker vpcChecker;

    @Mock
    private CredentialsChecker credentialsChecker;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        handler = new ReservationPreconditionHandler(functionChecker, serviceChecker,
            resourceChecker, resourceTypeMetricChecker, vpcChecker, credentialsChecker);
    }

    @Test
    void checkReservationIsValid(VertxTestContext testContext) {
        Region region = TestResourceProviderProvider.createRegion(1L, "us-east-1");
        Resource r1 = TestResourceProvider.createResourceFaaS(1L, region, 250.0, 512.0);
        Resource r2 = TestResourceProvider.createResourceVM(2L, region, "t2.micro");
        Resource r3 = TestResourceProvider.createResourceEdge(3L, "http://localhost:8080",
            "user", "pw");
        Resource r4 = TestResourceProvider.createResourceContainer(4L, "https://localhost", 1, 0.5,
            256);
        JsonArray resources = new JsonArray(List.of(JsonObject.mapFrom(r1), JsonObject.mapFrom(r2),
            JsonObject.mapFrom(r3), JsonObject.mapFrom(r4)));
        List<FunctionResourceIds> fids = TestFunctionProvider.createFunctionResourceIdsList(r1.getResourceId(),
            r2.getResourceId(), r3.getResourceId());
        List<ServiceResourceIds> sids = TestServiceProvider.createServiceResourceIdsList(r4.getResourceId());
        ReserveResourcesRequest request = TestRequestProvider.createReserveResourcesRequest(fids, sids);
        long accountId = 1L;
        JsonObject vpc1 = JsonObject.mapFrom(TestResourceProviderProvider.createVPC(1L, region));
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
        when(credentialsChecker.checkExistsOneByProviderId(accountId, 1L))
            .thenReturn(Completable.complete());
        when(resourceTypeMetricChecker.checkMissingRequiredMetricsByResources(resources))
            .thenReturn(Completable.complete());
        when(vpcChecker.checkVPCForFunctionResources(accountId, resources))
            .thenReturn(Single.just(vpcList));

        handler.checkReservationIsValid(request, accountId, necessaryVPCs)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.size()).isEqualTo(4);
                    assertThat(result.getJsonObject(0).getLong("resource_id")).isEqualTo(1L);
                    assertThat(result.getJsonObject(1).getLong("resource_id")).isEqualTo(2L);
                    assertThat(result.getJsonObject(2).getLong("resource_id")).isEqualTo(3L);
                    assertThat(result.getJsonObject(3).getLong("resource_id")).isEqualTo(4L);
                    assertThat(necessaryVPCs.size()).isEqualTo(1);
                    assertThat(necessaryVPCs.get(0).getVpcId()).isEqualTo(1L);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void checkReservationIsValidVPCNotFound(VertxTestContext testContext) {
        Region region = TestResourceProviderProvider.createRegion(1L, "us-east-1");
        Resource r1 = TestResourceProvider.createResourceFaaS(1L, region, 250.0, 512.0);
        JsonArray resources = new JsonArray(List.of(JsonObject.mapFrom(r1)));
        List<FunctionResourceIds> fids = List.of(TestFunctionProvider.createFunctionResourceIds(1L,
            r1.getResourceId()));
        List<ServiceResourceIds> sids = List.of();
        ReserveResourcesRequest request = TestRequestProvider.createReserveResourcesRequest(fids, sids);
        long accountId = 1L;
        List<VPC> necessaryVPCs = new ArrayList<>();

        when(functionChecker.checkExistAllByIds(request.getFunctionResources()))
            .thenReturn(Completable.complete());
        when(serviceChecker.checkExistAllByIds(request.getServiceResources()))
            .thenReturn(Completable.complete());
        when(resourceChecker.checkExistAllByIdsAndResourceType(request.getServiceResources(),
            request.getFunctionResources())).thenReturn(Completable.complete());
        when(resourceChecker.checkFindAllByResourceIds(List.of(1L)))
            .thenReturn(Single.just(resources));
        when(credentialsChecker.checkExistsOneByProviderId(accountId, 1L)).thenReturn(Completable.complete());
        when(resourceTypeMetricChecker.checkMissingRequiredMetricsByResources(resources))
            .thenReturn(Completable.complete());
        when(vpcChecker.checkVPCForFunctionResources(accountId, resources))
            .thenReturn(Single.error(NotFoundException::new));

        handler.checkReservationIsValid(request, accountId, necessaryVPCs)
            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void checkReservationIsValidMissingMetrics(VertxTestContext testContext) {
        Region region = TestResourceProviderProvider.createRegion(1L, "us-east-1");
        Resource r1 = TestResourceProvider.createResourceFaaS(1L, region, 250.0, 512.0);
        JsonArray resources = new JsonArray(List.of(JsonObject.mapFrom(r1)));
        List<FunctionResourceIds> fids = List.of(TestFunctionProvider.createFunctionResourceIds(1L,
            r1.getResourceId()));
        List<ServiceResourceIds> sids = List.of();
        ReserveResourcesRequest request = TestRequestProvider.createReserveResourcesRequest(fids, sids);
        long accountId = 1L;
        List<VPC> necessaryVPCs = new ArrayList<>();
        List<JsonObject> vpcList = new ArrayList<>();

        when(functionChecker.checkExistAllByIds(request.getFunctionResources()))
            .thenReturn(Completable.complete());
        when(serviceChecker.checkExistAllByIds(request.getServiceResources()))
            .thenReturn(Completable.complete());
        when(resourceChecker.checkExistAllByIdsAndResourceType(request.getServiceResources(),
            request.getFunctionResources())).thenReturn(Completable.complete());
        when(resourceChecker.checkFindAllByResourceIds(List.of(1L)))
            .thenReturn(Single.just(resources));
        when(credentialsChecker.checkExistsOneByProviderId(accountId, 1L)).thenReturn(Completable.complete());
        when(resourceTypeMetricChecker.checkMissingRequiredMetricsByResources(resources))
            .thenReturn(Completable.error(NotFoundException::new));
        when(vpcChecker.checkVPCForFunctionResources(accountId, resources))
            .thenReturn(Single.just(vpcList));

        handler.checkReservationIsValid(request, accountId, necessaryVPCs)
            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void checkReservationIsValidMissingCredentials(VertxTestContext testContext) {
        Region region = TestResourceProviderProvider.createRegion(1L, "us-east-1");
        Resource r1 = TestResourceProvider.createResourceFaaS(1L, region, 250.0, 512.0);
        JsonArray resources = new JsonArray(List.of(JsonObject.mapFrom(r1)));
        List<FunctionResourceIds> fids = List.of(TestFunctionProvider.createFunctionResourceIds(1L,
            r1.getResourceId()));
        List<ServiceResourceIds> sids = List.of();
        ReserveResourcesRequest request = TestRequestProvider.createReserveResourcesRequest(fids, sids);
        long accountId = 1L;
        List<VPC> necessaryVPCs = new ArrayList<>();
        List<JsonObject> vpcList = new ArrayList<>();

        when(functionChecker.checkExistAllByIds(request.getFunctionResources()))
            .thenReturn(Completable.complete());
        when(serviceChecker.checkExistAllByIds(request.getServiceResources()))
            .thenReturn(Completable.complete());
        when(resourceChecker.checkExistAllByIdsAndResourceType(request.getServiceResources(),
            request.getFunctionResources())).thenReturn(Completable.complete());
        when(resourceChecker.checkFindAllByResourceIds(List.of(1L)))
            .thenReturn(Single.just(resources));
        when(credentialsChecker.checkExistsOneByProviderId(accountId, 1L))
            .thenReturn(Completable.error(NotFoundException::new));
        when(resourceTypeMetricChecker.checkMissingRequiredMetricsByResources(resources))
            .thenReturn(Completable.complete());
        when(vpcChecker.checkVPCForFunctionResources(accountId, resources))
            .thenReturn(Single.just(vpcList));


        handler.checkReservationIsValid(request, accountId, necessaryVPCs)
            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }
}
