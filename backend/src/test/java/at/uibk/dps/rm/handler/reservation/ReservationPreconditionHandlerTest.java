package at.uibk.dps.rm.handler.reservation;

import at.uibk.dps.rm.entity.dto.ReserveResourcesRequest;
import at.uibk.dps.rm.entity.dto.reservation.FunctionResourceIds;
import at.uibk.dps.rm.entity.model.Region;
import at.uibk.dps.rm.entity.model.VPC;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.handler.account.CredentialsChecker;
import at.uibk.dps.rm.handler.function.FunctionResourceChecker;
import at.uibk.dps.rm.handler.metric.ResourceTypeMetricChecker;
import at.uibk.dps.rm.handler.resourceprovider.VPCChecker;
import at.uibk.dps.rm.testutil.objectprovider.TestFunctionProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestRequestProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestResourceProviderProvider;
import at.uibk.dps.rm.util.JsonMapperConfig;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
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

@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class ReservationPreconditionHandlerTest {

    private ReservationPreconditionHandler handler;

    @Mock
    private FunctionResourceChecker functionResourceChecker;

    @Mock
    private ResourceTypeMetricChecker resourceTypeMetricChecker;

    @Mock
    private VPCChecker vpcChecker;

    @Mock
    private CredentialsChecker credentialsChecker;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        handler = new ReservationPreconditionHandler(functionResourceChecker, resourceTypeMetricChecker, vpcChecker,
            credentialsChecker);
    }

    @Test
    void checkReservationIsValid(VertxTestContext testContext) {
        FunctionResourceIds ids1 = TestFunctionProvider.createFunctionResourceIds(1L, 1L);
        FunctionResourceIds ids2 = TestFunctionProvider.createFunctionResourceIds(2L, 6L);
        FunctionResourceIds ids3 = TestFunctionProvider.createFunctionResourceIds(3L, 2L);
        FunctionResourceIds ids4 = TestFunctionProvider.createFunctionResourceIds(4L, 1L);
        List<FunctionResourceIds> ids = List.of(ids1, ids2, ids3, ids4);
        List<JsonObject> functionResources = new ArrayList<>();
        Region region1 = TestResourceProviderProvider.createRegion(1L, "us-east-1");
        Region edge= TestResourceProviderProvider.createRegionEdge(2L);
        for (int i = 0; i < ids.size(); i++) {
            Region newResourceRegion = region1;
            if (ids.get(i).getResourceId() == 6L) {
                newResourceRegion = edge;
            }
            functionResources.add(JsonObject.mapFrom(TestFunctionProvider.createFunctionResource(i,
                ids.get(i).getFunctionId(), ids.get(i).getResourceId(), newResourceRegion)));
        }
        ReserveResourcesRequest request = TestRequestProvider.createReserveResourcesRequest(ids);
        long accountId = 1L;
        JsonObject vpc1 = JsonObject.mapFrom(TestResourceProviderProvider.createVPC(1L, region1));
        List<JsonObject> vpcList = List.of(vpc1);
        List<VPC> necessaryVPCs = new ArrayList<>();

        for (int i = 0; i < ids.size(); i++) {
            when(functionResourceChecker.checkFindOneByFunctionAndResource(ids.get(i).getFunctionId(),
                ids.get(i).getResourceId())).thenReturn(Single.just(functionResources.get(i)));
        }
        when(credentialsChecker.checkExistsOneByProviderId(accountId, 1L)).thenReturn(Completable.complete());
        when(resourceTypeMetricChecker.checkMissingRequiredMetricsByFunctionResources(functionResources))
            .thenReturn(Completable.complete());
        when(vpcChecker.checkVPCForFunctionResources(accountId, functionResources)).thenReturn(Single.just(vpcList));

        handler.checkReservationIsValid(request, accountId, necessaryVPCs)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.size()).isEqualTo(4);
                    assertThat(result.get(0).getJsonObject("function").getLong("function_id"))
                        .isEqualTo(1L);
                    assertThat(result.get(0).getJsonObject("resource")
                        .getLong("resource_id")).isEqualTo(1L);
                    assertThat(result.get(1).getJsonObject("function")
                        .getLong("function_id")).isEqualTo(2L);
                    assertThat(result.get(1).getJsonObject("resource")
                        .getLong("resource_id")).isEqualTo(6L);
                    assertThat(result.get(2).getJsonObject("function")
                        .getLong("function_id")).isEqualTo(3L);
                    assertThat(result.get(2).getJsonObject("resource")
                        .getLong("resource_id")).isEqualTo(2L);
                    assertThat(result.get(3).getJsonObject("function")
                        .getLong("function_id")).isEqualTo(4L);
                    assertThat(result.get(3).getJsonObject("resource")
                        .getLong("resource_id")).isEqualTo(1L);
                    assertThat(necessaryVPCs.size()).isEqualTo(1);
                    assertThat(necessaryVPCs.get(0).getVpcId()).isEqualTo(1L);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void checkReservationIsValidVPCNotFound(VertxTestContext testContext) {
        FunctionResourceIds ids1 = TestFunctionProvider.createFunctionResourceIds(1L, 1L);
        List<FunctionResourceIds> ids = List.of(ids1);
        List<JsonObject> functionResources = new ArrayList<>();
        Region region1 = TestResourceProviderProvider.createRegion(1L, "us-east-1");
        functionResources.add(JsonObject.mapFrom(TestFunctionProvider.createFunctionResource(1L, ids1.getFunctionId(),
            ids1.getResourceId(), region1)));
        ReserveResourcesRequest request = TestRequestProvider.createReserveResourcesRequest(ids);
        long accountId = 1L;
        List<VPC> necessaryVPCs = new ArrayList<>();
        when(functionResourceChecker.checkFindOneByFunctionAndResource(ids1.getFunctionId(),
            ids1.getResourceId())).thenReturn(Single.just(functionResources.get(0)));
        when(credentialsChecker.checkExistsOneByProviderId(accountId, 1L)).thenReturn(Completable.complete());
        when(resourceTypeMetricChecker.checkMissingRequiredMetricsByFunctionResources(functionResources))
            .thenReturn(Completable.complete());
        when(vpcChecker.checkVPCForFunctionResources(accountId, functionResources))
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
        FunctionResourceIds ids1 = TestFunctionProvider.createFunctionResourceIds(1L, 1L);
        List<FunctionResourceIds> ids = List.of(ids1);
        List<JsonObject> functionResources = new ArrayList<>();
        Region region1 = TestResourceProviderProvider.createRegion(1L, "us-east-1");
        functionResources.add(JsonObject.mapFrom(TestFunctionProvider.createFunctionResource(1L, ids1.getFunctionId(),
            ids1.getResourceId(), region1)));
        ReserveResourcesRequest request = TestRequestProvider.createReserveResourcesRequest(ids);
        long accountId = 1L;
        List<VPC> necessaryVPCs = new ArrayList<>();
        List<JsonObject> vpcList = new ArrayList<>();
        when(functionResourceChecker.checkFindOneByFunctionAndResource(ids1.getFunctionId(),
            ids1.getResourceId())).thenReturn(Single.just(functionResources.get(0)));
        when(credentialsChecker.checkExistsOneByProviderId(accountId, 1L)).thenReturn(Completable.complete());
        when(resourceTypeMetricChecker.checkMissingRequiredMetricsByFunctionResources(functionResources))
            .thenReturn(Completable.error(NotFoundException::new));
        when(vpcChecker.checkVPCForFunctionResources(accountId, functionResources)).thenReturn(Single.just(vpcList));

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
        FunctionResourceIds ids1 = TestFunctionProvider.createFunctionResourceIds(1L, 1L);
        List<FunctionResourceIds> ids = List.of(ids1);
        List<JsonObject> functionResources = new ArrayList<>();
        Region region1 = TestResourceProviderProvider.createRegion(1L, "us-east-1");
        functionResources.add(JsonObject.mapFrom(TestFunctionProvider.createFunctionResource(1L, ids1.getFunctionId(),
            ids1.getResourceId(), region1)));
        ReserveResourcesRequest request = TestRequestProvider.createReserveResourcesRequest(ids);
        long accountId = 1L;
        List<VPC> necessaryVPCs = new ArrayList<>();
        List<JsonObject> vpcList = new ArrayList<>();
        when(functionResourceChecker.checkFindOneByFunctionAndResource(ids1.getFunctionId(),
            ids1.getResourceId())).thenReturn(Single.just(functionResources.get(0)));
        when(credentialsChecker.checkExistsOneByProviderId(accountId, 1L))
            .thenReturn(Completable.error(NotFoundException::new));
        when(resourceTypeMetricChecker.checkMissingRequiredMetricsByFunctionResources(functionResources))
            .thenReturn(Completable.error(NotFoundException::new));
        when(vpcChecker.checkVPCForFunctionResources(accountId, functionResources)).thenReturn(Single.just(vpcList));


        handler.checkReservationIsValid(request, accountId, necessaryVPCs)
            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void checkReservationIsValidFunctionResourceNotFound(VertxTestContext testContext) {
        FunctionResourceIds ids1 = TestFunctionProvider.createFunctionResourceIds(1L, 1L);
        List<FunctionResourceIds> ids = List.of(ids1);
        ReserveResourcesRequest request = TestRequestProvider.createReserveResourcesRequest(ids);
        long accountId = 1L;
        List<VPC> necessaryVPCs = new ArrayList<>();
        when(functionResourceChecker.checkFindOneByFunctionAndResource(ids1.getFunctionId(),
            ids1.getResourceId())).thenReturn(Single.error(NotFoundException::new));

        handler.checkReservationIsValid(request, accountId, necessaryVPCs)
            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }
}
