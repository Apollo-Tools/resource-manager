package at.uibk.dps.rm.handler.reservation;

import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.service.rxjava3.database.reservation.ResourceReservationService;
import at.uibk.dps.rm.testutil.SingleHelper;
import at.uibk.dps.rm.testutil.TestObjectProvider;
import at.uibk.dps.rm.util.JsonMapperConfig;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class ResourceReservationCheckerTest {

    ResourceReservationChecker resourceReservationChecker;

    @Mock
    ResourceReservationService resourceReservationService;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        resourceReservationChecker = new ResourceReservationChecker(resourceReservationService);
    }

    @Test
    void checkFindAllByReservationIdValid(VertxTestContext testContext) {
        long reservationId = 1L;
        Account account = TestObjectProvider.createAccount(1L);
        Reservation reservation = TestObjectProvider.createReservation(reservationId, false, account);
        FunctionResource functionResource1 = TestObjectProvider.createFunctionResource(1L);
        FunctionResource functionResource2 = TestObjectProvider.createFunctionResource(2L);
        FunctionResource functionResource3 = TestObjectProvider.createFunctionResource(3L);
        ResourceReservation resourceReservation1 = TestObjectProvider.createResourceReservation(1L, functionResource1,
            reservation, false);
        ResourceReservation resourceReservation2 = TestObjectProvider.createResourceReservation(2L, functionResource2,
            reservation, true);
        ResourceReservation resourceReservation3 = TestObjectProvider.createResourceReservation(3L, functionResource3,
            reservation, true);
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
                throwable -> testContext.verify(() -> fail("method did throw exception"))
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
                throwable -> testContext.verify(() -> fail("method did throw exception"))
            );
    }

    @Test
    void checkFindAllByReservationIdNotFound(VertxTestContext testContext) {
        long reservationId = 1L;
        Single<JsonArray> handler = new SingleHelper<JsonArray>().getEmptySingle();

        when(resourceReservationService.findAllByReservationId(reservationId)).thenReturn(handler);

        resourceReservationChecker.checkFindAllByReservationId(reservationId)
            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }
}
