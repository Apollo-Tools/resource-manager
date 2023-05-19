package at.uibk.dps.rm.handler.reservation;

import at.uibk.dps.rm.entity.deployment.ProcessOutput;
import at.uibk.dps.rm.entity.deployment.ReservationStatusValue;
import at.uibk.dps.rm.entity.deployment.output.DeploymentOutput;
import at.uibk.dps.rm.entity.dto.DeployResourcesRequest;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.service.rxjava3.database.reservation.ResourceReservationService;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class ResourceReservationCheckerTest {

    private ResourceReservationChecker resourceReservationChecker;

    @Mock
    ResourceReservationService resourceReservationService;

    @Mock
    ProcessOutput processOutput;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        resourceReservationChecker = new ResourceReservationChecker(resourceReservationService);
    }

    @Test
    void checkFindAllByReservationIdValid(VertxTestContext testContext) {
        long reservationId = 1L;
        Account account = TestAccountProvider.createAccount(1L);
        Reservation reservation = TestReservationProvider.createReservation(reservationId, false, account);
        FunctionResource functionResource1 = TestFunctionProvider.createFunctionResource(1L);
        FunctionResource functionResource2 = TestFunctionProvider.createFunctionResource(2L);
        FunctionResource functionResource3 = TestFunctionProvider.createFunctionResource(3L);
        ResourceReservation resourceReservation1 = TestReservationProvider.createResourceReservation(1L, functionResource1,
            reservation, new ResourceReservationStatus());
        ResourceReservation resourceReservation2 = TestReservationProvider.createResourceReservation(2L, functionResource2,
            reservation, new ResourceReservationStatus());
        ResourceReservation resourceReservation3 = TestReservationProvider.createResourceReservation(3L, functionResource3,
            reservation, new ResourceReservationStatus());
        JsonArray resourceReservations = new JsonArray(List.of(JsonObject.mapFrom(resourceReservation1),
            JsonObject.mapFrom(resourceReservation2), JsonObject.mapFrom(resourceReservation3)));

        when(resourceReservationService.findAllByReservationId(reservationId)).thenReturn(Single.just(resourceReservations));

        resourceReservationChecker.checkFindAllByReservationId(reservationId)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.size()).isEqualTo(3);
                    assertThat(result.getJsonObject(0).getJsonObject("function_resource")
                        .getLong("function_resource_id")).isEqualTo(1L);
                    assertThat(result.getJsonObject(1).getJsonObject("function_resource")
                        .getLong("function_resource_id")).isEqualTo(2L);
                    assertThat(result.getJsonObject(2).getJsonObject("function_resource")
                        .getLong("function_resource_id")).isEqualTo(3L);
                    verify(resourceReservationService).findAllByReservationId(reservationId);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void checkFindAllByReservationIdEmptyList(VertxTestContext testContext) {
        long reservationId = 1L;
        JsonArray resourceReservations = new JsonArray(new ArrayList<JsonObject>());

        when(resourceReservationService.findAllByReservationId(reservationId)).thenReturn(Single.just(resourceReservations));

        resourceReservationChecker.checkFindAllByReservationId(reservationId)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.size()).isEqualTo(0);
                    verify(resourceReservationService).findAllByReservationId(reservationId);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void checkFindAllByReservationIdNotFound(VertxTestContext testContext) {
        long reservationId = 1L;
        Single<JsonArray> handler = SingleHelper.getEmptySingle();

        when(resourceReservationService.findAllByReservationId(reservationId)).thenReturn(handler);

        resourceReservationChecker.checkFindAllByReservationId(reservationId)
            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void storeOutputToFunctionResources(VertxTestContext testContext) {
        DeployResourcesRequest request = TestRequestProvider.createDeployRequest();
        long reservationId = request.getReservation().getReservationId();
        DeploymentOutput deploymentOutput = TestDTOProvider.createDeploymentOutput();

        when(processOutput.getOutput()).thenReturn(JsonObject.mapFrom(deploymentOutput).encode());
        when(resourceReservationService.updateTriggerUrl(1L, "http://localhostfaas1"))
            .thenReturn(Completable.complete());
        when(resourceReservationService.updateTriggerUrl(2L, "http://localhostvm1"))
            .thenReturn(Completable.complete());
        when(resourceReservationService.updateTriggerUrl(3L, "http://localhostvm2"))
            .thenReturn(Completable.complete());
        when(resourceReservationService.updateTriggerUrl(4L, "http://localhostedge1"))
            .thenReturn(Completable.complete());

        resourceReservationChecker.storeOutputToResourceReservations(processOutput, request)
            .blockingSubscribe(() -> {},
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
        testContext.completeNow();
    }

    @Test
    void storeOutputToFunctionResourcesRuntimeNotMatching(VertxTestContext testContext) {
        DeployResourcesRequest request = TestRequestProvider.createDeployRequest();
        DeploymentOutput deploymentOutput = TestDTOProvider.createDeploymentOutputUnknownFunction();

        when(processOutput.getOutput()).thenReturn(JsonObject.mapFrom(deploymentOutput).encode());

        resourceReservationChecker.storeOutputToResourceReservations(processOutput, request)
            .blockingSubscribe(() -> {},
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
        testContext.completeNow();
    }

    private static Stream<Arguments> provideStatusValue() {
        final ResourceReservationStatus statusNew = TestReservationProvider.createResourceReservationStatusNew();
        final ResourceReservationStatus statusDeployed = TestReservationProvider
            .createResourceReservationStatusDeployed();
        final ResourceReservationStatus statusTerminating = TestReservationProvider
            .createResourceReservationStatusTerminating();
        final ResourceReservationStatus statusTerminated = TestReservationProvider
            .createResourceReservationStatusTerminated();
        final ResourceReservationStatus statusError = TestReservationProvider
            .createResourceReservationStatusError();
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
    void checkCrucialReservationStatus(ResourceReservationStatus expectedStatus) {
        Reservation reservation = TestReservationProvider.createReservation(1L);
        ResourceReservation rr1 = TestReservationProvider.createResourceReservation(1L, new FunctionResource(),
            reservation, TestReservationProvider.createResourceReservationStatusTerminated());
        ResourceReservation rr2 = TestReservationProvider.createResourceReservation(2L, new FunctionResource(),
            reservation, expectedStatus);
        JsonArray resourceReservations = new JsonArray(List.of(JsonObject.mapFrom(rr1), JsonObject.mapFrom(rr2)));

        ReservationStatusValue result = resourceReservationChecker
            .checkCrucialResourceReservationStatus(resourceReservations);

        assertThat(result.name()).isEqualTo(expectedStatus.getStatusValue());
    }
}
