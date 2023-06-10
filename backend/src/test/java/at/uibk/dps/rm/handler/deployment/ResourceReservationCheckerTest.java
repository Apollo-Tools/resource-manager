package at.uibk.dps.rm.handler.deployment;

import at.uibk.dps.rm.entity.deployment.DeploymentStatusValue;
import at.uibk.dps.rm.entity.deployment.ProcessOutput;
import at.uibk.dps.rm.entity.deployment.output.DeploymentOutput;
import at.uibk.dps.rm.entity.dto.deployment.DeployResourcesDAO;
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

/**
 * Implements tests for the {@link ResourceDeploymentChecker} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class ResourceReservationCheckerTest {

    private ResourceDeploymentChecker resourceReservationChecker;

    @Mock
    ResourceReservationService resourceReservationService;

    @Mock
    ProcessOutput processOutput;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        resourceReservationChecker = new ResourceDeploymentChecker(resourceReservationService);
    }

    @Test
    void checkFindAllByReservationIdValid(VertxTestContext testContext) {
        long reservationId = 1L;
        Account account = TestAccountProvider.createAccount(1L);
        Deployment reservation = TestReservationProvider.createReservation(reservationId, false, account);
        Resource r1 = TestResourceProvider.createResource(1L);
        Resource r2 = TestResourceProvider.createResource(2L);
        Resource r3 = TestResourceProvider.createResource(3L);
        ResourceDeployment resourceReservation1 = TestReservationProvider.createResourceReservation(1L, reservation,
            r1, new ResourceDeploymentStatus());
        ResourceDeployment resourceReservation2 = TestReservationProvider.createResourceReservation(2L, reservation,
            r2, new ResourceDeploymentStatus());
        ResourceDeployment resourceReservation3 = TestReservationProvider.createResourceReservation(3L, reservation,
            r3, new ResourceDeploymentStatus());
        JsonArray resourceReservations = new JsonArray(List.of(JsonObject.mapFrom(resourceReservation1),
            JsonObject.mapFrom(resourceReservation2), JsonObject.mapFrom(resourceReservation3)));

        when(resourceReservationService.findAllByReservationId(reservationId)).thenReturn(Single.just(resourceReservations));

        resourceReservationChecker.checkFindAllByDeploymentId(reservationId)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.size()).isEqualTo(3);
                    assertThat(result.getJsonObject(0).getLong("resource_reservation_id")).isEqualTo(1L);
                    assertThat(result.getJsonObject(1).getLong("resource_reservation_id")).isEqualTo(2L);
                    assertThat(result.getJsonObject(2).getLong("resource_reservation_id")).isEqualTo(3L);
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

        resourceReservationChecker.checkFindAllByDeploymentId(reservationId)
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

        resourceReservationChecker.checkFindAllByDeploymentId(reservationId)
            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }



    @Test
    void storeOutputToFunctionResources(VertxTestContext testContext) {
        DeployResourcesDAO request = TestRequestProvider.createDeployRequest();
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
        when(resourceReservationService.updateTriggerUrl(5L, "/reservations/1/5/deploy"))
            .thenReturn(Completable.complete());
        when(resourceReservationService.updateTriggerUrl(6L, "/reservations/1/6/deploy"))
            .thenReturn(Completable.complete());

        resourceReservationChecker.storeOutputToResourceDeployments(processOutput, request)
            .blockingSubscribe(() -> {},
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
        testContext.completeNow();
    }

    @Test
    void storeOutputToFunctionResourcesRuntimeNotMatching(VertxTestContext testContext) {
        DeployResourcesDAO request = TestRequestProvider.createDeployRequest();
        DeploymentOutput deploymentOutput = TestDTOProvider.createDeploymentOutputUnknownFunction();

        when(processOutput.getOutput()).thenReturn(JsonObject.mapFrom(deploymentOutput).encode());
        when(resourceReservationService.updateTriggerUrl(5L, "/reservations/1/5/deploy"))
            .thenReturn(Completable.complete());
        when(resourceReservationService.updateTriggerUrl(6L, "/reservations/1/6/deploy"))
            .thenReturn(Completable.complete());

        resourceReservationChecker.storeOutputToResourceDeployments(processOutput, request)
            .blockingSubscribe(() -> {},
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
        testContext.completeNow();
    }

    private static Stream<Arguments> provideStatusValue() {
        final ResourceDeploymentStatus statusNew = TestReservationProvider.createResourceReservationStatusNew();
        final ResourceDeploymentStatus statusDeployed = TestReservationProvider
            .createResourceReservationStatusDeployed();
        final ResourceDeploymentStatus statusTerminating = TestReservationProvider
            .createResourceReservationStatusTerminating();
        final ResourceDeploymentStatus statusTerminated = TestReservationProvider
            .createResourceReservationStatusTerminated();
        final ResourceDeploymentStatus statusError = TestReservationProvider
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
    void checkCrucialReservationStatus(ResourceDeploymentStatus expectedStatus) {
        Deployment reservation = TestReservationProvider.createReservation(1L);
        ResourceDeployment rr1 = TestReservationProvider.createResourceReservation(1L, reservation,
            new Resource(), TestReservationProvider.createResourceReservationStatusTerminated());
        ResourceDeployment rr2 = TestReservationProvider.createResourceReservation(2L, reservation, new Resource()
            , expectedStatus);
        JsonArray resourceReservations = new JsonArray(List.of(JsonObject.mapFrom(rr1), JsonObject.mapFrom(rr2)));

        DeploymentStatusValue result = resourceReservationChecker
            .checkCrucialResourceDeploymentStatus(resourceReservations);

        assertThat(result.name()).isEqualTo(expectedStatus.getStatusValue());
    }
}
